package iuh.productservice.services;

import iuh.productservice.entities.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface SpecificationService {
    List<Specification> createSpecifications(List<Specification> specifications);
    List<Specification> updateSpecifications(List<Specification> specifications);
    boolean deleteAllSpecificationByProductId(String productId);
    List<Specification> findSpecificationsByProductId(String productId);
}
