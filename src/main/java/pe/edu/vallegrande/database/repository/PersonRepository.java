package pe.edu.vallegrande.database.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.database.model.Person;
import reactor.core.publisher.Flux;

@Repository
public interface PersonRepository extends ReactiveCrudRepository<Person, Integer> {

    Flux<Person> findByState(String state);
    Flux<Person> findByFamilyIdFamily(Integer familyId);

}
