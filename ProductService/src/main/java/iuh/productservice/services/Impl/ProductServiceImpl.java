package iuh.productservice.services.Impl;

import iuh.productservice.client.OrderServiceClient;
import iuh.productservice.dtos.responses.ProductResponse;
import iuh.productservice.dtos.responses.PromotionResponse;
import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.entities.*;
import iuh.productservice.enums.Color;
import iuh.productservice.mappers.ProductMapper;
import iuh.productservice.repositories.ProductRepository;
import iuh.productservice.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
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
    public Page<ProductResponse> getProductWithPage(int pageNo, int pageSize, String sortBy, String sortDirection) {
        try {
            log.info("Fetching products with pageNo={}, pageSize={}, sortBy={}, sortDirection={}",
                    pageNo, pageSize, sortBy, sortDirection);
            validateInputs(pageNo, pageSize, sortBy, sortDirection);
            Sort sort = createSort(sortBy, sortDirection);
            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
            List<ProductResponse> productResponses = fetchProductResponses(pageNo, pageSize, sort);
            long total = countTotalDocuments();
            log.info("Fetched {} products, total documents: {}", productResponses.size(), total);
            return new PageImpl<>(productResponses, pageable, total);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching products", e);
            throw new RuntimeException("Error fetching products: " + e.getMessage());
        }
    }

    private void validateInputs(int pageNo, int pageSize, String sortBy, String sortDirection) {
        if (pageNo < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        if (!Arrays.asList("name", "price", "category.name", "inventory.quantity", "supplier.name").contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
        if (!Arrays.asList("ASC", "DESC").contains(sortDirection.toUpperCase())) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection);
        }
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        String mongoSortField = sortBy;
        if (sortBy.equals("category.name")) {
            mongoSortField = "category.name";
        } else if (sortBy.equals("inventory.quantity")) {
            mongoSortField = "inventory.quantity";
        } else if (sortBy.equals("supplier.name")) {
            mongoSortField = "supplier.name";
        }
        return Sort.by(direction, mongoSortField);
    }

    private List<ProductResponse> fetchProductResponses(int pageNo, int pageSize, Sort sort) {
        Aggregation aggregation = buildAggregation(pageNo, pageSize, sort);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "product", Document.class);
        List<Document> documents = results.getMappedResults();
        log.info("Aggregation returned {} documents", documents.size());
        if (documents.isEmpty()) {
            log.warn("No documents returned from aggregation. Check collection data or lookup conditions.");
        } else {
            log.debug("First document: {}", documents.get(0).toJson());
        }
        return mapToProductResponses(documents);
    }

    private Aggregation buildAggregation(int pageNo, int pageSize, Sort sort) {
        return Aggregation.newAggregation(
                Aggregation.lookup("category", "categoryId", "categoryId", "category"),
                Aggregation.lookup("inventory", "inventoryId", "inventoryId", "inventory"),
                Aggregation.lookup("supplier", "supplierId", "supplierId", "supplier"),
                Aggregation.lookup("specification", "productId", "productId", "specifications"),
                Aggregation.unwind("category", true),
                Aggregation.unwind("inventory", true),
                Aggregation.unwind("supplier", true),
                Aggregation.sort(sort),
                Aggregation.skip((long) pageNo * pageSize),
                Aggregation.limit(pageSize)
        );
    }

    private List<ProductResponse> mapToProductResponses(List<Document> documents) {
        return documents.stream().map(doc -> {
            log.debug("Processing document: {}", doc.toJson());

            // Lấy dữ liệu Product
            Product product = new Product();
            product.setProductId(doc.getString("productId"));
            product.setName(doc.getString("name"));
            product.setCategoryId(doc.getString("categoryId"));
            product.setSupplierId(doc.getString("supplierId"));
            product.setInventoryId(doc.getString("inventoryId"));
            String colorStr = doc.getString("color");
            product.setColor(colorStr != null ? Color.valueOf(colorStr) : null);
            product.setDescription(doc.getString("description"));
            product.setImageUrls(doc.get("imageUrls", List.class));
            product.setPrice(doc.getDouble("price") != null ? doc.getDouble("price") : 0.0);
            product.setPriceReduced(doc.getDouble("priceReduced") != null ? doc.getDouble("priceReduced") : product.getPrice());
            product.setPromotionId(doc.getString("promotionId"));
            product.setCreatedAt(doc.getDate("createdAt") != null ?
                    LocalDateTime.ofInstant(doc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
            product.setUpdatedAt(doc.getDate("updatedAt") != null ?
                    LocalDateTime.ofInstant(doc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);

            // Lấy Category
            Category category = null;
            Object categoryObj = doc.get("category");
            if (categoryObj instanceof Document) {
                Document categoryDoc = (Document) categoryObj;
                category = new Category();
                category.setCategoryId(categoryDoc.getString("categoryId"));
                category.setName(categoryDoc.getString("name"));
                category.setDescription(categoryDoc.getString("description"));
                category.setCreatedAt(categoryDoc.getDate("createdAt") != null ?
                        LocalDateTime.ofInstant(categoryDoc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
                category.setUpdatedAt(categoryDoc.getDate("updatedAt") != null ?
                        LocalDateTime.ofInstant(categoryDoc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);
            } else {
                log.warn("Category is not a Document: {}", categoryObj);
            }

            // Lấy Inventory
            Inventory inventory = null;
            Object inventoryObj = doc.get("inventory");
            if (inventoryObj instanceof Document) {
                Document inventoryDoc = (Document) inventoryObj;
                inventory = new Inventory();
                inventory.setInventoryId(inventoryDoc.getString("inventoryId"));
                inventory.setProductId(inventoryDoc.getString("productId"));
                inventory.setImportDate(inventoryDoc.getDate("importDate") != null ?
                        LocalDateTime.ofInstant(inventoryDoc.getDate("importDate").toInstant(), ZoneId.systemDefault()) : null);
                inventory.setQuantity(inventoryDoc.getInteger("quantity", 0));
                inventory.setCreatedAt(inventoryDoc.getDate("createdAt") != null ?
                        LocalDateTime.ofInstant(inventoryDoc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
                inventory.setUpdatedAt(inventoryDoc.getDate("updatedAt") != null ?
                        LocalDateTime.ofInstant(inventoryDoc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);
            } else {
                log.warn("Inventory is not a Document: {}", inventoryObj);
            }

            // Lấy Supplier
            Supplier supplier = null;
            Object supplierObj = doc.get("supplier");
            if (supplierObj instanceof Document) {
                Document supplierDoc = (Document) supplierObj;
                supplier = new Supplier();
                supplier.setSupplierId(supplierDoc.getString("supplierId"));
                supplier.setName(supplierDoc.getString("name"));
                supplier.setAddressId(supplierDoc.getString("addressId"));
                supplier.setPhone(supplierDoc.getString("phone"));
                supplier.setEmail(supplierDoc.getString("email"));
                supplier.setDescription(supplierDoc.getString("description"));
                supplier.setCreatedAt(supplierDoc.getDate("createdAt") != null ?
                        LocalDateTime.ofInstant(supplierDoc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
                supplier.setUpdatedAt(supplierDoc.getDate("updatedAt") != null ?
                        LocalDateTime.ofInstant(supplierDoc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);
            } else {
                log.warn("Supplier is not a Document: {}", supplierObj);
            }

            // Lấy Specifications
            List<Specification> specifications = Collections.emptyList();
            Object specObj = doc.get("specifications");
            if (specObj instanceof List) {
                List<Document> specDocs = (List<Document>) specObj;
                specifications = specDocs.stream().map(specDoc -> {
                    Specification spec = new Specification();
                    spec.setSpecificationId(specDoc.getString("specificationId"));
                    spec.setProductId(specDoc.getString("productId"));
                    spec.setKey(specDoc.getString("key"));
                    spec.setValue(specDoc.getString("value"));
                    return spec;
                }).collect(Collectors.toList());
            } else {
                log.warn("Specifications is not a List: {}", specObj);
            }

            return ProductResponse.builder()
                    .product(product)
                    .category(category)
                    .inventory(inventory)
                    .supplier(supplier)
                    .specification(specifications)
                    .build();
        }).collect(Collectors.toList());
    }

    private long countTotalDocuments() {
        Aggregation countAggregation = Aggregation.newAggregation(
                Aggregation.lookup("category", "categoryId", "categoryId", "category"),
                Aggregation.lookup("inventory", "inventoryId", "inventoryId", "inventory"),
                Aggregation.lookup("supplier", "supplierId", "supplierId", "supplier"),
                Aggregation.lookup("specification", "productId", "productId", "specification"),
                Aggregation.unwind("category", true),
                Aggregation.unwind("inventory", true),
                Aggregation.unwind("supplier", true),
                Aggregation.count().as("total")
        );
        return mongoTemplate.aggregate(countAggregation, "product", Document.class)
                .getMappedResults()
                .stream()
                .mapToLong(doc -> doc.getInteger("total", 0))
                .findFirst()
                .orElse(0L);
    }
}
