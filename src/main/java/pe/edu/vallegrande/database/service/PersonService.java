package pe.edu.vallegrande.database.service;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.database.client.FamilyServiceClient;
import pe.edu.vallegrande.database.model.Person;
import pe.edu.vallegrande.database.repository.PersonRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final FamilyServiceClient familyServiceClient;

    public PersonService(PersonRepository personRepository, FamilyServiceClient familyServiceClient) {
        this.personRepository = personRepository;
        this.familyServiceClient = familyServiceClient;
    }

    /**
     * Obtiene lista de personas activas, ordenadas por ID
     */
    public Flux<Person> listActive() {
        return personRepository.findByState("A")
                .sort((p1, p2) -> p1.getIdPerson().compareTo(p2.getIdPerson()));
    }

    /**
     * Obtiene lista de personas inactivas, ordenadas por ID
     */
    public Flux<Person> listInactive() {
        return personRepository.findByState("I")
                .sort((p1, p2) -> p1.getIdPerson().compareTo(p2.getIdPerson()));
    }

    /**
     * Crea múltiples personas validando que las familias asociadas existan
     */
    public Flux<Person> createPersons(Flux<Person> persons) {
        return persons.flatMap(person -> {
            Integer familyId = person.getFamilyIdFamily();
            if (familyId != null) {
                return validateFamilyAndSavePerson(person, familyId);
            } else {
                return savePersonWithActiveState(person);
            }
        });
    }

    /**
     * Elimina lógicamente una persona (cambia estado a inactivo)
     */
    public Mono<Person> logicallyDelete(Integer id) {
        return findPersonByIdAndUpdateState(id, "I");
    }

    /**
     * Reactiva una persona (cambia estado a activo)
     */
    public Mono<Person> reactivate(Integer id) {
        return findPersonByIdAndUpdateState(id, "A");
    }

    /**
     * Lista personas por ID de familia
     */
    public Flux<Person> listByFamily(Integer familyId) {
        return personRepository.findByFamilyIdFamily(familyId)
                .filter(person -> person.getState().equals("A"));
    }

    /**
     * Actualiza los datos de una persona existente
     */
    public Mono<Person> updatePerson(Integer id, Person updatedPerson) {
        return personRepository.findById(id)
                .flatMap(person -> {
                    updatePersonData(person, updatedPerson);
                    return personRepository.save(person);
                });
    }

    /**
     * Elimina lógicamente las personas asociadas a una familia específica
     */
    public Flux<Person> deletePersonsByFamilyId(Integer familyId) {
        return personRepository.findByFamilyIdFamily(familyId)
                .flatMap(person -> {
                    person.setState("I");
                    return personRepository.save(person);
                });
    }

    /**
     * Reactiva las personas asociadas a una familia específica
     */
    public Flux<Person> reactivatePersonsByFamilyId(Integer familyId) {
        return personRepository.findByFamilyIdFamily(familyId)
                .flatMap(person -> {
                    person.setState("A");
                    return personRepository.save(person);
                });
    }

    // Métodos privados auxiliares

    private Mono<Person> validateFamilyAndSavePerson(Person person, Integer familyId) {
        return familyServiceClient.familyExists(familyId)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return savePersonWithActiveState(person);
                    } else {
                        return Mono.error(new RuntimeException("La familia con ID " + familyId + " no existe"));
                    }
                });
    }

    private Mono<Person> savePersonWithActiveState(Person person) {
        person.setState("A");
        return personRepository.save(person);
    }

    private Mono<Person> findPersonByIdAndUpdateState(Integer id, String state) {
        return personRepository.findById(id)
                .flatMap(person -> {
                    person.setState(state);
                    return personRepository.save(person);
                });
    }

    private void updatePersonData(Person person, Person updatedPerson) {
        person.setName(updatedPerson.getName());
        person.setSurname(updatedPerson.getSurname());
        person.setAge(updatedPerson.getAge());
        person.setBirthdate(updatedPerson.getBirthdate());
        person.setTypeDocument(updatedPerson.getTypeDocument());
        person.setDocumentNumber(updatedPerson.getDocumentNumber());
        person.setTypeKinship(updatedPerson.getTypeKinship());
        person.setSponsored(updatedPerson.getSponsored());
        person.setState(updatedPerson.getState());
        person.setFamilyIdFamily(updatedPerson.getFamilyIdFamily());
    }
}