package baji.lab1.repository;

import baji.lab1.entity.Order;
import baji.lab1.entity.OrderStatus;
import baji.lab1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findByOrderDateAfter(LocalDateTime date);


    long countByUser(User user);
    Order findFirstByUserOrderByOrderDateDesc(User user);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);

    // поиск заказов по статусу
    List<Order> findByStatus(String status);
}