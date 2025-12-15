package baji.lab1.repository;

import baji.lab1.entity.Order;
import baji.lab1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByOrderDateDesc(User user);

    // НОВЫЙ МЕТОД для админа: все заказы по дате (сначала новые)
    List<Order> findAllByOrderByOrderDateDesc();
}