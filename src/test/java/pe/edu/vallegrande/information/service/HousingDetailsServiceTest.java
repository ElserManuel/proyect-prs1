package pe.edu.vallegrande.information.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.information.model.HousingDetails;
import pe.edu.vallegrande.information.repository.HousingDetailsRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousingDetailsServiceTest {

    @Mock
    private HousingDetailsRepository housingDetailsRepository;

    @InjectMocks
    private HousingDetailsService housingDetailsService;

    private HousingDetails housingDetails;
    private HousingDetails updatedHousingDetails;

    /**
     * Configura los objetos de prueba antes de cada método de prueba.
     * Inicializa un objeto HousingDetails con valores predeterminados y otro para actualizaciones.
     */
    @BeforeEach
    void setUp() {
        housingDetails = HousingDetails.builder()
                .id(1)
                .typeOfHousing("Apartment")
                .housingMaterial("Concrete")
                .housingSecurity("High")
                .homeEnvironment(85)
                .bedroomNumber(3)
                .habitability("Good")
                .numberRooms(5)
                .numberOfBedrooms(3)
                .habitabilityBuilding("Excellent")
                .build();

        updatedHousingDetails = HousingDetails.builder()
                .id(1)
                .typeOfHousing("Updated House")
                .housingMaterial("Updated Material")
                .housingSecurity("Updated Security")
                .homeEnvironment(90)
                .bedroomNumber(4)
                .habitability("Updated Habitability")
                .numberRooms(6)
                .numberOfBedrooms(4)
                .habitabilityBuilding("Updated Building")
                .build();
    }

    /**
     * Verifica que se devuelvan todos los detalles de vivienda existentes.
     */
    @Test
    void findAll_ShouldReturnAllHousingDetails() {
        // Given
        when(housingDetailsRepository.findAll()).thenReturn(Flux.just(housingDetails));

        // When & Then
        StepVerifier.create(housingDetailsService.findAll())
                .expectNext(housingDetails)
                .verifyComplete();

        verify(housingDetailsRepository).findAll();
    }

    /**
     * Verifica que se devuelva un flujo vacío si no existen detalles de vivienda.
     */
    @Test
    void findAll_ShouldReturnEmptyFlux_WhenNoHousingDetailsExist() {
        // Given
        when(housingDetailsRepository.findAll()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(housingDetailsService.findAll())
                .verifyComplete();

        verify(housingDetailsRepository).findAll();
    }

    /**
     * Verifica que se devuelvan los detalles de vivienda específicos cuando existen.
     */
    @Test
    void findById_ShouldReturnHousingDetails_WhenHousingDetailsExists() {
        // Given
        when(housingDetailsRepository.findById(1)).thenReturn(Mono.just(housingDetails));

        // When & Then
        StepVerifier.create(housingDetailsService.findById(1))
                .expectNext(housingDetails)
                .verifyComplete();

        verify(housingDetailsRepository).findById(1);
    }

    /**
     * Verifica que se devuelva vacío si los detalles de vivienda no existen.
     */
    @Test
    void findById_ShouldReturnEmpty_WhenHousingDetailsDoesNotExist() {
        // Given
        when(housingDetailsRepository.findById(anyInt())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(housingDetailsService.findById(999))
                .verifyComplete();

        verify(housingDetailsRepository).findById(999);
    }

    /**
     * Verifica que se devuelvan los detalles de vivienda guardados correctamente.
     */
    @Test
    void save_ShouldReturnSavedHousingDetails() {
        // Given
        when(housingDetailsRepository.save(any(HousingDetails.class))).thenReturn(Mono.just(housingDetails));

        // When & Then
        StepVerifier.create(housingDetailsService.save(housingDetails))
                .expectNext(housingDetails)
                .verifyComplete();

        verify(housingDetailsRepository).save(housingDetails);
    }

    /**
     * Verifica que se maneje correctamente el intento de guardar detalles de vivienda nulos.
     */
    @Test
    void save_ShouldHandleNullHousingDetails() {
        // Given
        when(housingDetailsRepository.save(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(housingDetailsService.save(null))
                .verifyComplete();

        verify(housingDetailsRepository).save(null);
    }

    /**
     * Verifica que se devuelvan los detalles de vivienda actualizados cuando existen.
     */
    @Test
    void update_ShouldReturnUpdatedHousingDetails_WhenHousingDetailsExists() {
        // Given
        when(housingDetailsRepository.findById(1)).thenReturn(Mono.just(housingDetails));
        when(housingDetailsRepository.save(any(HousingDetails.class))).thenReturn(Mono.just(housingDetails));

        // When & Then
        StepVerifier.create(housingDetailsService.update(1, updatedHousingDetails))
                .expectNextMatches(housing -> 
                    "Updated House".equals(housing.getTypeOfHousing()) &&
                    "Updated Material".equals(housing.getHousingMaterial()) &&
                    "Updated Security".equals(housing.getHousingSecurity()) &&
                    Integer.valueOf(90).equals(housing.getHomeEnvironment()) &&
                    Integer.valueOf(4).equals(housing.getBedroomNumber()) &&
                    "Updated Habitability".equals(housing.getHabitability()) &&
                    Integer.valueOf(6).equals(housing.getNumberRooms()) &&
                    Integer.valueOf(4).equals(housing.getNumberOfBedrooms()) &&
                    "Updated Building".equals(housing.getHabitabilityBuilding())
                )
                .verifyComplete();

        verify(housingDetailsRepository).findById(1);
        verify(housingDetailsRepository).save(any(HousingDetails.class));
    }

    /**
     * Verifica que se devuelva vacío si los detalles de vivienda no existen al intentar actualizar.
     */
    @Test
    void update_ShouldReturnEmpty_WhenHousingDetailsDoesNotExist() {
        // Given
        when(housingDetailsRepository.findById(anyInt())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(housingDetailsService.update(999, updatedHousingDetails))
                .verifyComplete();

        verify(housingDetailsRepository).findById(999);
        verify(housingDetailsRepository, never()).save(any());
    }

    /**
     * Verifica que se maneje correctamente el intento de actualizar detalles de vivienda con un DTO nulo.
     */
    @Test
    void update_ShouldHandleNullDTO() {
        // Given
        when(housingDetailsRepository.findById(1)).thenReturn(Mono.just(housingDetails));
        when(housingDetailsRepository.save(any(HousingDetails.class))).thenReturn(Mono.just(housingDetails));

        // When & Then
        StepVerifier.create(housingDetailsService.update(1, null))
                .expectNext(housingDetails)
                .verifyComplete();

        verify(housingDetailsRepository).findById(1);
        verify(housingDetailsRepository).save(housingDetails);
    }

    /**
     * Verifica que la eliminación de un detalle de vivienda se complete exitosamente.
     */
    @Test
    void delete_ShouldCompleteSuccessfully() {
        // Given
        when(housingDetailsRepository.deleteById(1)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(housingDetailsService.delete(1))
                .verifyComplete();

        verify(housingDetailsRepository).deleteById(1);
    }

    /**
     * Verifica que se maneje correctamente la eliminación de un ID no existente.
     */
    @Test
    void delete_ShouldHandleNonExistentId() {
        // Given
        when(housingDetailsRepository.deleteById(anyInt())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(housingDetailsService.delete(999))
                .verifyComplete();

        verify(housingDetailsRepository).deleteById(999);
    }
}
