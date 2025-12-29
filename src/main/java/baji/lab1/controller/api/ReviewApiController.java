package baji.lab1.controller.api;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Review;
import baji.lab1.entity.User;
import baji.lab1.entity.Product;
import baji.lab1.repository.ReviewRepository;
import baji.lab1.repository.UserRepository;
import baji.lab1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewCreateDto dto) {
        try {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            Review review = Review.builder()
                    .user(user)
                    .product(product)
                    .text(dto.getText())
                    .rating(dto.getRating())
                    .build(); // createdAt установится автоматически через @PrePersist

            reviewRepository.save(review);
            return ResponseEntity.ok("Отзыв добавлен");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Получить отзывы товара
    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}