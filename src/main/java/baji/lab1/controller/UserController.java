package baji.lab1.controller;

import baji.lab1.entity.Order;
import baji.lab1.entity.User;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Профиль пользователя
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User currentUser, Model model) {
        // Получаем полного пользователя из БД
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Получаем заказы пользователя
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);

        // Рассчитываем общую сумму
        BigDecimal totalSpent = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                totalSpent = totalSpent.add(order.getTotalAmount());
            }
        }

        // Добавляем атрибуты
        model.addAttribute("user", user);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalSpent", totalSpent);

        return "user/profile";
    }

    // Список заказов пользователя
    @GetMapping("/orders")
    public String userOrders(@AuthenticationPrincipal User currentUser, Model model) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("orders", orderRepository.findByUserOrderByOrderDateDesc(user));
        return "user/orders";
    }
}