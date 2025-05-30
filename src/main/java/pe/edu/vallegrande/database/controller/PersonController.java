package pe.edu.vallegrande.database.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.database.model.Person;
import pe.edu.vallegrande.database.service.PersonService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/person")
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Lista todas las personas activas
     */
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Person> listActive() {
        return personService.listActive();
    }

    /**
     * Lista todas las personas inactivas
     */
    @GetMapping(value = "/inactive", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Person> listInactive() {
        return personService.listInactive();
    }

    /**
     * Crea múltiples personas
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Flux<Person>>> createPersons(@RequestBody Flux<Person> persons) {
        return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                .body(personService.createPersons(persons)
                        .onErrorResume(error -> {
                            logError("Error al crear personas", error);
                            return Flux.empty();
                        })));
    }

    /**
     * Elimina lógicamente una persona
     */
    @PatchMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Person>> logicallyDelete(@PathVariable Integer id) {
        return personService.logicallyDelete(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Reactiva una persona
     */
    @PatchMapping(value = "/active/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Person>> reactivate(@PathVariable Integer id) {
        return personService.reactivate(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Lista personas por familia
     */
    @GetMapping(value = "/family/{familyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Person> listByFamily(@PathVariable Integer familyId) {
        return personService.listByFamily(familyId);
    }

    /**
     * Actualiza una persona
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Person>> updatePerson(@PathVariable Integer id, @RequestBody Person updatedPerson) {
        return personService.updatePerson(id, updatedPerson)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private void logError(String message, Throwable error) {
        logger.error("{}: {}", message, error.getMessage(), error);
    }
}