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
import pe.edu.vallegrande.information.model.BasicService;
import pe.edu.vallegrande.information.security.TestSecurityConfig;
import pe.edu.vallegrande.information.service.BasicServiceService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(BasicServiceController.class)
@Import(TestSecurityConfig.class)
@DisplayName("BasicServiceController - Pruebas de Integración")
class BasicServiceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BasicServiceService basicServiceService;

    private BasicService testBasicService;
    private List<BasicService> testBasicServiceList;

    @BeforeEach
    void setUp() {
        testBasicService = createTestBasicService();
        testBasicServiceList = createTestBasicServiceList();
    }

    // ========== TESTS PARA GET /api/v1/services ==========

    @Test
    @DisplayName("GET / - Debe retornar todos los servicios básicos")
    void getAllServices_ShouldReturnAllServices() {
        // Given
        when(basicServiceService.findAll()).thenReturn(Flux.fromIterable(testBasicServiceList));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BasicService.class)
                .hasSize(2)
                .contains(testBasicServiceList.get(0), testBasicServiceList.get(1));
    }

    @Test
    @DisplayName("GET / - Debe retornar lista vacía cuando no hay servicios")
    void getAllServices_WhenNoServices_ShouldReturnEmptyList() {
        // Given
        when(basicServiceService.findAll()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BasicService.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("GET / - Debe manejar errores del servicio")
    void getAllServices_WhenServiceError_ShouldReturnError() {
        // Given
        when(basicServiceService.findAll()).thenReturn(Flux.error(new RuntimeException("Service error")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA GET /api/v1/services/{id} ==========

    @Test
    @DisplayName("GET /{id} - Debe retornar servicio existente")
    void getServiceById_ExistingService_ShouldReturnService() {
        // Given
        when(basicServiceService.findById(1)).thenReturn(Mono.just(testBasicService));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BasicService.class)
                .isEqualTo(testBasicService);
    }

    @Test
    @DisplayName("GET /{id} - Debe retornar 404 para servicio inexistente")
    void getServiceById_NonExistingService_ShouldReturn404() {
        // Given
        when(basicServiceService.findById(999)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========== TESTS PARA POST /api/v1/services ==========

    @Test
    @DisplayName("POST / - Debe crear servicio exitosamente")
    void createService_ValidData_ShouldCreateService() {
        // Given
        BasicService inputService = createTestBasicService();
        inputService.setServiceId(null); // Para creación, el ID debe ser null
        
        BasicService createdService = createTestBasicService();
        createdService.setServiceId(1);
        
        when(basicServiceService.save(any(BasicService.class))).thenReturn(Mono.just(createdService));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inputService)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BasicService.class)
                .isEqualTo(createdService);
    }

    @Test
    @DisplayName("POST / - Debe manejar errores de creación")
    void createService_ServiceError_ShouldReturnError() {
        // Given
        BasicService inputService = createTestBasicService();
        when(basicServiceService.save(any(BasicService.class)))
                .thenReturn(Mono.error(new RuntimeException("Creation error")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inputService)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST / - Debe rechazar JSON inválido")
    void createService_InvalidJson_ShouldReturnBadRequest() {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("{invalid json}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ========== TESTS PARA PUT /api/v1/services/{id} ==========

    @Test
    @DisplayName("PUT /{id} - Debe actualizar servicio existente")
    void updateService_ExistingService_ShouldUpdateService() {
        // Given
        BasicService updatedService = createTestBasicService();
        updatedService.setWaterService("No");
        
        when(basicServiceService.update(eq(1), any(BasicService.class))).thenReturn(Mono.just(updatedService));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testBasicService)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BasicService.class)
                .isEqualTo(updatedService);
    }

    @Test
    @DisplayName("PUT /{id} - Debe retornar 404 para servicio inexistente")
    void updateService_NonExistingService_ShouldReturn404() {
        // Given
        when(basicServiceService.update(eq(999), any(BasicService.class))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.put()
                .uri("/api/v1/services/999")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testBasicService)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /{id} - Debe manejar errores de actualización")
    void updateService_ServiceError_ShouldReturnError() {
        // Given
        when(basicServiceService.update(eq(1), any(BasicService.class)))
                .thenReturn(Mono.error(new RuntimeException("Update error")));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testBasicService)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA DELETE /api/v1/services/{id} ==========

    @Test
    @DisplayName("DELETE /{id} - Debe eliminar servicio exitosamente")
    void deleteService_ExistingService_ShouldDeleteService() {
        // Given
        when(basicServiceService.delete(1)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/services/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /{id} - Debe manejar errores de eliminación")
    void deleteService_ServiceError_ShouldReturnError() {
        // Given
        when(basicServiceService.delete(1))
                .thenReturn(Mono.error(new RuntimeException("Delete error")));

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/services/1")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS DE VALIDACIÓN DE CONTENT-TYPE ==========

    @Test
    @DisplayName("POST / - Debe rechazar content-type incorrecto")
    void createService_WrongContentType_ShouldReturnUnsupportedMediaType() {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("plain text")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }

    @Test
    @DisplayName("PUT /{id} - Debe rechazar content-type incorrecto")
    void updateService_WrongContentType_ShouldReturnUnsupportedMediaType() {
        // When & Then
        webTestClient.put()
                .uri("/api/v1/services/1")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("plain text")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }

    // ========== MÉTODOS AUXILIARES ==========

    private BasicService createTestBasicService() {
        return BasicService.builder()
            .serviceId(1)
            .waterService("Si")
            .servDrain("Si")
            .servLight("Si")
            .servCable("No")
            .servGas("Si")
            .area("Urbana")
            .referenceLocation("Cerca del parque central")
            .residue("Recolección municipal")
            .publicLighting("Si")
            .security("Serenazgo")
            .material("Concreto")
            .feeding("Mercado local")
            .economic("Comercio")
            .spiritual("Iglesia católica")
            .socialCompany("Centro comunal")
            .guideTip("Acceso por la avenida principal")
            .build();
    }

    private List<BasicService> createTestBasicServiceList() {
        BasicService service1 = createTestBasicService();
        service1.setServiceId(1);
        service1.setArea("Urbana");
        
        BasicService service2 = createTestBasicService();
        service2.setServiceId(2);
        service2.setArea("Rural");
        service2.setWaterService("No");
        
        return Arrays.asList(service1, service2);
    }
}