package baji.lab1.repository;

import baji.lab1.entity.Basket;
import baji.lab1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Long> {
    Optional<Basket> findByUser(User user);
    Optional<Basket> findByUserId(Long userId);
}