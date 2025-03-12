package iuh.productservice.controllers;

import iuh.productservice.dtos.responses.MessageResponse;
import iuh.productservice.dtos.responses.SuccessEntityResponse;
import iuh.productservice.entities.Review;
import iuh.productservice.services.Impl.ReviewServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewServiceImpl reviewService;

    public ReviewController(ReviewServiceImpl reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageResponse<Review>> createReview(@RequestBody Review review) {
        Optional<Review> reviewResponse = reviewService.createReview(review);
        if (reviewResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Review creation failed", false, null));
        }
        return SuccessEntityResponse.created("Review created successfully", reviewResponse.get());
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse<Review>> updateReview(@RequestBody Review review) {
        Optional<Review> reviewResponse = reviewService.updateReview(review);
        if (reviewResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Review update failed", false, null));
        }
        return SuccessEntityResponse.ok("Review updated successfully", reviewResponse.get());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse<Boolean>> deleteReview(@PathVariable String id) {
        boolean reviewResponse = reviewService.deleteReview(id);
        if (!reviewResponse) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Review deletion failed", false, null));
        }
        return SuccessEntityResponse.ok("Review deleted successfully", reviewResponse);
    }

    @GetMapping("/public/all")
    public ResponseEntity<MessageResponse<Iterable<Review>>> getAllReviewsByProductId(@RequestParam String productId) {
        return SuccessEntityResponse.ok("All reviews retrieved successfully", reviewService.getAllReviewsByProductId(productId));
    }
}
