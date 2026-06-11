package baji.lab1.repository;

import baji.lab1.entity.User;
import baji.lab1.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUser(User user);

    // Поиск всех вишлистов, содержащих товар с указанным ID
    List<Wishlist> findByProducts_Id(Long productId);

    // Проверка, есть ли товар в вишлисте у пользователя
    boolean existsByUserAndProducts_Id(User user, Long productId);
}