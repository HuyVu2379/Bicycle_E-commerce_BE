package iuh.productservice.services;

import iuh.productservice.dtos.responses.ProductResponse;
import iuh.productservice.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ProductService {
    boolean existsById(String productId);
    Optional<Product> createProduct(Product product);
    double getPrice(String productId);
    String getProductName(String productId);
    Optional<Product> updateProduct(Product product,String productId);
    boolean deleteProduct(String productId);
    Optional<ProductResponse> getProductById(String productId);
    List<Product> getAllProduct();
    List<Product> getProductByCategory(String categoryId);
    List<Product> getProductBySupplier(String supplierId);
    List<Product> getProductBySearch(String searchText);
    Page<ProductResponse> getProductWithPage(int pageNo, int pageSize, String sortBy, String sortDirection);
}
