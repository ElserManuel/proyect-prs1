package pe.edu.vallegrande.information.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.information.model.BasicService;
import pe.edu.vallegrande.information.repository.BasicServiceRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BasicServiceService {
    
    private final BasicServiceRepository basicServiceRepository;

    /**
     * Constructor que inyecta el repositorio de servicios básicos.
     * 
     * @param basicServiceRepository El repositorio para acceder a los datos de BasicService.
     */
    @Autowired
    public BasicServiceService(BasicServiceRepository basicServiceRepository) {
        this.basicServiceRepository = basicServiceRepository;
    }

    /**
     * Recupera todos los servicios básicos disponibles.
     * 
     * @return Un flujo de objetos BasicService.
     */
    public Flux<BasicService> findAll() {
        return basicServiceRepository.findAll();
    }

    /**
     * Recupera un servicio básico por su ID.
     * 
     * @param id El ID del servicio básico a recuperar.
     * @return Un objeto Mono que contiene el BasicService si se encuentra, o vacío si no.
     */
    public Mono<BasicService> findById(Integer id) {
        return basicServiceRepository.findById(id);
    }

    /**
     * Guarda un nuevo servicio básico en el repositorio.
     * 
     * @param basicService El servicio básico a guardar.
     * @return Un objeto Mono que contiene el servicio básico guardado.
     */
    public Mono<BasicService> save(BasicService basicService) {
        return basicServiceRepository.save(basicService);
    }

    /**
     * Actualiza un servicio básico existente.
     * 
     * @param id El ID del servicio básico a actualizar.
     * @param basicService El objeto BasicService que contiene los nuevos datos.
     * @return Un objeto Mono que contiene el servicio básico actualizado.
     */
    public Mono<BasicService> update(Integer id, BasicService basicService) {
        return basicServiceRepository.findById(id)
                .flatMap(existingService -> {
                    updateFromDTO(existingService, basicService);
                    return basicServiceRepository.save(existingService);
                });
    }

    /**
     * Elimina un servicio básico por su ID.
     * 
     * @param id El ID del servicio básico a eliminar.
     * @return Un objeto Mono vacío que indica la finalización de la operación.
     */
    public Mono<Void> delete(Integer id) {
        return basicServiceRepository.deleteById(id);
    }

    /**
     * Actualiza un servicio básico existente con los datos de un DTO.
     * 
     * @param service El servicio básico existente que se va a actualizar.
     * @param dto El objeto BasicService que contiene los nuevos datos.
     */
    private void updateFromDTO(BasicService service, BasicService dto) {
        if (service == null || dto == null) {
            return;
        }
        
        service.setWaterService(dto.getWaterService());
        service.setServDrain(dto.getServDrain());
        service.setServLight(dto.getServLight());
        service.setServCable(dto.getServCable());
        service.setServGas(dto.getServGas());
        service.setArea(dto.getArea());
        service.setReferenceLocation(dto.getReferenceLocation());
        service.setResidue(dto.getResidue());
        service.setPublicLighting(dto.getPublicLighting());
        service.setSecurity(dto.getSecurity());
        service.setMaterial(dto.getMaterial());
        service.setFeeding(dto.getFeeding());
        service.setEconomic(dto.getEconomic());
        service.setSpiritual(dto.getSpiritual());
        service.setSocialCompany(dto.getSocialCompany());
        service.setGuideTip(dto.getGuideTip());
    }
}
