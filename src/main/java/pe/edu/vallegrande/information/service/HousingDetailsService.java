package pe.edu.vallegrande.information.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.information.model.HousingDetails;
import pe.edu.vallegrande.information.repository.HousingDetailsRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class HousingDetailsService {
    
    private final HousingDetailsRepository housingDetailsRepository;

    /**
     * Constructor que inyecta el repositorio de detalles de vivienda.
     * 
     * @param housingDetailsRepository El repositorio para acceder a los datos de HousingDetails.
     */
    @Autowired
    public HousingDetailsService(HousingDetailsRepository housingDetailsRepository) {
        this.housingDetailsRepository = housingDetailsRepository;
    }

    /**
     * Recupera todos los detalles de vivienda disponibles.
     * 
     * @return Un flujo de objetos HousingDetails.
     */
    public Flux<HousingDetails> findAll() {
        return housingDetailsRepository.findAll();
    }

    /**
     * Recupera los detalles de vivienda por su ID.
     * 
     * @param id El ID de los detalles de vivienda a recuperar.
     * @return Un objeto Mono que contiene el HousingDetails si se encuentra, o vacío si no.
     */
    public Mono<HousingDetails> findById(Integer id) {
        return housingDetailsRepository.findById(id);
    }

    /**
     * Guarda un nuevo detalle de vivienda en el repositorio.
     * 
     * @param housingDetails El detalle de vivienda a guardar.
     * @return Un objeto Mono que contiene el detalle de vivienda guardado.
     */
    public Mono<HousingDetails> save(HousingDetails housingDetails) {
        return housingDetailsRepository.save(housingDetails);
    }

    /**
     * Actualiza un detalle de vivienda existente.
     * 
     * @param id El ID del detalle de vivienda a actualizar.
     * @param housingDetails El objeto HousingDetails que contiene los nuevos datos.
     * @return Un objeto Mono que contiene el detalle de vivienda actualizado.
     */
    public Mono<HousingDetails> update(Integer id, HousingDetails housingDetails) {
        return housingDetailsRepository.findById(id)
                .flatMap(existingHousing -> {
                    updateFromDTO(existingHousing, housingDetails);
                    return housingDetailsRepository.save(existingHousing);
                });
    }

    /**
     * Elimina un detalle de vivienda por su ID.
     * 
     * @param id El ID del detalle de vivienda a eliminar.
     * @return Un objeto Mono vacío que indica la finalización de la operación.
     */
    public Mono<Void> delete(Integer id) {
        return housingDetailsRepository.deleteById(id);
    }

    /**
     * Actualiza un detalle de vivienda existente con los datos de un DTO.
     * 
     * @param housing El detalle de vivienda existente que se va a actualizar.
     * @param dto El objeto HousingDetails que contiene los nuevos datos.
     */
    private void updateFromDTO(HousingDetails housing, HousingDetails dto) {
        if (housing == null || dto == null) {
            return;
        }
        
        housing.setTypeOfHousing(dto.getTypeOfHousing());
        housing.setHousingMaterial(dto.getHousingMaterial());
        housing.setHousingSecurity(dto.getHousingSecurity());
        housing.setHomeEnvironment(dto.getHomeEnvironment());
        housing.setBedroomNumber(dto.getBedroomNumber());
        housing.setHabitability(dto.getHabitability());
        housing.setNumberRooms(dto.getNumberRooms());
        housing.setNumberOfBedrooms(dto.getNumberOfBedrooms());
        housing.setHabitabilityBuilding(dto.getHabitabilityBuilding());
    }
}
