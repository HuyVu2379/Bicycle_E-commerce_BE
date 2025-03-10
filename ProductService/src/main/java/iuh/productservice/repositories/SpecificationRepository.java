package iuh.productservice.repositories;

import iuh.productservice.entities.Specification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecificationRepository extends MongoRepository<Specification, String> {
}
