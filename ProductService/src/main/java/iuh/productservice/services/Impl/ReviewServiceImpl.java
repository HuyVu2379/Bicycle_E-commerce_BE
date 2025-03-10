package iuh.productservice.services.Impl;

import iuh.productservice.entities.Review;
import iuh.productservice.repositories.ReviewRepository;
import iuh.productservice.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Override
    public Optional<Review> createReview(Review review) {
        return Optional.of(reviewRepository.save(review));
    }
}
