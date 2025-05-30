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
import pe.edu.vallegrande.database.model.Person;
import pe.edu.vallegrande.database.security.TestSecurityConfig;
import pe.edu.vallegrande.database.service.PersonService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PersonController.class)
@Import(TestSecurityConfig.class)
@DisplayName("PersonController - Pruebas de Integración")
class PersonControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PersonService personService;

    private Person testPerson;
    private List<Person> testPersonList;

    @BeforeEach
    void setUp() {
        testPerson = createTestPerson();
        testPersonList = createTestPersonList();
    }

    // ========== TESTS PARA GET /api/v1/person/active ==========

    @Test
    @DisplayName("GET /active - Debe retornar todas las personas activas")
    void listActive_ShouldReturnActivePersons() {
        // Given
        when(personService.listActive()).thenReturn(Flux.fromIterable(testPersonList));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Person.class)
                .hasSize(2)
                .contains(testPersonList.get(0), testPersonList.get(1));
    }

    @Test
    @DisplayName("GET /active - Debe retornar lista vacía cuando no hay personas activas")
    void listActive_WhenNoActivePersons_ShouldReturnEmptyList() {
        // Given
        when(personService.listActive()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Person.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("GET /active - Debe manejar errores del servicio")
    void listActive_WhenServiceError_ShouldReturnError() {
        // Given
        when(personService.listActive()).thenReturn(Flux.error(new RuntimeException("Service error")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA GET /api/v1/person/inactive ==========

    @Test
    @DisplayName("GET /inactive - Debe retornar todas las personas inactivas")
    void listInactive_ShouldReturnInactivePersons() {
        // Given
        Person inactivePerson = createTestPerson();
        inactivePerson.setState("I");
        when(personService.listInactive()).thenReturn(Flux.just(inactivePerson));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/inactive")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Person.class)
                .hasSize(1)
                .contains(inactivePerson);
    }

    @Test
    @DisplayName("GET /inactive - Debe retornar lista vacía cuando no hay personas inactivas")
    void listInactive_WhenNoInactivePersons_ShouldReturnEmptyList() {
        // Given
        when(personService.listInactive()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/inactive")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(0);
    }

    // ========== TESTS PARA POST /api/v1/person ==========

    @Test
    @DisplayName("POST / - Debe crear múltiples personas exitosamente")
    void createPersons_ValidData_ShouldCreatePersons() {
        // Given
        List<Person> inputPersons = createTestPersonList();
        inputPersons.forEach(person -> person.setIdPerson(null)); // Para creación, los IDs deben ser null
        
        List<Person> createdPersons = createTestPersonList();
        
        when(personService.createPersons(any())).thenReturn(Flux.fromIterable(createdPersons));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/person")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Flux.fromIterable(inputPersons), Person.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Person.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("POST / - Debe manejar errores de creación")
    void createPersons_ServiceError_ShouldReturnEmptyFlux() {
        // Given
        List<Person> inputPersons = createTestPersonList();
        when(personService.createPersons(any()))
                .thenReturn(Flux.error(new RuntimeException("Creation error")));

        // When & Then - El controlador maneja el error y retorna Flux vacío
        webTestClient.post()
                .uri("/api/v1/person")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Flux.fromIterable(inputPersons), Person.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Person.class)
                .hasSize(0); // Flux vacío debido al manejo de errores
    }

    // ========== TESTS PARA PATCH /api/v1/person/delete/{id} ==========

    @Test
    @DisplayName("PATCH /delete/{id} - Debe eliminar lógicamente persona existente")
    void logicallyDelete_ExistingPerson_ShouldDeletePerson() {
        // Given
        Person deletedPerson = createTestPerson();
        deletedPerson.setState("I");
        when(personService.logicallyDelete(1)).thenReturn(Mono.just(deletedPerson));

        // When & Then
        webTestClient.patch()
                .uri("/api/v1/person/delete/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Person.class)
                .isEqualTo(deletedPerson);
    }

    @Test
    @DisplayName("PATCH /delete/{id} - Debe retornar 404 para persona inexistente")
    void logicallyDelete_NonExistingPerson_ShouldReturn404() {
        // Given
        when(personService.logicallyDelete(999)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.patch()
                .uri("/api/v1/person/delete/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PATCH /delete/{id} - Debe manejar errores del servicio")
    void logicallyDelete_ServiceError_ShouldReturnError() {
        // Given
        when(personService.logicallyDelete(1))
                .thenReturn(Mono.error(new RuntimeException("Delete error")));

        // When & Then
        webTestClient.patch()
                .uri("/api/v1/person/delete/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA PATCH /api/v1/person/active/{id} ==========

    @Test
    @DisplayName("PATCH /active/{id} - Debe reactivar persona existente")
    void reactivate_ExistingPerson_ShouldReactivatePerson() {
        // Given
        Person reactivatedPerson = createTestPerson();
        reactivatedPerson.setState("A");
        when(personService.reactivate(1)).thenReturn(Mono.just(reactivatedPerson));

        // When & Then
        webTestClient.patch()
                .uri("/api/v1/person/active/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Person.class)
                .isEqualTo(reactivatedPerson);
    }

    @Test
    @DisplayName("PATCH /active/{id} - Debe retornar 404 para persona inexistente")
    void reactivate_NonExistingPerson_ShouldReturn404() {
        // Given
        when(personService.reactivate(999)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.patch()
                .uri("/api/v1/person/active/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PATCH /active/{id} - Debe manejar errores del servicio")
    void reactivate_ServiceError_ShouldReturnError() {
        // Given
        when(personService.reactivate(1))
                .thenReturn(Mono.error(new RuntimeException("Reactivate error")));

        // When & Then
        webTestClient.patch()
                .uri("/api/v1/person/active/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA GET /api/v1/person/family/{familyId} ==========

    @Test
    @DisplayName("GET /family/{familyId} - Debe retornar personas de una familia")
    void listByFamily_ExistingFamily_ShouldReturnPersonsFromFamily() {
        // Given
        when(personService.listByFamily(1)).thenReturn(Flux.fromIterable(testPersonList));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/family/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Person.class)
                .hasSize(2)
                .contains(testPersonList.get(0), testPersonList.get(1));
    }

    @Test
    @DisplayName("GET /family/{familyId} - Debe retornar lista vacía para familia sin personas")
    void listByFamily_FamilyWithoutPersons_ShouldReturnEmptyList() {
        // Given
        when(personService.listByFamily(999)).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/family/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("GET /family/{familyId} - Debe manejar errores del servicio")
    void listByFamily_ServiceError_ShouldReturnError() {
        // Given
        when(personService.listByFamily(1))
                .thenReturn(Flux.error(new RuntimeException("Service error")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/person/family/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS PARA PUT /api/v1/person/{id} ==========

    @Test
    @DisplayName("PUT /{id} - Debe actualizar persona existente")
    void updatePerson_ExistingPerson_ShouldUpdatePerson() {
        // Given
        Person updatedPerson = createTestPerson();
        updatedPerson.setName("Juan Carlos");
        
        when(personService.updatePerson(eq(1), any(Person.class))).thenReturn(Mono.just(updatedPerson));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/person/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testPerson)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Person.class)
                .isEqualTo(updatedPerson);
    }

    @Test
    @DisplayName("PUT /{id} - Debe retornar 404 para persona inexistente")
    void updatePerson_NonExistingPerson_ShouldReturn404() {
        // Given
        when(personService.updatePerson(eq(999), any(Person.class))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.put()
                .uri("/api/v1/person/999")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testPerson)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /{id} - Debe manejar errores de actualización")
    void updatePerson_ServiceError_ShouldReturnError() {
        // Given
        when(personService.updatePerson(eq(1), any(Person.class)))
                .thenReturn(Mono.error(new RuntimeException("Update error")));

        // When & Then
        webTestClient.put()
                .uri("/api/v1/person/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testPerson)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ========== TESTS DE VALIDACIÓN DE CONTENT-TYPE ==========

    @Test
    @DisplayName("POST / - Debe rechazar content-type incorrecto")
    void createPersons_WrongContentType_ShouldReturnUnsupportedMediaType() {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/person")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("plain text")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }

    @Test
    @DisplayName("PUT /{id} - Debe rechazar content-type incorrecto")
    void updatePerson_WrongContentType_ShouldReturnUnsupportedMediaType() {
        // When & Then
        webTestClient.put()
                .uri("/api/v1/person/1")
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("plain text")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }

    // ========== MÉTODOS AUXILIARES ==========

    private Person createTestPerson() {
        Person person = new Person();
        person.setIdPerson(1);
        person.setName("Juan");
        person.setSurname("Pérez");
        person.setAge(33);
        person.setBirthdate(LocalDate.of(1990, 5, 15));
        person.setTypeDocument("DNI");
        person.setDocumentNumber("12345678");
        person.setTypeKinship("Jefe de familia");
        person.setSponsored("No");
        person.setState("A");
        person.setFamilyIdFamily(1);
        return person;
    }

    private List<Person> createTestPersonList() {
        Person person1 = createTestPerson();
        person1.setIdPerson(1);
        person1.setName("Juan");
        person1.setTypeKinship("Jefe de familia");
        
        Person person2 = createTestPerson();
        person2.setIdPerson(2);
        person2.setName("María");
        person2.setSurname("García");
        person2.setAge(30);
        person2.setDocumentNumber("87654321");
        person2.setTypeKinship("Esposa");
        person2.setBirthdate(LocalDate.of(1993, 8, 20));
        
        return Arrays.asList(person1, person2);
    }
}