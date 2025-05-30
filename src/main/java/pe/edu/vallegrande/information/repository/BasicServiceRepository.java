package pe.edu.vallegrande.information.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.information.model.BasicService;

@Repository
public interface BasicServiceRepository extends ReactiveCrudRepository<BasicService, Integer> {
     
}
