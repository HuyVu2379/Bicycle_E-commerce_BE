package iuh.productservice.services.Impl;

import iuh.productservice.client.OrderServiceClient;
import iuh.productservice.dtos.responses.ProductResponse;
import iuh.productservice.dtos.responses.PromotionResponse;
import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.entities.*;
import iuh.productservice.enums.Color;
import iuh.productservice.mappers.ProductMapper;
import iuh.productservice.repositories.ProductRepository;
import iuh.productservice.services.InventoryService;
import iuh.productservice.services.ProductService;
import iuh.productservice.services.SpecificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
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
    private InventoryService inventoryService;
    @Autowired
    private SpecificationService specificationService;
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
                    double discount = product.getPrice() * (1 - Double.parseDouble(String.valueOf(promotionRequest.getData().getReducePercent())) / 100);
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

    @Override
    public Optional<ProductResponse> getProductById(String productId) {
        ProductResponse productResponse = new ProductResponse();
        Product product = productRepository.findById(productId).orElse(null);
        Category category = categoryService.getCategoryById(product.getCategoryId()).orElse(null);
        Supplier supplier = supplierService.getSupplierById(product.getSupplierId()).orElse(null);
        List<Inventory> inventories = inventoryService.getAllInventoryByProductId(productId);
        List<Specification> specifications = specificationService.findSpecificationsByProductId(productId);
        productResponse.setProduct(product);
        productResponse.setCategory(category);
        productResponse.setSupplier(supplier);
        productResponse.setInventory(inventories);
        productResponse.setSpecification(specifications);
        return Optional.of(productResponse);
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
        return mapToProductResponses(documents);
    }

    private Aggregation buildAggregation(int pageNo, int pageSize, Sort sort) {
        return Aggregation.newAggregation(
                Aggregation.project()
                        .and(ConvertOperators.ToObjectId.toObjectId("$categoryId")).as("categoryIdObj")
                        .and(ConvertOperators.ToObjectId.toObjectId("$inventoryId")).as("inventoryIdObj")
                        .and(ConvertOperators.ToObjectId.toObjectId("$supplierId")).as("supplierIdObj")
                        .andInclude("_id", "productId", "name", "categoryId", "supplierId", "inventoryId", "description", "price", "priceReduced",
                                "promotionId", "createdAt", "updatedAt"),
                Aggregation.lookup("category", "categoryIdObj", "_id", "category"),
                Aggregation.lookup("inventory", "inventoryIdObj", "_id", "inventory"),
                Aggregation.lookup("supplier", "supplierIdObj", "_id", "supplier"),
                Aggregation.lookup("specification", "_id", "productId", "specifications"),

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
            log.warn("Document in mapToProduct: {}", doc);
            Product product = new Product();
            product.setProductId(doc.getObjectId("_id").toString());
            product.setName(doc.getString("name"));
            product.setCategoryId(doc.getString("categoryId"));
            product.setSupplierId(doc.getString("supplierId"));
            product.setDescription(doc.getString("description"));
            product.setPrice(doc.getDouble("price") != null ? doc.getDouble("price") : 0.0);
            product.setPriceReduced(doc.getDouble("priceReduced") != null ? doc.getDouble("priceReduced") : product.getPrice());
            product.setPromotionId(doc.getString("promotionId"));
            product.setCreatedAt(doc.getDate("createdAt") != null ?
                    LocalDateTime.ofInstant(doc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
            product.setUpdatedAt(doc.getDate("updatedAt") != null ?
                    LocalDateTime.ofInstant(doc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);

            // Lấy Category
            Category category = null;
            if (doc.get("category") instanceof Document) {
                Document categoryDoc = (Document) doc.get("category");
                category = new Category();
                // Đây là điểm quan trọng - lấy _id từ subdocument và gán vào categoryId
                category.setCategoryId(categoryDoc.getObjectId("_id").toString());
                category.setName(categoryDoc.getString("name"));
                category.setDescription(categoryDoc.getString("description"));

                if (categoryDoc.get("createdAt") != null) {
                    if (categoryDoc.get("createdAt") instanceof Date) {
                        category.setCreatedAt(LocalDateTime.ofInstant(((Date) categoryDoc.get("createdAt")).toInstant(), ZoneId.systemDefault()));
                    }
                }

                if (categoryDoc.get("updatedAt") != null) {
                    if (categoryDoc.get("updatedAt") instanceof Date) {
                        category.setUpdatedAt(LocalDateTime.ofInstant(((Date) categoryDoc.get("updatedAt")).toInstant(), ZoneId.systemDefault()));
                    }
                }
            }

            // Lấy Inventory
            List<Inventory> inventory = new ArrayList<>();
            if (doc.get("inventory") instanceof Document) {
                List<Document> inventoryDocs = (List<Document>) doc.get("inventory");
                inventory = inventoryDocs.stream().map(inventoryDoc -> {
                    Inventory inven = new Inventory();
                    if (inventoryDoc.get("_id") != null) {
                        inven.setInventoryId(inventoryDoc.getObjectId("_id").toString());
                    }
                    inven.setInventoryId(inventoryDoc.getObjectId("_id").toString());
                    inven.setProductId(inventoryDoc.getString("productId"));
                    inven.setQuantity(inventoryDoc.getInteger("quantity", 0));
                    inven.setImageUrls(Collections.singletonList(inventoryDoc.getString("imageUrls")));
                    inven.setColor(Color.valueOf(inventoryDoc.getString("color")));
                    if (inventoryDoc.get("importDate") != null && inventoryDoc.get("importDate") instanceof Date) {
                        inven.setImportDate(((Date) inventoryDoc.get("importDate")).toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay());
                    }

                    if (inventoryDoc.get("createdAt") != null && inventoryDoc.get("createdAt") instanceof Date) {
                        inven.setCreatedAt(LocalDateTime.ofInstant(((Date) inventoryDoc.get("createdAt")).toInstant(), ZoneId.systemDefault()));
                    }

                    if (inventoryDoc.get("updatedAt") != null && inventoryDoc.get("updatedAt") instanceof Date) {
                        inven.setUpdatedAt(LocalDateTime.ofInstant(((Date) inventoryDoc.get("updatedAt")).toInstant(), ZoneId.systemDefault()));
                    }
                    return inven;
                }).collect(Collectors.toList());
            }

            // Lấy Supplier
            Supplier supplier = null;
            if (doc.get("supplier") instanceof Document) {
                Document supplierDoc = (Document) doc.get("supplier");
                supplier = new Supplier();
                // Lấy _id từ subdocument supplier
                supplier.setSupplierId(supplierDoc.getObjectId("_id").toString());
                supplier.setName(supplierDoc.getString("name"));
                supplier.setAddressId(supplierDoc.getString("addressId"));
                supplier.setPhone(supplierDoc.getString("phone"));
                supplier.setEmail(supplierDoc.getString("email"));
                supplier.setDescription(supplierDoc.getString("description"));

                if (supplierDoc.get("createdAt") != null && supplierDoc.get("createdAt") instanceof Date) {
                    supplier.setCreatedAt(LocalDateTime.ofInstant(((Date) supplierDoc.get("createdAt")).toInstant(), ZoneId.systemDefault()));
                }

                if (supplierDoc.get("updatedAt") != null && supplierDoc.get("updatedAt") instanceof Date) {
                    supplier.setUpdatedAt(LocalDateTime.ofInstant(((Date) supplierDoc.get("updatedAt")).toInstant(), ZoneId.systemDefault()));
                }
            }

            // Lấy Specifications
            List<Specification> specifications = new ArrayList<>();
            if (doc.get("specifications") instanceof List) {
                List<Document> specDocs = (List<Document>) doc.get("specifications");
                specifications = specDocs.stream().map(specDoc -> {
                    Specification spec = new Specification();
                    if (specDoc.get("_id") != null) {
                        spec.setSpecificationId(specDoc.getObjectId("_id").toString());
                    }
                    spec.setProductId(specDoc.getString("productId"));
                    spec.setKey(specDoc.getString("key"));
                    spec.setValue(specDoc.getString("value"));
                    return spec;
                }).collect(Collectors.toList());
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
                Aggregation.group("productId")
                        .count().as("total")
        );

        return mongoTemplate.aggregate(countAggregation, "product", Document.class)
                .getMappedResults()
                .stream()
                .mapToInt(doc -> doc.getInteger("total"))
                .findFirst()
                .orElse(0);
    }
}
