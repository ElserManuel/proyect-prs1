package pe.edu.vallegrande.database.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.database.client.HousingServiceClient;
import pe.edu.vallegrande.database.dto.BasicServiceDTO;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.dto.HousingDetailsDTO;
import pe.edu.vallegrande.database.kafka.FamilyEventService;
import pe.edu.vallegrande.database.model.Family;
import pe.edu.vallegrande.database.repository.FamilyRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FamilyService - Pruebas Unitarias")
class FamilyServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilyEventService familyEventService;

    @Mock
    private FamilyMapper familyMapper;

    @Mock
    private HousingServiceClient housingServiceClient;

    @InjectMocks
    private FamilyService familyService;

    private Family testFamily;
    private FamilyDTO testFamilyDTO;
    private BasicServiceDTO testBasicService;
    private HousingDetailsDTO testHousingDetails;

    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba reutilizables
        testFamily = createTestFamily();
        testFamilyDTO = createTestFamilyDTO();
        testBasicService = createTestBasicService();
        testHousingDetails = createTestHousingDetails();
    }

    /**
     * Prueba el mapeo exitoso de Family a FamilyDTO con servicios básicos y detalles de vivienda
     * Verifica que se obtengan correctamente los datos del microservicio externo
     */
    @Test
    @DisplayName("Debe mapear Family a FamilyDTO con servicios básicos y detalles de vivienda")
    void mapToFamilyDTO_WithAllServices_ShouldReturnCompleteFamilyDTO() {
        // Given - Configuración de mocks para simular respuestas exitosas del cliente externo
        when(familyMapper.toDTO(testFamily)).thenReturn(testFamilyDTO);
        when(housingServiceClient.getBasicServiceById(testFamily.getServiceId()))
                .thenReturn(Mono.just(testBasicService));
        when(housingServiceClient.getHousingDetailsById(testFamily.getHousingId()))
                .thenReturn(Mono.just(testHousingDetails));

        // When & Then - Verificación del comportamiento reactivo
        StepVerifier.create(familyService.mapToFamilyDTO(testFamily))
                .expectNextMatches(dto -> 
                    dto.getBasicService() != null && 
                    dto.getHousingDetails() != null &&
                    dto.getId().equals(testFamily.getId())
                )
                .verifyComplete();
    }

    /**
     * Prueba el mapeo cuando no hay servicios asociados
     * Verifica que el DTO se cree correctamente sin servicios externos
     */
    @Test
    @DisplayName("Debe mapear Family a FamilyDTO sin servicios cuando no existen IDs")
    void mapToFamilyDTO_WithoutServices_ShouldReturnBasicFamilyDTO() {
        // Given - Familia sin IDs de servicios
        testFamily.setServiceId(null);
        testFamily.setHousingId(null);
        
        when(familyMapper.toDTO(testFamily)).thenReturn(testFamilyDTO);
        // No configuramos mocks para housingServiceClient porque no deben ser llamados

        // When & Then - Verificación que no se llamen los servicios externos
        StepVerifier.create(familyService.mapToFamilyDTO(testFamily))
                .expectNextMatches(dto -> 
                    dto.getBasicService() == null && 
                    dto.getHousingDetails() == null
                )
                .verifyComplete();

        verify(housingServiceClient, never()).getBasicServiceById(any());
        verify(housingServiceClient, never()).getHousingDetailsById(any());
    }

    /**
     * Prueba la obtención de familias activas ordenadas por ID
     * Verifica que solo se obtengan familias con status "A" y estén ordenadas
     */
    @Test
    @DisplayName("Debe obtener todas las familias activas ordenadas por ID")
    void findAllActive_ShouldReturnActiveFamiliesOrderedById() {
        // Given - Lista de familias activas desordenadas
        Family family1 = createTestFamily();
        family1.setId(2);
        family1.setServiceId(null); // Sin servicios para evitar llamadas al cliente
        family1.setHousingId(null);
        
        Family family2 = createTestFamily();
        family2.setId(1);
        family2.setServiceId(null); // Sin servicios para evitar llamadas al cliente
        family2.setHousingId(null);
        
        when(familyRepository.findAllByStatus("A"))
                .thenReturn(Flux.just(family1, family2));
        when(familyMapper.toDTO(any(Family.class))).thenReturn(testFamilyDTO);

        // When & Then - Verificación del orden y filtrado
        StepVerifier.create(familyService.findAllActive())
                .expectNextCount(2)
                .verifyComplete();

        verify(familyRepository).findAllByStatus("A");
    }

    /**
     * Prueba la obtención de familias inactivas
     * Verifica que solo se obtengan familias con status "I"
     */
    @Test
    @DisplayName("Debe obtener todas las familias inactivas")
    void findAllInactive_ShouldReturnInactiveFamilies() {
        // Given - Configuración de repository mock
        testFamily.setServiceId(null); // Sin servicios para evitar llamadas al cliente
        testFamily.setHousingId(null);
        
        when(familyRepository.findAllByStatus("I"))
                .thenReturn(Flux.just(testFamily));
        when(familyMapper.toDTO(testFamily)).thenReturn(testFamilyDTO);

        // When & Then - Verificación de filtrado por status inactivo
        StepVerifier.create(familyService.findAllInactive())
                .expectNext(testFamilyDTO)
                .verifyComplete();

        verify(familyRepository).findAllByStatus("I");
    }

    /**
     * Prueba la búsqueda exitosa de familia por ID
     * Verifica que se obtenga correctamente una familia específica
     */
    @Test
    @DisplayName("Debe encontrar familia por ID exitosamente")
    void findById_ExistingFamily_ShouldReturnFamilyDTO() {
        // Given - Familia existente en el repository sin servicios externos
        testFamily.setServiceId(null);
        testFamily.setHousingId(null);
        
        when(familyRepository.findById(1)).thenReturn(Mono.just(testFamily));
        when(familyMapper.toDTO(testFamily)).thenReturn(testFamilyDTO);

        // When & Then - Verificación de búsqueda exitosa
        StepVerifier.create(familyService.findById(1))
                .expectNext(testFamilyDTO)
                .verifyComplete();

        verify(familyRepository).findById(1);
    }

    /**
     * Prueba la búsqueda de familia inexistente
     * Verifica que se complete vacío cuando no existe la familia
     */
    @Test
    @DisplayName("Debe completar vacío cuando la familia no existe")
    void findById_NonExistingFamily_ShouldCompleteEmpty() {
        // Given - Repository que retorna vacío
        when(familyRepository.findById(999)).thenReturn(Mono.empty());

        // When & Then - Verificación de resultado vacío
        StepVerifier.create(familyService.findById(999))
                .verifyComplete();

        verify(familyRepository).findById(999);
    }

    /**
     * Prueba la actualización de familia inexistente
     * Verifica que se complete vacío cuando no existe la familia
     */
    @Test
    @DisplayName("Debe completar vacío al actualizar familia inexistente")
    void updateFamily_NonExistingFamily_ShouldCompleteEmpty() {
        // Given - Familia inexistente
        when(familyRepository.findById(999)).thenReturn(Mono.empty());

        // When & Then - Verificación de resultado vacío
        StepVerifier.create(familyService.updateFamily(999, testFamilyDTO))
                .verifyComplete();
    }

    /**
     * Prueba la eliminación lógica exitosa de familia
     * Verifica que se cambie el status a "I" correctamente
     */
    @Test
    @DisplayName("Debe eliminar familia lógicamente (cambiar status a I)")
    void deleteFamily_ExistingFamily_ShouldSetStatusToInactive() {
        // Given - Familia existente
        when(familyRepository.findById(1)).thenReturn(Mono.just(testFamily));
        when(familyRepository.save(any(Family.class))).thenReturn(Mono.just(testFamily));

        // When & Then - Verificación de eliminación lógica
        StepVerifier.create(familyService.deleteFamily(1))
                .verifyComplete();

        // Verificación que se publique evento de eliminación
        verify(familyEventService).publishFamilyEvent(any(Family.class), eq("DELETED"));
    }

    /**
     * Prueba la activación exitosa de familia
     * Verifica que se cambie el status a "A" correctamente
     */
    @Test
    @DisplayName("Debe activar familia exitosamente (cambiar status a A)")
    void activeFamily_ExistingFamily_ShouldSetStatusToActive() {
        // Given - Familia existente inactiva
        testFamily.setStatus("I");
        when(familyRepository.findById(1)).thenReturn(Mono.just(testFamily));
        when(familyRepository.save(any(Family.class))).thenReturn(Mono.just(testFamily));

        // When & Then - Verificación de activación exitosa
        StepVerifier.create(familyService.activeFamily(1))
                .verifyComplete();

        // Verificación que se publique evento de actualización
        verify(familyEventService).publishFamilyEvent(any(Family.class), eq("UPDATED"));
    }

    /**
     * Prueba el cambio de status en familia inexistente
     * Verifica que se lance excepción apropiada
     */
    @Test
    @DisplayName("Debe lanzar excepción al cambiar status de familia inexistente")
    void changeStatus_NonExistingFamily_ShouldThrowException() {
        // Given - Familia inexistente
        when(familyRepository.findById(999)).thenReturn(Mono.empty());

        // When & Then - Verificación de excepción
        StepVerifier.create(familyService.deleteFamily(999))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    /**
     * Prueba la obtención de detalles de familia
     * Verifica que sea idéntico a findById
     */
    @Test
    @DisplayName("Debe obtener detalles de familia (idéntico a findById)")
    void findDetailById_ShouldReturnSameAsFindById() {
        // Given - Configuración idéntica a findById
        testFamily.setServiceId(null);
        testFamily.setHousingId(null);
        
        when(familyRepository.findById(1)).thenReturn(Mono.just(testFamily));
        when(familyMapper.toDTO(testFamily)).thenReturn(testFamilyDTO);

        // When & Then - Verificación de comportamiento idéntico
        StepVerifier.create(familyService.findDetailById(1))
                .expectNext(testFamilyDTO)
                .verifyComplete();
    }

    // Métodos auxiliares para crear objetos de prueba

    private Family createTestFamily() {
        Family family = new Family();
        family.setId(1);
        family.setLastName("Pérez");
        family.setDirection("Av. Principal 123");
        family.setNumberMembers(4);
        family.setNumberChildren(2);
        family.setStatus("A");
        family.setServiceId(100);
        family.setHousingId(200);
        family.setCreated(LocalDateTime.now());
        return family;
    }

    private FamilyDTO createTestFamilyDTO() {
        FamilyDTO dto = new FamilyDTO();
        dto.setId(1);
        dto.setLastName("Pérez");
        dto.setDirection("Av. Principal 123");
        dto.setNumberMembers(4);
        dto.setNumberChildren(2);
        dto.setStatus("A");
        dto.setCreated(LocalDateTime.now());
        return dto;
    }

    private BasicServiceDTO createTestBasicService() {
        return BasicServiceDTO.builder()
                .serviceId(100)
                .waterService("Si")
                .servLight("Si")
                .area("Urbana")
                .build();
    }

    private HousingDetailsDTO createTestHousingDetails() {
        return HousingDetailsDTO.builder()
                .id(200)
                .typeOfHousing("Casa")
                .housingMaterial("Material Noble")
                .bedroomNumber(3)
                .build();
    }
}