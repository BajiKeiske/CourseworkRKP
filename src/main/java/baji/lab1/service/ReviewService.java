package baji.lab1.service;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Product;
import baji.lab1.entity.Review;
import baji.lab1.entity.User;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    // UserService уже есть у тебя

    // Создать отзыв
    @Transactional
    public Review createReview(User user, ReviewCreateDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        // Проверяем, не оставлял ли уже пользователь отзыв на этот товар
        if (reviewRepository.existsByUser_IdAndProduct_Id(user.getId(), product.getId())) {
            throw new IllegalArgumentException("Вы уже оставляли отзыв на этот товар");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .text(dto.getText())
                .rating(dto.getRating())
                .build();

        return reviewRepository.save(review);
    }

    // Получить все отзывы по товару
    public List<Review> getReviewsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        return reviewRepository.findByProduct(product);
    }

    // Удалить отзыв (может автор или админ)
    @Transactional
    public void deleteReview(Long reviewId, User currentUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

        // Проверка прав: либо автор, либо админ (предполагается, что у User есть поле isAdmin или роль)
        if (!review.getUser().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new SecurityException("У вас нет прав на удаление этого отзыва");
        }

        reviewRepository.delete(review);
    }

    // Получить средний рейтинг товара
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0; // Округление до 1 знака
    }

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
    }
}