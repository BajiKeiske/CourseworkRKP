package baji.lab1.service;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.*;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Transactional
    public Review createReview(User user, ReviewCreateDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        boolean hasPurchased = orderRepository.existsByUserAndOrderItems_Product_IdAndStatus(
                user, product.getId(), OrderStatus.COMPLETED);

        if (!hasPurchased) {
            throw new IllegalArgumentException("Отзыв можно оставить только после получения товара");
        }

        if (reviewRepository.existsByUser_IdAndProduct_Id(user.getId(), product.getId())) {
            throw new IllegalArgumentException("Вы уже оставляли отзыв на этот товар");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setText(dto.getText());
        review.setRating(dto.getRating());
        review.setStatus(ReviewStatus.PENDING);

        return reviewRepository.save(review);
    }

    public List<Review> getApprovedReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED);
    }

    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatus(ReviewStatus.PENDING);
    }

    public long countPendingReviews() {
        return reviewRepository.countByStatus(ReviewStatus.PENDING);
    }

    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());

        emailService.sendEmail(
                review.getUser().getEmail(),
                "✅ Ваш отзыв одобрен",
                "Здравствуйте, " + review.getUser().getUsername() + "!\n\n" +
                        "Ваш отзыв на товар \"" + review.getProduct().getName() + "\" опубликован.\n" +
                        "Спасибо за ваше мнение!\n\n" +
                        "С уважением, Vinyl Store"
        );
    }

    @Transactional
    public void rejectReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
        review.setStatus(ReviewStatus.REJECTED);
        review.setRejectionReason(reason);
        reviewRepository.save(review);

        emailService.sendEmail(
                review.getUser().getEmail(),
                "Ваш отзыв отклонён",
                "Здравствуйте, " + review.getUser().getUsername() + "!\n\n" +
                        "Ваш отзыв на товар \"" + review.getProduct().getName() + "\" был отклонён.\n" +
                        "Причина: " + reason + "\n\n" +
                        "Вы можете оставить новый отзыв с учётом замечаний.\n\n" +
                        "С уважением, Vinyl Store"
        );
    }

    public void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        List<Review> approvedReviews = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED);

        if (approvedReviews.isEmpty()) {
            product.setAverageRating(BigDecimal.ZERO);
            product.setReviewCount(0);
        } else {
            double sum = 0;
            for (Review r : approvedReviews) {
                sum += r.getRating();
            }
            double avg = sum / approvedReviews.size();
            product.setAverageRating(BigDecimal.valueOf(Math.round(avg * 10.0) / 10.0));
            product.setReviewCount(approvedReviews.size());
        }
        productRepository.save(product);
    }

    @Transactional
    public void deleteReview(Long reviewId, User currentUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

        if (!review.getUser().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new SecurityException("Нет прав на удаление");
        }
        reviewRepository.delete(review);
        updateProductRating(review.getProduct().getId());
    }

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
    }
}