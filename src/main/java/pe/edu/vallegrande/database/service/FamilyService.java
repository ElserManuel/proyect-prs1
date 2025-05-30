package pe.edu.vallegrande.database.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.database.client.HousingServiceClient;
import pe.edu.vallegrande.database.dto.BasicServiceDTO;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.dto.HousingDetailsDTO;
import pe.edu.vallegrande.database.kafka.FamilyEventService;
import pe.edu.vallegrande.database.model.Family;
import pe.edu.vallegrande.database.repository.FamilyRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class FamilyService {

    private static final Logger logger = LoggerFactory.getLogger(FamilyService.class);
    private final FamilyRepository familyRepository;
    private final FamilyEventService familyEventService;
    private final FamilyMapper familyMapper;
    private final HousingServiceClient housingServiceClient;

    @Autowired
    public FamilyService(
            FamilyRepository familyRepository,
            FamilyEventService familyEventService,
            FamilyMapper familyMapper,
            HousingServiceClient housingServiceClient) {
        this.familyRepository = familyRepository;
        this.familyEventService = familyEventService;
        this.familyMapper = familyMapper;
        this.housingServiceClient = housingServiceClient;
    }

    /**
     * Mapea una entidad Family a un FamilyDTO incluyendo sus servicios básicos y detalles de vivienda
     * obtenidos del otro microservicio
     */
    public Mono<FamilyDTO> mapToFamilyDTO(Family family) {
        FamilyDTO dto = familyMapper.toDTO(family);

        // Obtener servicios básicos del otro microservicio si existen
        Mono<FamilyDTO> withBasicService = family.getServiceId() != null
                ? housingServiceClient.getBasicServiceById(family.getServiceId())
                    .map(basicService -> {
                        dto.setBasicService(basicService);
                        return dto;
                    })
                    .defaultIfEmpty(dto)
                : Mono.just(dto);

        // Obtener detalles de vivienda del otro microservicio si existen
        return withBasicService.flatMap(dtoWithService -> 
            family.getHousingId() != null
                ? housingServiceClient.getHousingDetailsById(family.getHousingId())
                    .map(housingDetails -> {
                        dtoWithService.setHousingDetails(housingDetails);
                        return dtoWithService;
                    })
                    .defaultIfEmpty(dtoWithService)
                : Mono.just(dtoWithService)
        );
    }

    /**
     * Obtiene listado de familias activas
     */
    public Flux<FamilyDTO> findAllActive() {
        return familyRepository.findAllByStatus("A")
                .sort((f1, f2) -> f1.getId().compareTo(f2.getId()))
                .flatMap(this::mapToFamilyDTO);
    }

    /**
     * Obtiene listado de familias inactivas
     */
    public Flux<FamilyDTO> findAllInactive() {
        return familyRepository.findAllByStatus("I")
                .sort((f1, f2) -> f1.getId().compareTo(f2.getId()))
                .flatMap(this::mapToFamilyDTO);
    }

    /**
     * Obtiene una familia por ID
     */
    public Mono<FamilyDTO> findById(Integer id) {
        return familyRepository.findById(id)
                .flatMap(this::mapToFamilyDTO);
    }

    /**
     * Crea una nueva familia con sus servicios y detalles de vivienda asociados
     * en el otro microservicio
     */
    public Mono<FamilyDTO> createFamily(FamilyDTO familyDTO) {
        Mono<BasicServiceDTO> basicServiceMono = createBasicServiceInOtherService(familyDTO);
        Mono<HousingDetailsDTO> housingDetailsMono = createHousingDetailsInOtherService(familyDTO);

        return Mono.zip(basicServiceMono, housingDetailsMono)
                .flatMap(tuple -> {
                    BasicServiceDTO savedBasicService = tuple.getT1();
                    HousingDetailsDTO savedHousingDetails = tuple.getT2();

                    Family family = familyMapper.toEntity(familyDTO);
                    family.setStatus("A"); // Active by default
                    family.setCreated(LocalDateTime.now());
                    
                    if (savedBasicService != null && savedBasicService.getServiceId() != null) {
                        family.setServiceId(savedBasicService.getServiceId());
                    }
                    
                    if (savedHousingDetails != null && savedHousingDetails.getId() != null) {
                        family.setHousingId(savedHousingDetails.getId());
                    }

                    return familyRepository.save(family)
                            .doOnSuccess(savedFamily -> familyEventService.publishFamilyEvent(savedFamily, "CREATED"))
                            .flatMap(this::mapToFamilyDTO);
                })
                .onErrorResume(e -> {
                    logger.error("Error creating family", e);
                    return Mono.error(new RuntimeException("Error durante la creación de la familia: " + e.getMessage()));
                });
    }

    /**
     * Actualiza una familia existente y sus servicios y detalles de vivienda
     * en el otro microservicio
     */
    public Mono<FamilyDTO> updateFamily(Integer id, FamilyDTO familyDTO) {
        return familyRepository.findById(id)
                .flatMap(existingFamily -> {
                    familyMapper.updateEntityFromDTO(existingFamily, familyDTO);
                    
                    Mono<Void> updateBasicServiceMono = updateBasicServiceInOtherService(existingFamily, familyDTO)
                            .then();
                    
                    Mono<Void> updateHousingDetailsMono = updateHousingDetailsInOtherService(existingFamily, familyDTO)
                            .then();
                    
                    return Mono.when(updateBasicServiceMono, updateHousingDetailsMono)
                            .then(familyRepository.save(existingFamily))
                            .doOnSuccess(savedFamily -> familyEventService.publishFamilyEvent(savedFamily, "UPDATED"));
                })
                .flatMap(this::mapToFamilyDTO)
                .onErrorResume(e -> {
                    logger.error("Error updating family", e);
                    return Mono.error(new RuntimeException("Error durante la actualización de la familia: " + e.getMessage()));
                });
    }

    /**
     * Desactiva lógicamente una familia
     */
    public Mono<Void> deleteFamily(Integer id) {
        return changeStatus(id, "I", "DELETED");
    }

    /**
     * Activa lógicamente una familia
     */
    public Mono<Void> activeFamily(Integer id) {
        return changeStatus(id, "A", "UPDATED");
    }

    /**
     * Método común para cambiar el estado de una familia
     */
    private Mono<Void> changeStatus(Integer id, String status, String eventType) {
        return familyRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Familia no encontrada con ID: " + id)))
                .flatMap(family -> {
                    family.setStatus(status);
                    return familyRepository.save(family)
                            .doOnSuccess(savedFamily -> familyEventService.publishFamilyEvent(savedFamily, eventType))
                            .then();
                });
    }

    /**
     * Obtiene detalles de una familia (idéntico a findById en este caso)
     */
    public Mono<FamilyDTO> findDetailById(Integer id) {
        return findById(id);
    }

    // Métodos privados auxiliares para comunicación con el otro microservicio

    private Mono<BasicServiceDTO> createBasicServiceInOtherService(FamilyDTO familyDTO) {
        if (familyDTO.getBasicService() != null) {
            return housingServiceClient.createBasicService(familyDTO.getBasicService());
        } else {
            return Mono.empty();
        }
    }

    private Mono<HousingDetailsDTO> createHousingDetailsInOtherService(FamilyDTO familyDTO) {
        if (familyDTO.getHousingDetails() != null) {
            return housingServiceClient.createHousingDetails(familyDTO.getHousingDetails());
        } else {
            return Mono.empty();
        }
    }

    private Mono<BasicServiceDTO> updateBasicServiceInOtherService(Family family, FamilyDTO familyDTO) {
        if (family.getServiceId() != null && familyDTO.getBasicService() != null) {
            return housingServiceClient.updateBasicService(family.getServiceId(), familyDTO.getBasicService());
        }
        return Mono.empty();
    }

    private Mono<HousingDetailsDTO> updateHousingDetailsInOtherService(Family family, FamilyDTO familyDTO) {
        if (family.getHousingId() != null && familyDTO.getHousingDetails() != null) {
            return housingServiceClient.updateHousingDetails(family.getHousingId(), familyDTO.getHousingDetails());
        } else if (familyDTO.getHousingDetails() != null) {
            // Si la familia no tiene un housingId pero se proporciona HousingDetails, crea uno nuevo
            return housingServiceClient.createHousingDetails(familyDTO.getHousingDetails())
                    .doOnSuccess(savedHousing -> {
                        if (savedHousing != null && savedHousing.getId() != null) {
                            family.setHousingId(savedHousing.getId());
                        }
                    });
        }
        return Mono.empty();
    }
}
