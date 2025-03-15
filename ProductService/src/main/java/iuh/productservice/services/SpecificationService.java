package iuh.productservice.services;

import iuh.productservice.entities.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface SpecificationService {
    Optional<Specification> createSpecification(Specification specification);
    Optional<Specification> updateSpecification(Specification specification);
    boolean deleteSpecificationByProductId(String productId);
    Optional<Specification> findSpecificationByProductId(String productId);
}
