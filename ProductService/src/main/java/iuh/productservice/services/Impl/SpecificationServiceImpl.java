package iuh.productservice.services.Impl;

import iuh.productservice.entities.Specification;
import iuh.productservice.repositories.SpecificationRepository;
import iuh.productservice.services.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecificationRepository specificationRepository;
    @Override
    public Optional<Specification> createSpecification(Specification specification) {
        return Optional.of(specificationRepository.save(specification));
    }

    @Override
    public Optional<Specification> updateSpecification(Specification specification) {
        if (specificationRepository.findById(specification.getSpecificationId()).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(specificationRepository.save(specification));
    }

    @Override
    public boolean deleteSpecificationByProductId(String productId) {
        Optional<Specification> specification = specificationRepository.findByProductId(productId);
        if (specification.isEmpty()) {
            return false;
        }
        specificationRepository.deleteById(specification.get().getSpecificationId());
        return true;
    }

    @Override
    public Optional<Specification> findSpecificationByProductId(String productId) {
        return specificationRepository.findByProductId(productId);
    }
}
