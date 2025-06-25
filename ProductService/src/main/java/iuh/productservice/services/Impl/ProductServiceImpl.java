package iuh.productservice.services.Impl;

import iuh.productservice.client.OrderServiceClient;
import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.ProductResponse;
import iuh.productservice.dtos.responses.ProductResponseAtHome;
import iuh.productservice.dtos.responses.PromotionResponse;
import iuh.productservice.entities.*;
import iuh.productservice.enums.Color;
import iuh.productservice.mappers.ProductMapper;
import iuh.productservice.repositories.InventoryRepository;
import iuh.productservice.repositories.ProductRepository;
import iuh.productservice.services.InventoryService;
import iuh.productservice.services.ProductService;
import iuh.productservice.services.SpecificationService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
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
    @Autowired
    private InventoryRepository inventoryRepository;

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
        if (product != null && (product.getPriceReduced() < product.getPrice())) {
            return product.getPriceReduced();
        }
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
        product1.setCategoryIds(product.getCategoryIds());
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
    public Page<ProductResponseAtHome> getProductsWithPagination(int pageNo, int pageSize, String sortBy, String sortDirection) {
        validateInputs(pageNo, pageSize, sortBy, sortDirection);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponseAtHome> productResponses = productPage.getContent().stream().map(
                product -> {
                    if (product.getPrice() <= product.getPriceReduced()) {
                        return null;
                    }

                    Optional<Inventory> inventoryOptional = inventoryRepository.findFirstByProductId((product.getProductId()));

                    String imageUrl = inventoryOptional
                            .map(inventory -> {
                                List<String> imageUrls = inventory.getImageUrls();
                                if (imageUrls != null && !imageUrls.isEmpty()) {
                                    return imageUrls.get(0);
                                }
                                return null;
                            }).orElse(null);

                    return ProductResponseAtHome.builder()
                            .productId(product.getProductId())
                            .productName(product.getName())
                            .price(product.getPrice())
                            .priceReduced(product.getPriceReduced())
                            .image(imageUrl)
                            .build();
                }).filter(Objects::nonNull).collect(Collectors.toList());

        Long total = productRepository.countByPriceGreaterThanPriceReduced();

        long totalCount = total != null ? total : 0L;
        return new PageImpl<>(productResponses, pageable, totalCount);
    }

    @Override
    public Optional<ProductResponse> getProductById(String productId) {
        ProductResponse productResponse = new ProductResponse();
        Product product = productRepository.findById(productId).orElse(null);
        List<Category> categories = new ArrayList<>();
        for (String categoryId : product.getCategoryIds()) {
            categories.add(categoryService.getCategoryById(categoryId).orElse(null));
        }
        Supplier supplier = supplierService.getSupplierById(product.getSupplierId()).orElse(null);
        List<Inventory> inventories = inventoryService.getAllInventoryByProductId(productId);
        List<Specification> specifications = specificationService.findSpecificationsByProductId(productId);
        productResponse.setProduct(product);
        productResponse.setCategory(categories);
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
            mongoSortField = "totalQuantity";
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
                        .and(ConvertOperators.ToObjectId.toObjectId("$supplierId")).as("supplierIdObj")
                        .and(context -> new Document("$toString", "$_id")).as("productIdStr")
                        .andInclude("_id", "productId", "name", "categoryIds", "supplierId", "description", "price", "priceReduced",
                                "promotionId", "createdAt", "updatedAt"),
//                Aggregation.lookup("category", "categoryIdObj", "_id", "category"),
                Aggregation.lookup("supplier", "supplierIdObj", "_id", "supplier"),
                Aggregation.lookup("inventory", "productIdStr", "productId", "inventory"),
                Aggregation.lookup("specification", "productIdStr", "productId", "specifications"),
                new AggregationOperation() {
                    @Override
                    public Document toDocument(AggregationOperationContext context) {
                        return new Document("$addFields", new Document()
                                .append("totalQuantity", new Document("$sum", "$inventory.quantity"))
                        );
                    }
                },

                Aggregation.unwind("category", true),
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
            product.setCategoryIds(doc.getList("categoryIds", String.class).toArray(new String[0]));
            product.setSupplierId(doc.getString("supplierId"));
            product.setDescription(doc.getString("description"));
            product.setPrice(doc.getDouble("price") != null ? doc.getDouble("price") : 0.0);
            product.setPriceReduced(doc.getDouble("priceReduced") != null ? doc.getDouble("priceReduced") : product.getPrice());
            product.setPromotionId(doc.getString("promotionId"));
            product.setCreatedAt(doc.getDate("createdAt") != null ?
                    LocalDateTime.ofInstant(doc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
            product.setUpdatedAt(doc.getDate("updatedAt") != null ?
                    LocalDateTime.ofInstant(doc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);

            List<Category> categories = new ArrayList<>();
            Object categoryIds = doc.get("categoryIds");
            if (categoryIds instanceof List<?>) {
                for (Object categoryDoc : (List<?>) categoryIds) {
                    if (categoryDoc instanceof Document) {
                        String categoryId = ((Document) categoryDoc).getObjectId("_id").toString();
                        categoryService.getCategoryById(categoryId).ifPresent(categories::add);
                    }
                }
            }
            List<Inventory> inventories = new ArrayList<>();
            if (doc.get("inventory") instanceof List) {
                List<Document> inventoryDocs = (List<Document>) doc.get("inventory");
                inventories = inventoryDocs.stream().map(inventoryDoc -> {
                    Inventory inven = new Inventory();
                    inven.setInventoryId(inventoryDoc.getObjectId("_id").toString());
                    inven.setProductId(inventoryDoc.getString("productId"));
                    inven.setQuantity(inventoryDoc.getInteger("quantity", 0));
                    inven.setImageUrls((List<String>) inventoryDoc.get("imageUrls"));
                    inven.setColor(Color.valueOf(inventoryDoc.getString("color")));
                    inven.setImportDate(inventoryDoc.getDate("importDate") != null ?
                            inventoryDoc.getDate("importDate").toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay() : null);
                    inven.setCreatedAt(inventoryDoc.getDate("createdAt") != null ?
                            LocalDateTime.ofInstant(inventoryDoc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
                    inven.setUpdatedAt(inventoryDoc.getDate("updatedAt") != null ?
                            LocalDateTime.ofInstant(inventoryDoc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);
                    return inven;
                }).collect(Collectors.toList());
            }

            List<Specification> specifications = new ArrayList<>();
            if (doc.get("specifications") instanceof List) {
                List<Document> specDocs = (List<Document>) doc.get("specifications");
                specifications = specDocs.stream().map(specDoc -> {
                    Specification spec = new Specification();
                    spec.setSpecificationId(specDoc.getObjectId("_id").toString());
                    spec.setProductId(specDoc.getString("productId"));
                    spec.setKey(specDoc.getString("key"));
                    spec.setValue(specDoc.getString("value"));
                    return spec;
                }).collect(Collectors.toList());
            }

            Supplier supplier = null;
            if (doc.get("supplier") instanceof Document) {
                Document supplierDoc = (Document) doc.get("supplier");
                supplier = new Supplier();
                supplier.setSupplierId(supplierDoc.getObjectId("_id").toString());
                supplier.setName(supplierDoc.getString("name"));
                supplier.setAddressId(supplierDoc.getString("addressId"));
                supplier.setPhone(supplierDoc.getString("phone"));
                supplier.setEmail(supplierDoc.getString("email"));
                supplier.setDescription(supplierDoc.getString("description"));
                supplier.setCreatedAt(supplierDoc.getDate("createdAt") != null ?
                        LocalDateTime.ofInstant(supplierDoc.getDate("createdAt").toInstant(), ZoneId.systemDefault()) : null);
                supplier.setUpdatedAt(supplierDoc.getDate("updatedAt") != null ?
                        LocalDateTime.ofInstant(supplierDoc.getDate("updatedAt").toInstant(), ZoneId.systemDefault()) : null);
            }

            return ProductResponse.builder()
                    .product(product)
                    .category(categories)
                    .inventory(inventories)
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
