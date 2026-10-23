package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;

    public ReviewController(ReviewRepository reviewRepository, CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{storeId}/{productId}")
    public Map<String, Object> getReviews(@PathVariable Long storeId, @PathVariable Long productId) {
        List<Map<String, Object>> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId)
                .stream()
                .map(this::reviewResponse)
                .toList();
        return Map.of("reviews", reviews);
    }

    private Map<String, Object> reviewResponse(Review review) {
        String customerName = customerRepository.findById(review.getCustomerId())
                .map(Customer::getName)
                .orElse("Unknown");
        String comment = review.getComment() == null ? "" : review.getComment();
        return Map.of(
                "comment", comment,
                "review", comment,
                "rating", review.getRating(),
                "customerName", customerName);
    }
}
