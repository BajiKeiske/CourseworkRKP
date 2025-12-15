package baji.lab1.repository;

import baji.lab1.entity.Product;
import baji.lab1.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Найти все отзывы по товару
    List<Review> findByProduct(Product product);

    // Найти все отзывы пользователя
    List<Review> findByUser_Id(Long userId);

    // Проверить, оставлял ли пользователь отзыв на товар
    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    // Средний рейтинг товара (можно использовать в сервисе)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // Количество отзывов по товару
    Long countByProduct_Id(Long productId);
}