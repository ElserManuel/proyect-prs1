package pe.edu.vallegrande.information.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.vallegrande.information.model.BasicService;
import pe.edu.vallegrande.information.service.BasicServiceService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/services")
public class BasicServiceController {
    
    private final BasicServiceService basicServiceService;

    /**
     * Constructor que inyecta el servicio de servicios básicos.
     * 
     * @param basicServiceService El servicio para acceder a la lógica de negocio de BasicService.
     */
    @Autowired
    public BasicServiceController(BasicServiceService basicServiceService) {
        this.basicServiceService = basicServiceService;
    }

    /**
     * Recupera todos los servicios básicos disponibles.
     * 
     * @return Un flujo de objetos BasicService en formato JSON.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BasicService> getAllServices() {
        return basicServiceService.findAll();
    }

    /**
     * Recupera un servicio básico por su ID.
     * 
     * @param id El ID del servicio básico a recuperar.
     * @return Un objeto Mono que contiene una respuesta HTTP con el servicio básico si se encuentra, o un estado 404 si no.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BasicService>> getServiceById(@PathVariable Integer id) {
        return basicServiceService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo servicio básico.
     * 
     * @param basicService El objeto BasicService a crear.
     * @return Un objeto Mono que contiene una respuesta HTTP con el servicio básico creado y estado 201 (CREATED).
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BasicService>> createService(@RequestBody BasicService basicService) {
        return basicServiceService.save(basicService)
                .map(savedService -> ResponseEntity.status(HttpStatus.CREATED).body(savedService));
    }

    /**
     * Actualiza un servicio básico existente.
     * 
     * @param id El ID del servicio básico a actualizar.
     * @param basicService El objeto BasicService que contiene los nuevos datos.
     * @return Un objeto Mono que contiene una respuesta HTTP con el servicio básico actualizado si se encuentra, o un estado 404 si no.
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BasicService>> updateService(@PathVariable Integer id, @RequestBody BasicService basicService) {
        return basicServiceService.update(id, basicService)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un servicio básico por su ID.
     * 
     * @param id El ID del servicio básico a eliminar.
     * @return Un objeto Mono que contiene una respuesta HTTP con estado 204 (NO CONTENT) si se elimina correctamente, o un estado 404 si no se encuentra.
     */
    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity<Void>> deleteService(@PathVariable Integer id) {
        return basicServiceService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
