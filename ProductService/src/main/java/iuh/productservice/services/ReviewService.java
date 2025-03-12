package iuh.productservice.services;

import iuh.productservice.entities.Review;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ReviewService {
    Optional<Review> createReview(Review review);
    Optional<Review> updateReview(Review review);
    boolean deleteReview(String reviewId);
    List<Review> getAllReviewsByProductId(String productId);
}
