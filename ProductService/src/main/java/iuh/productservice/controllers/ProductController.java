package iuh.productservice.controllers;

import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.entities.Product;
import iuh.productservice.exception.erorrs.NotFoundException;
import iuh.productservice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Product>> createProduct(@RequestBody Product product) {
        System.out.println("Check product from controller: " + product);
        Optional<Product> productResponse = productService.createProduct(product);
        if (productResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Product creation failed", false, null));
        }
        return SuccessEntityResponse.created("Product created successfully", productResponse.get());
    }

    @GetMapping("/public/get-price/{productId}")
    public ResponseEntity<MessageResponse<Double>> getPrice(@PathVariable String productId) {
        double price = productService.getPrice(productId);
        return SuccessEntityResponse.ok("Price retrieved successfully", price);
    }

    @GetMapping("/public/get-name/{productId}")
    public ResponseEntity<MessageResponse<String>> getProductName(@PathVariable String productId) {
        String productName = productService.getProductName(productId);
        return SuccessEntityResponse.ok("Product name retrieved successfully", productName);
    }
    @PutMapping("/update/{productId}")
    public ResponseEntity<MessageResponse<Product>> updateProduct(@RequestBody Product product, @PathVariable String productId) {
        Optional<Product> productResponse = productService.updateProduct(product, productId);
        if (productResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Product update failed", false, null));
        }
        return SuccessEntityResponse.ok("Product updated successfully", productResponse.get());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<MessageResponse<Boolean>> deleteProduct(@PathVariable String productId) {
        boolean isDeleted = productService.deleteProduct(productId);
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product with id " + productId);
        }
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Product deletion failed", false, null));
        }
        return SuccessEntityResponse.ok("Product deleted successfully", isDeleted);
    }

    @GetMapping("/public/all")
    public ResponseEntity<MessageResponse<List<Product>>> getAllProducts() {
        return SuccessEntityResponse.ok("All products retrieved successfully", productService.getAllProduct());
    }

    @GetMapping("/public/getProductsByCategory/{categoryId}")
    public ResponseEntity<MessageResponse<List<Product>>> getProductsByCategory(@PathVariable String categoryId) {
        return SuccessEntityResponse.ok("Products retrieved successfully", productService.getProductByCategory(categoryId));
    }

    @GetMapping("/public/getProductsBySupplier/{supplierId}")
    public ResponseEntity<MessageResponse<List<Product>>> getProductsBySupplier(@PathVariable String supplierId) {
        return SuccessEntityResponse.ok("Products retrieved successfully", productService.getProductBySupplier(supplierId));
    }

    @GetMapping("/public/getProductsBySearchName")
    public ResponseEntity<MessageResponse<List<Product>>> getProductsBySearchText(@RequestParam String searchText) {
        List<Product> products = productService.getProductBySearch(searchText);
        if (products.isEmpty()) {
            throw new NotFoundException("Product with name " + searchText);
        }
        return SuccessEntityResponse.ok("Products retrieved successfully", productService.getProductBySearch(searchText));
    }

    @GetMapping("/public/getProductsWithPage")
    public ResponseEntity<MessageResponse<List<Product>>> getProductsWithPage(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return SuccessEntityResponse.ok("Products retrieved successfully", productService.getProductWithPage(pageSize, pageNo, sortBy, sortDirection));
    }
}
