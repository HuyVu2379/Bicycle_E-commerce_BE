package iuh.productservice.services;

import iuh.productservice.entities.Review;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ReviewService {
    Optional<Review> createReview(Review review);
}
