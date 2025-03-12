package iuh.productservice.repositories;

import iuh.productservice.entities.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    public List<Review> findAllByProductId(String productId);
}
