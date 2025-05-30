package pe.edu.vallegrande.information.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.information.model.BasicService;
import pe.edu.vallegrande.information.repository.BasicServiceRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicServiceServiceTest {

    @Mock
    private BasicServiceRepository basicServiceRepository;

    @InjectMocks
    private BasicServiceService basicServiceService;

    private BasicService basicService;
    private BasicService updatedBasicService;

    /**
     * Configura los objetos de prueba antes de cada método de prueba.
     * Inicializa un objeto BasicService con valores predeterminados y otro para actualizaciones.
     */
    @BeforeEach
    void setUp() {
        // Inicializa un objeto BasicService con valores de prueba
        basicService = BasicService.builder()
                .serviceId(1)
                .waterService("Available")
                .servDrain("Available")
                .servLight("Available")
                .servCable("Available")
                .servGas("Available")
                .area("Urban")
                .referenceLocation("Downtown")
                .residue("Collected")
                .publicLighting("Available")
                .security("High")
                .material("Concrete")
                .feeding("Good")
                .economic("Stable")
                .spiritual("Active")
                .socialCompany("Present")
                .guideTip("Follow rules")
                .build();

        // Inicializa un objeto BasicService actualizado con valores de prueba
        updatedBasicService = BasicService.builder()
                .serviceId(1)
                .waterService("Updated Water")
                .servDrain("Updated Drain")
                .servLight("Updated Light")
                .servCable("Updated Cable")
                .servGas("Updated Gas")
                .area("Updated Area")
                .referenceLocation("Updated Location")
                .residue("Updated Residue")
                .publicLighting("Updated Lighting")
                .security("Updated Security")
                .material("Updated Material")
                .feeding("Updated Feeding")
                .economic("Updated Economic")
                .spiritual("Updated Spiritual")
                .socialCompany("Updated Company")
                .guideTip("Updated Tip")
                .build();
    }

    /**
     * Verifica que se devuelvan todos los servicios existentes.
     */
    @Test
    void findAll_ShouldReturnAllServices() {
        // Given
        when(basicServiceRepository.findAll()).thenReturn(Flux.just(basicService));

        // When & Then
        StepVerifier.create(basicServiceService.findAll())
                .expectNext(basicService)
                .verifyComplete();

        verify(basicServiceRepository).findAll();
    }

    /**
     * Verifica que se devuelva un flujo vacío si no existen servicios.
     */
    @Test
    void findAll_ShouldReturnEmptyFlux_WhenNoServicesExist() {
        // Given
        when(basicServiceRepository.findAll()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(basicServiceService.findAll())
                .verifyComplete();

        verify(basicServiceRepository).findAll();
    }

    /**
     * Verifica que se devuelva un servicio específico cuando existe.
     */
    @Test
    void findById_ShouldReturnService_WhenServiceExists() {
        // Given
        when(basicServiceRepository.findById(1)).thenReturn(Mono.just(basicService));

        // When & Then
        StepVerifier.create(basicServiceService.findById(1))
                .expectNext(basicService)
                .verifyComplete();

        verify(basicServiceRepository).findById(1);
    }

    /**
     * Verifica que se devuelva vacío si el servicio no existe.
     */
    @Test
    void findById_ShouldReturnEmpty_WhenServiceDoesNotExist() {
        // Given
        when(basicServiceRepository.findById(anyInt())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(basicServiceService.findById(999))
                .verifyComplete();

        verify(basicServiceRepository).findById(999);
    }

    /**
     * Verifica que se devuelva el servicio guardado correctamente.
     */
    @Test
    void save_ShouldReturnSavedService() {
        // Given
        when(basicServiceRepository.save(any(BasicService.class))).thenReturn(Mono.just(basicService));

        // When & Then
        StepVerifier.create(basicServiceService.save(basicService))
                .expectNext(basicService)
                .verifyComplete();

        verify(basicServiceRepository).save(basicService);
    }

    /**
     * Verifica que se maneje correctamente el intento de guardar un servicio nulo.
     */
    @Test
    void save_ShouldHandleNullService() {
        // Given
        when(basicServiceRepository.save(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(basicServiceService.save(null))
                .verifyComplete();

        verify(basicServiceRepository).save(null);
    }

    /**
     * Verifica que se devuelva el servicio actualizado cuando existe.
     */
    @Test
    void update_ShouldReturnUpdatedService_WhenServiceExists() {
        // Given
        when(basicServiceRepository.findById(1)).thenReturn(Mono.just(basicService));
        when(basicServiceRepository.save(any(BasicService.class))).thenReturn(Mono.just(basicService));

        // When & Then
        StepVerifier.create(basicServiceService.update(1, updatedBasicService))
                .expectNextMatches(service -> 
                    "Updated Water".equals(service.getWaterService()) &&
                    "Updated Drain".equals(service.getServDrain()) &&
                    "Updated Light".equals(service.getServLight()) &&
                    "Updated Cable".equals(service.getServCable()) &&
                    "Updated Gas".equals(service.getServGas()) &&
                    "Updated Area".equals(service.getArea()) &&
                    "Updated Location".equals(service.getReferenceLocation()) &&
                    "Updated Residue".equals(service.getResidue()) &&
                    "Updated Lighting".equals(service.getPublicLighting()) &&
                    "Updated Security".equals(service.getSecurity()) &&
                    "Updated Material".equals(service.getMaterial()) &&
                    "Updated Feeding".equals(service.getFeeding()) &&
                    "Updated Economic".equals(service.getEconomic()) &&
                    "Updated Spiritual".equals(service.getSpiritual()) &&
                    "Updated Company".equals(service.getSocialCompany()) &&
                    "Updated Tip".equals(service.getGuideTip())
                )
                .verifyComplete();

        verify(basicServiceRepository).findById(1);
        verify(basicServiceRepository).save(any(BasicService.class));
    }

    /**
     * Verifica que se devuelva vacío si el servicio no existe al intentar actualizar.
     */
    @Test
    void update_ShouldReturnEmpty_WhenServiceDoesNotExist() {
        // Given
        when(basicServiceRepository.findById(anyInt())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(basicServiceService.update(999, updatedBasicService))
                .verifyComplete();

        verify(basicServiceRepository).findById(999);
        verify(basicServiceRepository, never()).save(any());
    }

    /**
     * Verifica que se maneje correctamente el intento de actualizar un servicio con un DTO nulo.
     */
    @Test
    void update_ShouldHandleNullDTO() {
        // Given
        when(basicServiceRepository.findById(1)).thenReturn(Mono.just(basicService));
        when(basicServiceRepository.save(any(BasicService.class))).thenReturn(Mono.just(basicService));

        // When & Then
        StepVerifier.create(basicServiceService.update(1, null))
                .expectNext(basicService)
                .verifyComplete();

        verify(basicServiceRepository).findById(1);
        verify(basicServiceRepository).save(basicService);
    }

    /**
     * Verifica que la eliminación de un servicio se complete exitosamente.
     */
    @Test
    void delete_ShouldCompleteSuccessfully() {
        // Given
        when(basicServiceRepository.deleteById(1)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(basicServiceService.delete(1))
                .verifyComplete();

        verify(basicServiceRepository).deleteById(1);
    }

    /**
     * Verifica que se maneje correctamente la eliminación de un ID no existente.
     */
    @Test
    void delete_ShouldHandleNonExistentId() {
        // Given
        when(basicServiceRepository.deleteById(anyInt())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(basicServiceService.delete(999))
                .verifyComplete();

        verify(basicServiceRepository).deleteById(999);
    }
}
