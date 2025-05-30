package pe.edu.vallegrande.information.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.vallegrande.information.model.HousingDetails;
import pe.edu.vallegrande.information.service.HousingDetailsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/housing")
public class HousingDetailsController {
    
    private final HousingDetailsService housingDetailsService;

    /**
     * Constructor que inyecta el servicio de detalles de vivienda.
     * 
     * @param housingDetailsService El servicio para acceder a la lógica de negocio de HousingDetails.
     */
    @Autowired
    public HousingDetailsController(HousingDetailsService housingDetailsService) {
        this.housingDetailsService = housingDetailsService;
    }

    /**
     * Recupera todos los detalles de vivienda disponibles.
     * 
     * @return Un flujo de objetos HousingDetails en formato JSON.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<HousingDetails> getAllHousingDetails() {
        return housingDetailsService.findAll();
    }

    /**
     * Recupera los detalles de vivienda por su ID.
     * 
     * @param id El ID de los detalles de vivienda a recuperar.
     * @return Un objeto Mono que contiene una respuesta HTTP con los detalles de vivienda si se encuentran, o un estado 404 si no.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HousingDetails>> getHousingDetailsById(@PathVariable Integer id) {
        return housingDetailsService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo detalle de vivienda.
     * 
     * @param housingDetails El objeto HousingDetails a crear.
     * @return Un objeto Mono que contiene una respuesta HTTP con el detalle de vivienda creado y estado 201 (CREATED).
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HousingDetails>> createHousingDetails(@RequestBody HousingDetails housingDetails) {
        return housingDetailsService.save(housingDetails)
                .map(savedHousing -> ResponseEntity.status(HttpStatus.CREATED).body(savedHousing));
    }

    /**
     * Actualiza un detalle de vivienda existente.
     * 
     * @param id El ID del detalle de vivienda a actualizar.
     * @param housingDetails El objeto HousingDetails que contiene los nuevos datos.
     * @return Un objeto Mono que contiene una respuesta HTTP con el detalle de vivienda actualizado si se encuentra, o un estado 404 si no.
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HousingDetails>> updateHousingDetails(@PathVariable Integer id, @RequestBody HousingDetails housingDetails) {
        return housingDetailsService.update(id, housingDetails)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un detalle de vivienda por su ID.
     * 
     * @param id El ID del detalle de vivienda a eliminar.
     * @return Un objeto Mono que contiene una respuesta HTTP con estado 204 (NO CONTENT) si se elimina correctamente, o un estado 404 si no se encuentra.
     */
    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity<Void>> deleteHousingDetails(@PathVariable Integer id) {
        return housingDetailsService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
