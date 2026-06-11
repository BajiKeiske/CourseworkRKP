package baji.lab1.repository;

import baji.lab1.entity.Order;
import baji.lab1.entity.OrderStatus;
import baji.lab1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByOrderDateAfter(LocalDateTime date);

    long countByUser(User user);

    Order findFirstByUserOrderByOrderDateDesc(User user);

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    // Проверка, покупал ли пользователь этот товар (заказ со статусом COMPLETED)
    // Используем JPA метод: ищем заказы пользователя, у которых есть товар и статус COMPLETED
    boolean existsByUserAndOrderItems_Product_IdAndStatus(User user, Long productId, OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}