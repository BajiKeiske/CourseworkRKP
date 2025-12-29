package baji.lab1.controller.api;

import baji.lab1.dto.OrderCreateDto;
import baji.lab1.entity.Order;
import baji.lab1.entity.User;
import baji.lab1.entity.Product;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.UserRepository;
import baji.lab1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Создать заказ от Android
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateDto dto) {
        try {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<Product> products = new ArrayList<>();
            for (Long productId : dto.getProductIds()) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Товар не найден: " + productId));
                products.add(product);
            }

            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(BigDecimal.valueOf(dto.getTotalAmount()));
            order.setStatus("В ожидании");
            order.setProducts(products);

            orderRepository.save(order);
            return ResponseEntity.ok("Заказ создан, ID: " + order.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Получить все заказы (для админа)
    @GetMapping
    public List<Order> getAllOrders() {
        return (List<Order>) orderRepository.findAll();
    }

    // Получить заказы пользователя
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        return orderRepository.findByUserId(userId);
    }
}