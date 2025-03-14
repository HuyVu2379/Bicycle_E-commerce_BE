package iuh.productservice.services;

import iuh.productservice.entities.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CategoryService {
    public List<Category> getAll();
    public Optional<Category> addCategory(Category category);
    public Optional<Category> updateCategory(Category category);
    public boolean removeCategory(String id);
    public boolean removeAllCategories();
    public boolean bulkDelete(List<String> array);
}
