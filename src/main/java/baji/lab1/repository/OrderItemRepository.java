package baji.lab1.repository;

import baji.lab1.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Проверяет, есть ли товар хотя бы в одном заказе
    boolean existsByProductId(Long productId);
}