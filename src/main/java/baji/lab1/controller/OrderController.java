package baji.lab1.controller;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Order;
import baji.lab1.entity.Product;
import baji.lab1.entity.User;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    // ========== ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ==========
    @PostMapping("/checkout")
    public String checkout(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        if (basket.getProducts().isEmpty()) {
            return "redirect:/basket?error=empty";
        }

        BigDecimal totalAmount = basket.getProducts().stream()
                .map(product -> BigDecimal.valueOf(product.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setProducts(new ArrayList<>(basket.getProducts()));
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("Ожидание");

        orderRepository.save(order);

        basket.getProducts().clear();
        basketRepository.save(basket);

        return "redirect:/user/orders?success=true";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет прав для отмены этого заказа");
        }

        order.setStatus("Отменен");
        orderRepository.save(order);

        return "redirect:/user/orders?cancelled=true";
    }

    // ========== ДЛЯ АДМИНИСТРАТОРОВ ==========
    @GetMapping("/admin/all")
    public String getAllOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @PostMapping("/admin/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus("В обработке");
        orderRepository.save(order);

        return "redirect:/order/admin/all";
    }

    @PostMapping("/admin/{orderId}/complete")
    public String completeOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus("Завершен");
        orderRepository.save(order);

        return "redirect:/order/admin/all";
    }

    @PostMapping("/admin/{orderId}/cancel")
    public String cancelOrderByAdmin(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus("Отменен");
        orderRepository.save(order);

        return "redirect:/order/admin/all";
    }
}