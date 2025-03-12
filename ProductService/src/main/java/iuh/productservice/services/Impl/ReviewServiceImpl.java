package iuh.productservice.services.Impl;

import iuh.productservice.entities.Review;
import iuh.productservice.repositories.ReviewRepository;
import iuh.productservice.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Optional<Review> createReview(Review review) {
        return Optional.of(reviewRepository.save(review));
    }

    @Override
    public Optional<Review> updateReview(Review review) {
        Review review1 = reviewRepository.findById(review.getReviewId()).get();
        if (review1 != null) {
            review1.setContent(review.getContent());
            review1.setRating(review.getRating());
            return Optional.of(reviewRepository.save(review1));
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        if (review != null) {
            reviewRepository.delete(review);
            return true;
        }
        return false;
    }

    @Override
    public List<Review> getAllReviewsByProductId(String productId) {
        return reviewRepository.findAllByProductId(productId);
    }
}
