package iuh.productservice.services;

import iuh.productservice.entities.Product;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ProductService {
    Optional<Product> createProduct(Product product);
    double getPrice(String productId);
}
