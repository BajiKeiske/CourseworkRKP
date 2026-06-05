//package baji.lab1.controller.api;
//
//import baji.lab1.dto.OrderCreateDto;
//import baji.lab1.entity.*;
//import baji.lab1.repository.OrderRepository;
//import baji.lab1.repository.UserRepository;
//import baji.lab1.repository.ProductRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/orders")
//public class OrderApiController {
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    // ================= СОЗДАНИЕ ЗАКАЗА =================
//    @PostMapping
//    public ResponseEntity<?> createOrder(@RequestBody OrderCreateDto dto) {
//
//        try {
//            // ищем пользователя
//            User user = userRepository.findById(dto.getUserId())
//                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
//
//            // собираем список товаров с количеством
//            List<OrderItem> orderItems = new ArrayList<>();
//            BigDecimal total = BigDecimal.ZERO;
//
//            for (int i = 0; i < dto.getProductIds().size(); i++) {
//                Long productId = dto.getProductIds().get(i);
//                Integer quantity = dto.getQuantities() != null && i < dto.getQuantities().size()
//                        ? dto.getQuantities().get(i) : 1;
//
//                Product product = productRepository.findById(productId)
//                        .orElseThrow(() -> new RuntimeException("Товар не найден: " + productId));
//
//                // проверка наличия
//                if (product.getStock() < quantity) {
//                    return ResponseEntity.badRequest().body("Недостаточно товара: " + product.getName());
//                }
//
//                // создаём элемент заказа
//                OrderItem item = new OrderItem();
//                item.setProduct(product);
//                item.setQuantity(quantity);
//                item.setPriceAtTime(BigDecimal.valueOf(product.getPrice()));
//                orderItems.add(item);
//
//                // считаем сумму
//                total = total.add(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity)));
//
//                // списываем со склада
//                product.setStock(product.getStock() - quantity);
//                productRepository.save(product);
//            }
//
//            // создаём заказ
//            Order order = new Order();
//            order.setUser(user);
//            order.setOrderDate(LocalDateTime.now());
//            order.setTotalAmount(total);
//            order.setStatus(OrderStatus.NEW);
//            order.setOrderItems(orderItems);
//
//            // связываем order c orderItems
//            for (OrderItem item : orderItems) {
//                item.setOrder(order);
//            }
//
//            orderRepository.save(order);
//
//            return ResponseEntity.ok("Заказ создан, ID: " + order.getId());
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    // ================= ВСЕ ЗАКАЗЫ =================
//    @GetMapping
//    public List<Order> getAllOrders() {
//        return orderRepository.findAll();
//    }
//
//    // ================= ЗАКАЗЫ ПОЛЬЗОВАТЕЛЯ =================
//    @GetMapping("/user/{userId}")
//    public List<Order> getOrdersByUser(@PathVariable Long userId) {
//        return orderRepository.findByUserId(userId);
//    }
//}