package iuh.productservice.services.Impl;

import iuh.productservice.client.OrderServiceClient;
import iuh.productservice.dtos.responses.ProductResponse;
import iuh.productservice.dtos.responses.PromotionResponse;
import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.entities.Category;
import iuh.productservice.entities.Product;
import iuh.productservice.entities.Supplier;
import iuh.productservice.mappers.ProductMapper;
import iuh.productservice.repositories.ProductRepository;
import iuh.productservice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private CategoryServiceImpl categoryService;
    @Autowired
    private SupplierServiceImpl supplierService;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public boolean existsById(String productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public Optional<Product> createProduct(Product product) {
        product.setPriceReduced(product.getPrice());
        String promotionId = product.getPromotionId();
        if (promotionId != null) {
            String token = "Bearer " + SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
            try {
                MessageResponse<PromotionResponse> promotionRequest = orderServiceClient.getPromotion(token, promotionId);
                System.out.println("Check promotion: " + promotionRequest);
                if (promotionRequest.isSuccess() && promotionRequest.getData() != null) {
                    double discount = product.getPrice() * (1 - Double.parseDouble(String.valueOf(promotionRequest.getData().getReducePercent())) / 100 );
                    product.setPriceReduced(discount);
                }
            } catch (Exception e) {
                System.out.println("Feign client error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return Optional.of(productRepository.save(product));
    }

    @Override
    public double getPrice(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product.getPrice();
    }

    @Override
    public String getProductName(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product.getName();
    }
    public Optional<Product> updateProduct(Product product, String productId) {
        Product product1 = productRepository.findById(productId).orElse(null);
        if (product1 == null) {
            return Optional.empty();
        }
        product1.setName(product.getName());
        product1.setCategoryId(product.getCategoryId());
        product1.setSupplierId(product.getSupplierId());
        product1.setDescription(product.getDescription());
        product1.setPrice(product.getPrice());
        product1.setPriceReduced(product.getPriceReduced());
        product1.setPromotionId(product.getPromotionId());
        return Optional.of(productRepository.save(product1));
    }

@Override
    public boolean deleteProduct(String productId) {
        return productRepository.deleteByProductId(productId) > 0;
    }

    @Override
    public Optional<ProductResponse> getProductById(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        ProductResponse productResponse = productMapper.productToProductResponse(product);
        Category category = categoryService.getCategoryById(product.getCategoryId()).orElse(null);
        Supplier supplier = supplierService.getSupplierById(product.getSupplierId()).orElse(null);
        productResponse.setCategory(category);
        productResponse.setSupplier(supplier);
        return productResponse != null ? Optional.of(productResponse) : Optional.empty();
    }

    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductByCategory(String categoryId) {
        return productRepository.getProductsByCategoryId(categoryId);
    }

    @Override
    public List<Product> getProductBySupplier(String supplierId) {
        return productRepository.getProductsBySupplierId(supplierId);
    }

    @Override
    public List<Product> getProductBySearch(String searchText) {
        return productRepository.getProductsByName(searchText);
    }

    @Override
    public List<Product> getProductWithPage(int pageNo, int pageSize, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        return productRepository.findAll(pageable).getContent();
    }
}
