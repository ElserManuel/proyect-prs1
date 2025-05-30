package pe.edu.vallegrande.information.controller;

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
import pe.edu.vallegrande.information.model.HousingDetails;
import pe.edu.vallegrande.information.security.TestSecurityConfig;
import pe.edu.vallegrande.information.service.HousingDetailsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(HousingDetailsController.class)
@Import(TestSecurityConfig.class)
@DisplayName("HousingDetailsController - Pruebas de Integración")
class HousingDetailsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private HousingDetailsService housingDetailsService;

    private HousingDetails testHousingDetails;
    private List<HousingDetails> testHousingDetailsList;

    @BeforeEach
    void setUp() {
        testHousingDetails = createTestHousingDetails();
        testHousingDetailsList = createTestHousingDetailsList();
    }

    // ========== TESTS PARA GET /api/v1/housing ==========

    @Test
    @DisplayName("GET / - Debe retornar todos los detalles de vivienda")
    void getAllHousingDetails_ShouldReturnAllHousingDetails() {
        // Given
        when(housingDetailsService.findAll()).thenReturn(Flux.fromIterable(testHousingDetailsList));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/housing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(HousingDetails.class)
                .hasSize(2)
                .contains(testHousingDetailsList.get(0), testHousingDetailsList.get(1));
    }

    @Test
    @DisplayName("GET / - Debe retornar lista vacía cuando no hay detalles de vivienda")
    void getAllHousingDetails_WhenNoHousingDetails_ShouldReturnEmptyList() {
        // Given
        when(housingDetailsService.findAll()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/housing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(HousingDetails.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("GET / - Debe manejar errores del servicio")
    void getAllHousingDetails_WhenServiceError_ShouldReturnError() {
        // Given
        when(housingDetailsService.findAll()).thenReturn(Flux.error(new RuntimeException("Service error")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/housing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA GET /api/v1/housing/{id} ==========

    @Test
    @DisplayName("GET /{id} - Debe retornar detalles de vivienda existente")
    void getHousingDetailsById_ExistingHousingDetails_ShouldReturnHousingDetails() {
        // Given
        when(housingDetailsService.findById(1)).thenReturn(Mono.just(testHousingDetails));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/housing/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HousingDetails.class)
                .isEqualTo(testHousingDetails);
    }

    @Test
    @DisplayName("GET /{id} - Debe retornar 404 para detalles de vivienda inexistente")
    void getHousingDetailsById_NonExistingHousingDetails_ShouldReturn404() {
        // Given
        when(housingDetailsService.findById(999)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/housing/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========== TESTS PARA POST /api/v1/housing ==========

    @Test
    @DisplayName("POST / - Debe crear detalles de vivienda exitosamente")
    void createHousingDetails_ValidData_ShouldCreateHousingDetails() {
        // Given
        HousingDetails inputHousingDetails = createTestHousingDetails();
        inputHousingDetails.setId(null); // Para creación, el ID debe ser null
        
        HousingDetails createdHousingDetails = createTestHousingDetails();
        createdHousingDetails.setId(1);
        
        when(housingDetailsService.save(any(HousingDetails.class))).thenReturn(Mono.just(createdHousingDetails));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/housing")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inputHousingDetails)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HousingDetails.class)
                .isEqualTo(createdHousingDetails);
    }

    @Test
    @DisplayName("POST / - Debe manejar errores de creación")
    void createHousingDetails_ServiceError_ShouldReturnError() {
        // Given
        HousingDetails inputHousingDetails = createTestHousingDetails();
        when(housingDetailsService.save(any(HousingDetails.class)))
                .thenReturn(Mono.error(new RuntimeException("Creation error")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/housing")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inputHousingDetails)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST / - Debe rechazar JSON inválido")
    void createHousingDetails_InvalidJson_ShouldReturnBadRequest() {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/housing")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("{invalid json}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ========== TESTS PARA PUT /api/v1/housing/{id} ==========

    @Test
    @DisplayName("PUT /{id} - Debe actualizar detalles de vivienda existente")
    void updateHousingDetails_ExistingHousingDetails_ShouldUpdateHousingDetails() {
        // Given
        HousingDetails updatedHousingDetails = createTestHousingDetails();
        updatedHousingDetails.setTypeOfHousing("Departamento");
        
        when(housingDetailsService.update(eq(1), any(HousingDetails.class))).thenReturn(Mono.just(updatedHousingDetails));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/housing/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testHousingDetails)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HousingDetails.class)
                .isEqualTo(updatedHousingDetails);
    }

    @Test
    @DisplayName("PUT /{id} - Debe retornar 404 para detalles de vivienda inexistente")
    void updateHousingDetails_NonExistingHousingDetails_ShouldReturn404() {
        // Given
        when(housingDetailsService.update(eq(999), any(HousingDetails.class))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.put()
                .uri("/api/v1/housing/999")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testHousingDetails)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /{id} - Debe manejar errores de actualización")
    void updateHousingDetails_ServiceError_ShouldReturnError() {
        // Given
        when(housingDetailsService.update(eq(1), any(HousingDetails.class)))
                .thenReturn(Mono.error(new RuntimeException("Update error")));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/housing/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testHousingDetails)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA DELETE /api/v1/housing/{id} ==========

    @Test
    @DisplayName("DELETE /{id} - Debe eliminar detalles de vivienda exitosamente")
    void deleteHousingDetails_ExistingHousingDetails_ShouldDeleteHousingDetails() {
        // Given
        when(housingDetailsService.delete(1)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/housing/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /{id} - Debe manejar errores de eliminación")
    void deleteHousingDetails_ServiceError_ShouldReturnError() {
        // Given
        when(housingDetailsService.delete(1))
                .thenReturn(Mono.error(new RuntimeException("Delete error")));

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/housing/1")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS DE VALIDACIÓN DE CONTENT-TYPE ==========

    @Test
    @DisplayName("POST / - Debe rechazar content-type incorrecto")
    void createHousingDetails_WrongContentType_ShouldReturnUnsupportedMediaType() {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/housing")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("plain text")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }

    @Test
    @DisplayName("PUT /{id} - Debe rechazar content-type incorrecto")
    void updateHousingDetails_WrongContentType_ShouldReturnUnsupportedMediaType() {
        // When & Then
        webTestClient.put()
                .uri("/api/v1/housing/1")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("plain text")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }

    // ========== MÉTODOS AUXILIARES ==========

    private HousingDetails createTestHousingDetails() {
        return HousingDetails.builder()
            .id(1)
            .typeOfHousing("Casa")
            .housingMaterial("Material Noble")
            .housingSecurity("Rejas y portón")
            .homeEnvironment(3)
            .bedroomNumber(3)
            .habitability("Buena")
            .numberRooms(5)
            .numberOfBedrooms(3)
            .habitabilityBuilding("Excelente")
            .build();
    }

    private List<HousingDetails> createTestHousingDetailsList() {
        HousingDetails housing1 = createTestHousingDetails();
        housing1.setId(1);
        housing1.setTypeOfHousing("Casa");
        
        HousingDetails housing2 = createTestHousingDetails();
        housing2.setId(2);
        housing2.setTypeOfHousing("Departamento");
        housing2.setBedroomNumber(2);
        
        return Arrays.asList(housing1, housing2);
    }
}