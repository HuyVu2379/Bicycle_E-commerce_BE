package iuh.productservice.repositories;

import iuh.productservice.entities.Product;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    int deleteByProductId(String productId);

    List<Product> getProductsByCategoryId(String categoryId);

    List<Product> getProductsBySupplierId(String supplierId);

    @Query("{ 'name' : { $regex: ?0, $options: 'i' } }")
    List<Product> getProductsByName(String name);

    @Aggregation(pipeline = {
        "{ $match: { $expr: { $gt: [ '$price', '$priceReduced' ] } } }",
        "{ $count: 'total' }"
    })
    Long countByPriceGreaterThanPriceReduced();
}
