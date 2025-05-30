package pe.edu.vallegrande.database.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.edu.vallegrande.database.dto.BasicServiceDTO;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.dto.HousingDetailsDTO;
import pe.edu.vallegrande.database.security.TestSecurityConfig;
import pe.edu.vallegrande.database.service.FamilyService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@ExtendWith(SpringExtension.class)
@WebFluxTest(FamilyController.class)
@Import(TestSecurityConfig.class)
@DisplayName("FamilyController - Pruebas de Integración")
class FamilyControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FamilyService familyService;

    private FamilyDTO testFamilyDTO;
    private List<FamilyDTO> testFamilyList;

    @BeforeEach
    void setUp() {
        testFamilyDTO = createTestFamilyDTO();
        testFamilyList = createTestFamilyList();
    }

    // ========== TESTS PARA GET /api/v1/families/active (Sin autenticación requerida) ==========

    @Test
    @DisplayName("GET /active - Debe retornar todas las familias activas")
    void getAllActiveFamilies_ShouldReturnActiveFamilies() {
        // Given
        when(familyService.findAllActive()).thenReturn(Flux.fromIterable(testFamilyList));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/families/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(FamilyDTO.class)
                .hasSize(2)
                .contains(testFamilyList.get(0), testFamilyList.get(1));
    }

    @Test
    @DisplayName("GET /active - Debe retornar lista vacía cuando no hay familias activas")
    void getAllActiveFamilies_WhenNoActiveFamilies_ShouldReturnEmptyList() {
        // Given
        when(familyService.findAllActive()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/families/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(FamilyDTO.class)
                .hasSize(0);
    }

    // ========== TESTS PARA GET /api/v1/families/inactive (Con autenticación) ==========

    @Test
    @DisplayName("GET /inactive - Debe retornar todas las familias inactivas")
    void getAllInactiveFamilies_ShouldReturnInactiveFamilies() {
        // Given
        FamilyDTO inactiveFamily = createTestFamilyDTO();
        inactiveFamily.setStatus("I");
        when(familyService.findAllInactive()).thenReturn(Flux.just(inactiveFamily));

        // When & Then
        webTestClient
                .mutateWith(mockJwt()) // Agregar JWT mock
                .get()
                .uri("/api/v1/families/inactive")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(FamilyDTO.class)
                .hasSize(1)
                .contains(inactiveFamily);
    }

    // ========== TESTS PARA GET /api/v1/families/detail/{id} (Con autenticación) ==========

    @Test
    @DisplayName("GET /detail/{id} - Debe retornar detalles de familia existente")
    void getFamilyDetailById_ExistingFamily_ShouldReturnFamilyDetails() {
        // Given
        when(familyService.findDetailById(1)).thenReturn(Mono.just(testFamilyDTO));

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/api/v1/families/detail/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FamilyDTO.class)
                .isEqualTo(testFamilyDTO);
    }

    @Test
    @DisplayName("GET /detail/{id} - Debe retornar 404 para familia inexistente")
    void getFamilyDetailById_NonExistingFamily_ShouldReturn404() {
        // Given
        when(familyService.findDetailById(999)).thenReturn(Mono.empty());

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/api/v1/families/detail/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========== TESTS PARA GET /api/v1/families/{id} (Sin autenticación requerida) ==========

    @Test
    @DisplayName("GET /{id} - Debe retornar familia existente")
    void getFamilyById_ExistingFamily_ShouldReturnFamily() {
        // Given
        when(familyService.findById(1)).thenReturn(Mono.just(testFamilyDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/families/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FamilyDTO.class)
                .isEqualTo(testFamilyDTO);
    }

    @Test
    @DisplayName("GET /{id} - Debe retornar 404 para familia inexistente")
    void getFamilyById_NonExistingFamily_ShouldReturn404() {
        // Given
        when(familyService.findById(999)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/families/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========== TESTS PARA POST /api/v1/families (Con autenticación) ==========

    @Test
    @DisplayName("POST / - Debe crear familia exitosamente")
    void createFamily_ValidData_ShouldCreateFamily() {
        // Given
        FamilyDTO inputDTO = createTestFamilyDTO();
        inputDTO.setId(null);
        
        FamilyDTO createdDTO = createTestFamilyDTO();
        createdDTO.setId(1);
        
        when(familyService.createFamily(any(FamilyDTO.class))).thenReturn(Mono.just(createdDTO));

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .post()
                .uri("/api/v1/families")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inputDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FamilyDTO.class)
                .isEqualTo(createdDTO);
    }

    @Test
    @DisplayName("POST / - Debe manejar errores de creación")
    void createFamily_ServiceError_ShouldReturnBadRequest() {
        // Given
        FamilyDTO inputDTO = createTestFamilyDTO();
        when(familyService.createFamily(any(FamilyDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("Creation error")));

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .post()
                .uri("/api/v1/families")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inputDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ========== TESTS PARA PUT /api/v1/families/{id} (Con autenticación) ==========

    @Test
    @DisplayName("PUT /{id} - Debe actualizar familia existente")
    void updateFamily_ExistingFamily_ShouldUpdateFamily() {
        // Given
        FamilyDTO updatedDTO = createTestFamilyDTO();
        updatedDTO.setLastName("García Actualizado");
        
        when(familyService.updateFamily(eq(1), any(FamilyDTO.class))).thenReturn(Mono.just(updatedDTO));

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .put()
                .uri("/api/v1/families/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testFamilyDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FamilyDTO.class)
                .isEqualTo(updatedDTO);
    }

    // ========== TESTS PARA PUT /api/v1/families/delete/{id} (Con autenticación) ==========

    @Test
    @DisplayName("PUT /delete/{id} - Debe eliminar familia exitosamente")
    void deleteFamily_ExistingFamily_ShouldDeleteFamily() {
        // Given
        when(familyService.deleteFamily(1)).thenReturn(Mono.empty());

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .put()
                .uri("/api/v1/families/delete/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("PUT /delete/{id} - Debe retornar 404 para familia inexistente")
    void deleteFamily_NonExistingFamily_ShouldReturn404() {
        // Given
        when(familyService.deleteFamily(999))
                .thenReturn(Mono.error(new IllegalArgumentException("Family not found")));

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .put()
                .uri("/api/v1/families/delete/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .isEqualTo("Family not found");
    }

    // ========== TESTS PARA PUT /api/v1/families/active/{id} (Con autenticación) ==========

    @Test
    @DisplayName("PUT /active/{id} - Debe activar familia exitosamente")
    void activeFamily_ExistingFamily_ShouldActivateFamily() {
        // Given
        when(familyService.activeFamily(1)).thenReturn(Mono.empty());

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .put()
                .uri("/api/v1/families/active/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("PUT /active/{id} - Debe retornar 404 para familia inexistente")
    void activeFamily_NonExistingFamily_ShouldReturn404() {
        // Given
        when(familyService.activeFamily(999))
                .thenReturn(Mono.error(new IllegalArgumentException("Family not found")));

        // When & Then
        webTestClient
                .mutateWith(mockJwt())
                .put()
                .uri("/api/v1/families/active/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .isEqualTo("Family not found");
    }

    // ========== MÉTODOS AUXILIARES ==========

    private FamilyDTO createTestFamilyDTO() {
        FamilyDTO dto = new FamilyDTO();
        dto.setId(1);
        dto.setLastName("García");
        dto.setDirection("Av. Principal 123");
        dto.setNumberMembers(4);
        dto.setNumberChildren(2);
        dto.setStatus("A");
        dto.setCreated(LocalDateTime.now());
        
        BasicServiceDTO basicService = BasicServiceDTO.builder()
                .serviceId(100)
                .waterService("Si")
                .servLight("Si")
                .area("Urbana")
                .build();
        dto.setBasicService(basicService);
        
        HousingDetailsDTO housingDetails = HousingDetailsDTO.builder()
                .id(200)
                .typeOfHousing("Casa")
                .housingMaterial("Material Noble")
                .bedroomNumber(3)
                .build();
        dto.setHousingDetails(housingDetails);
        
        return dto;
    }

    private List<FamilyDTO> createTestFamilyList() {
        FamilyDTO family1 = createTestFamilyDTO();
        family1.setId(1);
        family1.setLastName("García");
        
        FamilyDTO family2 = createTestFamilyDTO();
        family2.setId(2);
        family2.setLastName("Rodríguez");
        family2.setDirection("Calle Secundaria 456");
        
        return Arrays.asList(family1, family2);
    }
}