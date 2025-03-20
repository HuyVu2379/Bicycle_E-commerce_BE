package iuh.productservice.services.Impl;

import iuh.productservice.entities.Product;
import iuh.productservice.repositories.ProductRepository;
import iuh.productservice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public Optional<Product> createProduct(Product product) {
        return Optional.of(productRepository.save(product));
    }

    @Override
    public double getPrice(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product.getPrice();
    }
}
