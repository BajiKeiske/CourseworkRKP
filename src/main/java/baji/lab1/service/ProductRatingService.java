package baji.lab1.service;

import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductRatingService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void updateProductRating(Long productId) {
        // Получаем средний рейтинг и количество отзывов
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
        Long count = reviewRepository.countByProduct_Id(productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        // Обновляем поля
        product.setAverageRating(avgRating != null ?
                BigDecimal.valueOf(avgRating) : BigDecimal.ZERO);
        product.setReviewCount(count != null ? count.intValue() : 0);

        productRepository.save(product);
    }
}
