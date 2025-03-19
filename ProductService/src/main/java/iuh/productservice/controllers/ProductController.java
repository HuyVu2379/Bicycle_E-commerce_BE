package iuh.productservice.controllers;

import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.entities.Product;
import iuh.productservice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Product>> createProduct(@RequestBody Product product) {
        Optional<Product> productResponse = productService.createProduct(product);
        if (productResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Product creation failed", false, null));
        }
        return SuccessEntityResponse.created("Category created successfully", productResponse.get());
    }

    @GetMapping("/public/get-price/{productId}")
    public ResponseEntity<MessageResponse<Double>> getPrice(@PathVariable String productId) {
        double price = productService.getPrice(productId);
        return SuccessEntityResponse.ok("Price retrieved successfully", price);
    }
}
