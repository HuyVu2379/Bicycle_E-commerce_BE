package iuh.productservice.services.Impl;

import iuh.productservice.entities.Specification;
import iuh.productservice.repositories.SpecificationRepository;
import iuh.productservice.services.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecificationRepository specificationRepository;

    @Override
    public List<Specification> createSpecifications(List<Specification> specifications) {
        return specificationRepository.saveAll(specifications);
    }

    @Override
    public List<Specification> updateSpecifications(List<Specification> specifications) {
        return (specificationRepository.saveAll(specifications));
    }

    @Override
    public boolean deleteAllSpecificationByProductId(String productId) {
        List<Specification> specifications = specificationRepository.findAllByProductId(productId);
        if (specifications.isEmpty()) {
            return false;
        }
        specificationRepository.deleteAll(specifications);
        return true;
    }

    @Override
    public List<Specification> findSpecificationsByProductId(String productId) {
        return specificationRepository.findAllByProductId(productId);
    }
}
