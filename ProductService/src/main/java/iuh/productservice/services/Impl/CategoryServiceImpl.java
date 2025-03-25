package iuh.productservice.services.Impl;

import iuh.productservice.entities.Category;
import iuh.productservice.repositories.CategoryRepository;
import iuh.productservice.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> addCategory(Category category) {
        return Optional.of(categoryRepository.save(category));
    }

    @Override
    public Optional<Category> updateCategory(Category category) {
        Optional<Category> existingCategory = categoryRepository.findById(category.getCategoryId());
        if (existingCategory.isPresent()) {
            Category updatedCategory = existingCategory.get();
            updatedCategory.setName(category.getName());
            updatedCategory.setDescription(category.getDescription());
            return Optional.of(categoryRepository.save(updatedCategory));
        }
        return Optional.empty();
    }

    @Override
    public boolean removeCategory(String id) {
        Category category = categoryRepository.findById(id).get();
        if (category != null) {
            categoryRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeAllCategories() {
        try {
            categoryRepository.deleteAll();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean bulkDelete(List<String> array) {
        List<Category> cartItemList = categoryRepository.findAllById(array);
        if (cartItemList.size() == array.size()) {
            categoryRepository.deleteAll(cartItemList);
            return true;
        }
        return false;
    }
}
