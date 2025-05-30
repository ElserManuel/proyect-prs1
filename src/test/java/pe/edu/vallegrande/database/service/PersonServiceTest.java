package pe.edu.vallegrande.database.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.database.client.FamilyServiceClient;
import pe.edu.vallegrande.database.model.Person;
import pe.edu.vallegrande.database.repository.PersonRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonService Unit Tests")
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;
    
    @Mock
    private FamilyServiceClient familyServiceClient;
    
    @InjectMocks
    private PersonService personService;

    private Person person1;
    private Person person2;
    private Person person3;

    @BeforeEach
    void setUp() {
        person1 = new Person(1, "Juan", "Pérez", 25, LocalDate.of(1998, 5, 15), 
                            "DNI", "12345678", "Padre", "Sí", "A", 1);
        person2 = new Person(2, "María", "González", 30, LocalDate.of(1993, 8, 20), 
                            "DNI", "87654321", "Madre", "No", "A", 1);
        person3 = new Person(3, "Pedro", "López", 22, LocalDate.of(2001, 3, 10), 
                            "DNI", "11223344", "Hijo", "Sí", "I", 2);
    }

    @Nested
    @DisplayName("List Active Persons Tests")
    class ListActivePersonsTests {

        @Test
        @DisplayName("Should return active persons sorted by ID")
        void shouldReturnActivePersonsSortedById() {
            // Given
            Person personWithHigherId = new Person(10, "Ana", "Martínez", 28, LocalDate.of(1995, 7, 12), 
                                                  "DNI", "55566677", "Hermana", "No", "A", 1);
            List<Person> unsortedPersons = Arrays.asList(personWithHigherId, person1, person2);
            
            when(personRepository.findByState("A")).thenReturn(Flux.fromIterable(unsortedPersons));

            // When & Then
            StepVerifier.create(personService.listActive())
                    .expectNext(person1)
                    .expectNext(person2)
                    .expectNext(personWithHigherId)
                    .verifyComplete();

            verify(personRepository).findByState("A");
        }
    }

    @Nested
    @DisplayName("List Inactive Persons Tests")
    class ListInactivePersonsTests {

        @Test
        @DisplayName("Should return inactive persons sorted by ID")
        void shouldReturnInactivePersonsSortedById() {
            // Given
            when(personRepository.findByState("I")).thenReturn(Flux.just(person3));

            // When & Then
            StepVerifier.create(personService.listInactive())
                    .expectNext(person3)
                    .verifyComplete();

            verify(personRepository).findByState("I");
        }
    }

    @Nested
    @DisplayName("Create Persons Tests")
    class CreatePersonsTests {

        @Test
        @DisplayName("Should create person without family validation when familyId is null")
        void shouldCreatePersonWithoutFamilyValidationWhenFamilyIdIsNull() {
            // Given
            Person personWithoutFamily = new Person(null, "Carlos", "Ruiz", 35, LocalDate.of(1988, 12, 5), 
                                                   "DNI", "99887766", "Tío", "No", null, null);
            Person savedPerson = new Person(4, "Carlos", "Ruiz", 35, LocalDate.of(1988, 12, 5), 
                                           "DNI", "99887766", "Tío", "No", "A", null);

            when(personRepository.save(any(Person.class))).thenReturn(Mono.just(savedPerson));

            // When & Then
            StepVerifier.create(personService.createPersons(Flux.just(personWithoutFamily)))
                    .expectNext(savedPerson)
                    .verifyComplete();

            verify(personRepository).save(argThat(person -> "A".equals(person.getState())));
            verifyNoInteractions(familyServiceClient);
        }

        @Test
        @DisplayName("Should create person with family validation when family exists")
        void shouldCreatePersonWithFamilyValidationWhenFamilyExists() {
            // Given
            Person savedPerson = new Person(1, "Juan", "Pérez", 25, LocalDate.of(1998, 5, 15), 
                                           "DNI", "12345678", "Padre", "Sí", "A", 1);

            when(familyServiceClient.familyExists(1)).thenReturn(Mono.just(true));
            when(personRepository.save(any(Person.class))).thenReturn(Mono.just(savedPerson));

            // When & Then
            StepVerifier.create(personService.createPersons(Flux.just(person1)))
                    .expectNext(savedPerson)
                    .verifyComplete();

            verify(familyServiceClient).familyExists(1);
            verify(personRepository).save(argThat(person -> "A".equals(person.getState())));
        }

        @Test
        @DisplayName("Should create multiple persons with mixed scenarios")
        void shouldCreateMultiplePersonsWithMixedScenarios() {
            // Given
            Person personWithoutFamily = new Person(null, "Carlos", "Ruiz", 35, LocalDate.of(1988, 12, 5), 
                                                   "DNI", "99887766", "Tío", "No", null, null);
            Person savedPerson1 = new Person(1, "Juan", "Pérez", 25, LocalDate.of(1998, 5, 15), 
                                            "DNI", "12345678", "Padre", "Sí", "A", 1);
            Person savedPerson2 = new Person(4, "Carlos", "Ruiz", 35, LocalDate.of(1988, 12, 5), 
                                            "DNI", "99887766", "Tío", "No", "A", null);

            when(familyServiceClient.familyExists(1)).thenReturn(Mono.just(true));
            when(personRepository.save(any(Person.class)))
                    .thenReturn(Mono.just(savedPerson1))
                    .thenReturn(Mono.just(savedPerson2));

            // When & Then
            StepVerifier.create(personService.createPersons(Flux.just(person1, personWithoutFamily)))
                    .expectNext(savedPerson1)
                    .expectNext(savedPerson2)
                    .verifyComplete();

            verify(familyServiceClient).familyExists(1);
            verify(personRepository, times(2)).save(any(Person.class));
        }
    }

    @Nested
    @DisplayName("Logical Delete Tests")
    class LogicalDeleteTests {

        @Test
        @DisplayName("Should logically delete person when person exists")
        void shouldLogicallyDeletePersonWhenPersonExists() {
            // Given
            Person deletedPerson = new Person(1, "Juan", "Pérez", 25, LocalDate.of(1998, 5, 15), 
                                             "DNI", "12345678", "Padre", "Sí", "I", 1);

            when(personRepository.findById(1)).thenReturn(Mono.just(person1));
            when(personRepository.save(any(Person.class))).thenReturn(Mono.just(deletedPerson));

            // When & Then
            StepVerifier.create(personService.logicallyDelete(1))
                    .expectNext(deletedPerson)
                    .verifyComplete();

            verify(personRepository).findById(1);
            verify(personRepository).save(argThat(person -> "I".equals(person.getState())));
        }
    }

    @Nested
    @DisplayName("Reactivate Person Tests")
    class ReactivatePersonTests {

        @Test
        @DisplayName("Should reactivate person when person exists")
        void shouldReactivatePersonWhenPersonExists() {
            // Given
            Person reactivatedPerson = new Person(3, "Pedro", "López", 22, LocalDate.of(2001, 3, 10), 
                                                 "DNI", "11223344", "Hijo", "Sí", "A", 2);

            when(personRepository.findById(3)).thenReturn(Mono.just(person3));
            when(personRepository.save(any(Person.class))).thenReturn(Mono.just(reactivatedPerson));

            // When & Then
            StepVerifier.create(personService.reactivate(3))
                    .expectNext(reactivatedPerson)
                    .verifyComplete();

            verify(personRepository).findById(3);
            verify(personRepository).save(argThat(person -> "A".equals(person.getState())));
        }
    }

    @Nested
    @DisplayName("List By Family Tests")
    class ListByFamilyTests {

        @Test
        @DisplayName("Should return active persons by family ID")
        void shouldReturnActivePersonsByFamilyId() {
            // Given
            when(personRepository.findByFamilyIdFamily(1)).thenReturn(Flux.just(person1, person2));

            // When & Then
            StepVerifier.create(personService.listByFamily(1))
                    .expectNext(person1)
                    .expectNext(person2)
                    .verifyComplete();

            verify(personRepository).findByFamilyIdFamily(1);
        }

        @Test
        @DisplayName("Should filter out inactive persons")
        void shouldFilterOutInactivePersons() {
            // Given
            Person inactivePerson = new Person(4, "Ana", "Martínez", 28, LocalDate.of(1995, 7, 12), 
                                              "DNI", "55566677", "Hermana", "No", "I", 1);
            
            when(personRepository.findByFamilyIdFamily(1)).thenReturn(Flux.just(person1, inactivePerson));

            // When & Then
            StepVerifier.create(personService.listByFamily(1))
                    .expectNext(person1)
                    .verifyComplete();

            verify(personRepository).findByFamilyIdFamily(1);
        }
    }

    @Nested
    @DisplayName("Update Person Tests")
    class UpdatePersonTests {

        @Test
        @DisplayName("Should update person when person exists")
        void shouldUpdatePersonWhenPersonExists() {
            // Given
            Person updatedData = new Person(null, "Juan Carlos", "Pérez García", 26, LocalDate.of(1997, 5, 15), 
                                           "DNI", "12345678", "Padre", "No", "A", 2);
            Person savedPerson = new Person(1, "Juan Carlos", "Pérez García", 26, LocalDate.of(1997, 5, 15), 
                                           "DNI", "12345678", "Padre", "No", "A", 2);

            when(personRepository.findById(1)).thenReturn(Mono.just(person1));
            when(personRepository.save(any(Person.class))).thenReturn(Mono.just(savedPerson));

            // When & Then
            StepVerifier.create(personService.updatePerson(1, updatedData))
                    .expectNext(savedPerson)
                    .verifyComplete();

            verify(personRepository).findById(1);
            verify(personRepository).save(argThat(person -> 
                "Juan Carlos".equals(person.getName()) && 
                "Pérez García".equals(person.getSurname()) &&
                26 == person.getAge() &&
                2 == person.getFamilyIdFamily()));
        }

        @Test
        @DisplayName("Should return empty when person does not exist for update")
        void shouldReturnEmptyWhenPersonDoesNotExistForUpdate() {
            // Given
            Person updatedData = new Person(null, "Juan Carlos", "Pérez García", 26, LocalDate.of(1997, 5, 15), 
                                           "DNI", "12345678", "Padre", "No", "A", 2);

            when(personRepository.findById(999)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(personService.updatePerson(999, updatedData))
                    .verifyComplete();

            verify(personRepository).findById(999);
            verify(personRepository, never()).save(any(Person.class));
        }
    }

}