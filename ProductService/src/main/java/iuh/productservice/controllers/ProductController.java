package iuh.productservice.controllers;

import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.ProductResponse;
import iuh.productservice.dtos.responses.ProductResponseAtHome;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.entities.Product;
import iuh.productservice.exception.erorrs.NotFoundException;
import iuh.productservice.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private SpecificationService specificationService;
    @Autowired
    private SupplierService supplierService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse<Product>> createProduct(@RequestBody Product product) {
        try {
            System.out.println("Check product from controller: " + product);
            Optional<Product> productResponse = productService.createProduct(product);
            if (productResponse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                                "Product creation failed. Possibly duplicate or invalid data.", false, null));
            }
            return SuccessEntityResponse.created("Product created successfully", productResponse.get());
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/public/{productId}")
    public ResponseEntity<MessageResponse<ProductResponse>> getProductById(@PathVariable String productId) {
        try {
            Optional<ProductResponse> productResponse = productService.getProductById(productId);
            if (productResponse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                                "Product creation failed. Possibly duplicate or invalid data.", false, null));
            }
            return SuccessEntityResponse.ok("Product created successfully", productResponse.get());
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/public/get-price/{productId}")
    public ResponseEntity<MessageResponse<Double>> getPrice(@PathVariable String productId) {
        try {
            double price = productService.getPrice(productId);
            return SuccessEntityResponse.ok("Price retrieved successfully", price);
        } catch (NotFoundException e) {
            throw new NotFoundException("Product not found");
        }
    }

    @GetMapping("/public/get-name/{productId}")
    public ResponseEntity<MessageResponse<String>> getProductName(@PathVariable String productId) {
        try {
            String productName = productService.getProductName(productId);
            return SuccessEntityResponse.ok("Product name retrieved successfully", productName);
        } catch (NotFoundException e) {
            throw new NotFoundException("Product not found");
        }
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<MessageResponse<Product>> updateProduct(@RequestBody Product product, @PathVariable String productId) {
        try {
            Optional<Product> productResponse = productService.updateProduct(product, productId);
            if (productResponse.isEmpty()) {
                throw new NotFoundException("Product not found or invalid data");
            }
            return SuccessEntityResponse.ok("Product updated successfully", productResponse.get());
        } catch (Exception e) {
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<MessageResponse<Boolean>> deleteProduct(@PathVariable String productId) {
        try {
            if (!productService.existsById(productId)) {
                throw new NotFoundException("Product with id " + productId + " not found");
            }
            boolean isDeleted = productService.deleteProduct(productId);
            if (!isDeleted) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Product deletion failed due to server error", false, false));
            }
            return SuccessEntityResponse.ok("Product deleted successfully", true);
        } catch (Exception e) {
            throw e;
        }

    }

    @GetMapping("/public/all")
    public ResponseEntity<MessageResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProduct();
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new MessageResponse<>(HttpStatus.NO_CONTENT.value(),
                            "No products found", true, products));
        }
        return SuccessEntityResponse.ok("All products retrieved successfully", products);
    }

    @GetMapping("/public/getProductsByCategory/{categoryId}")
    public ResponseEntity<MessageResponse<List<Product>>> getProductsByCategory(@PathVariable String categoryId) {
        List<Product> products = productService.getProductByCategory(categoryId);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new MessageResponse<>(HttpStatus.NO_CONTENT.value(),
                            "No products found for category " + categoryId, true, products));
        }
        return SuccessEntityResponse.ok("Products retrieved successfully", products);
    }

    @GetMapping("/public/getProductsBySupplier/{supplierId}")
    public ResponseEntity<MessageResponse<List<Product>>> getProductsBySupplier(@PathVariable String supplierId) {
        List<Product> products = productService.getProductBySupplier(supplierId);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new MessageResponse<>(HttpStatus.NO_CONTENT.value(),
                            "No products found for supplier " + supplierId, true, products));
        }
        return SuccessEntityResponse.ok("Products retrieved successfully", products);
    }

    @GetMapping("/public/getProductsBySearchName")
    public ResponseEntity<MessageResponse<List<Product>>> getProductsBySearchText(@RequestParam String searchText) {
        List<Product> products = productService.getProductBySearch(searchText);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse<>(HttpStatus.NOT_FOUND.value(),
                            "No products found with name containing: " + searchText, false, null));
        }
        return SuccessEntityResponse.ok("Products retrieved successfully", products);
    }

    @GetMapping("/public/getProductsWithPage")
    public ResponseEntity<MessageResponse<Page<ProductResponse>>> getProductsWithPage(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        System.out.println("check productResponsePage: " + pageSize + " " + pageNo + " " + sortBy + " " + sortDirection);
        // Gọi service để lấy Page<ProductResponse>
        Page<ProductResponse> productResponsePage = productService.getProductWithPage(pageNo, pageSize, sortBy, sortDirection);
        System.out.println("check productResponsePage: " + productResponsePage);
        // Kiểm tra kết quả
        if (productResponsePage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new MessageResponse<>(HttpStatus.NO_CONTENT.value(),
                            "No products found", true, productResponsePage));
        }

        return SuccessEntityResponse.ok("Products retrieved successfully", productResponsePage);
    }

    @GetMapping("/public/getProductForHome")
    public ResponseEntity<MessageResponse<List<ProductResponseAtHome>>> getAllProduct(
    ) {
        List<ProductResponseAtHome> products = productService.getProductsWithPagination();
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new MessageResponse<>(HttpStatus.NO_CONTENT.value(),
                            "No products found", true, products));
        }
        return SuccessEntityResponse.ok("Products retrieved successfully", products);
    }
}
