package baji.lab1.controller.api;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.dto.ReviewDto;
import baji.lab1.entity.Product;
import baji.lab1.entity.Review;
import baji.lab1.entity.User;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.ReviewRepository;
import baji.lab1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewApiController(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // СОЗДАНИЕ ОТЗЫВА
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewCreateDto dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // если отзыв уже есть — обновляем
        if (reviewRepository.existsByUser_IdAndProduct_Id(user.getId(), product.getId())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Отзыв уже существует"));
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .text(dto.getText())
                .rating(dto.getRating())
                .build();

        reviewRepository.save(review);

        return ResponseEntity.ok(Map.of("message", "Отзыв добавлен"));
    }
    
    // ПОЛУЧИТЬ ОТЗЫВЫ ТОВАРА
    @GetMapping("/product/{productId}")
    public List<ReviewDto> getReviewsByProduct(@PathVariable Long productId) {

        return reviewRepository.findByProductId(productId)
                .stream()
                .map(review -> ReviewDto.builder()
                        .id(review.getId())
                        .userId(review.getUser().getId())
                        .productId(review.getProduct().getId())
                        .text(review.getText())
                        .rating(review.getRating())
                        .createdAt(review.getCreatedAt().toString())
                        .username(review.getUser().getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    // УДАЛЕНИЕ ОТЗЫВА
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        if (!review.getUser().getId().equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Нельзя удалить чужой отзыв"));
        }

        reviewRepository.delete(review);
        return ResponseEntity.ok(Map.of("message", "Отзыв удалён"));
    }

    // ОБНОВЛЕНИЕ ОТЗЫВА
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId,
            @RequestBody ReviewCreateDto dto
    ) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        if (!review.getUser().getId().equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Нельзя редактировать чужой отзыв"));
        }

        review.setText(dto.getText());
        review.setRating(dto.getRating());

        reviewRepository.save(review);
        return ResponseEntity.ok(Map.of("message", "Отзыв обновлён"));
    }
}
