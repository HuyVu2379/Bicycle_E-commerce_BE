package iuh.productservice.services;

import iuh.productservice.entities.Category;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CategoryService {
    public Optional<Category> findById(String id);
    public Category getAll(Category category);
}
