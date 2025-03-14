package iuh.productservice.controllers;

import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.entities.Category;
import iuh.productservice.entities.Review;
import iuh.productservice.services.Impl.CategoryServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryServiceImpl categoryService;
    
    public CategoryController(CategoryServiceImpl categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/public/getAll")
    public ResponseEntity<MessageResponse<Iterable<Category>>> getAll() {
        return SuccessEntityResponse.ok("All Categorys retrieved successfully", categoryService.getAll());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Category>> createCategory(@RequestBody Category category) {
        Optional<Category> CategoryResponse = categoryService.addCategory(category);
        if (CategoryResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Category creation failed", false, null));
        }
        return SuccessEntityResponse.created("Category created successfully", CategoryResponse.get());
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse<Category>> updateCategory(@RequestBody Category category) {
        Optional<Category> categoryResponse = categoryService.updateCategory(category);
        if (categoryResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Category update failed", false, null));
        }
        return SuccessEntityResponse.ok("Category updated successfully", categoryResponse.get());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse<Boolean>> removeCategory(@PathVariable String id) {
        boolean CategoryResponse = categoryService.removeCategory(id);
        if (!CategoryResponse) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Category deletion failed", false, null));
        }
        return SuccessEntityResponse.ok("Category deleted successfully", CategoryResponse);
    }

    @DeleteMapping("/delete-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse<Boolean>> removeAllCategories() {
        boolean CategoryResponse = categoryService.removeAllCategories();
        if (!CategoryResponse) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Category deletion failed", false, null));
        }
        return SuccessEntityResponse.ok("Category deleted successfully", CategoryResponse);
    }

}
