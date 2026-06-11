package baji.lab1.repository;

import baji.lab1.entity.Product;
import baji.lab1.entity.Review;
import baji.lab1.entity.ReviewStatus;
import baji.lab1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Найти все отзывы по товару
    List<Review> findByProduct(Product product);

    // Найти все отзывы пользователя
    List<Review> findByUser_Id(Long userId);

    // Проверить, оставлял ли пользователь отзыв на товар
    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    // Количество отзывов по товару
    Long countByProduct_Id(Long productId);

    // Найти отзывы по ID товара
    List<Review> findByProductId(Long productId);

    // Найти отзывы по статусу
    List<Review> findByStatus(ReviewStatus status);

    // Найти отзывы по товару и статусу
    List<Review> findByProductIdAndStatus(Long productId, ReviewStatus status);

    // Посчитать количество отзывов с определённым статусом
    long countByStatus(ReviewStatus status);

    // Посчитать одобренные отзывы по товару
    long countByProductIdAndStatus(Long productId, ReviewStatus status);

    Optional<Review> findByUserAndProduct(User user, Product product);
}