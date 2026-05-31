package baji.lab1.controller;

import baji.lab1.entity.Order;
import baji.lab1.entity.User;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Профиль пользователя
    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal User currentUser,
            Model model) {

        User user = userRepository
                .findByUsername(currentUser.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Пользователь не найден"));

        List<Order> orders =
                orderRepository.findByUserOrderByOrderDateDesc(user);

        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Order order : orders) {

            if (order.getTotalAmount() != null) {

                totalSpent =
                        totalSpent.add(order.getTotalAmount());
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalSpent", totalSpent);

        return "user/profile";
    }

    // Список заказов пользователя
    @GetMapping("/orders")
    public String userOrders(
            @AuthenticationPrincipal User currentUser,
            Model model) {
        User user = userRepository
                .findByUsername(currentUser.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Пользователь не найден"));
        model.addAttribute(
                "orders",
                orderRepository.findByUserOrderByOrderDateDesc(user)
        );
        return "user/orders";
    }

    // Смена пароля
    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal User currentUser,

            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        User user = userRepository
                .findByUsername(currentUser.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Пользователь не найден"));

        // Проверка старого пароля
        if (!passwordEncoder.matches(
                oldPassword,
                user.getPassword())) {
            redirectAttributes.addFlashAttribute(
                    "passwordError",
                    "Старый пароль неверный"
            );
            return "redirect:/user/profile";
        }

        // Проверка совпадения паролей
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute(
                    "passwordError",
                    "Пароли не совпадают"
            );
            return "redirect:/user/profile";
        }

        // Проверка сложности пароля
        if (!isPasswordStrong(newPassword)) {
            redirectAttributes.addFlashAttribute(
                    "passwordError",
                    "Пароль должен содержать минимум 8 символов, большую и маленькую букву и цифру"
            );
            return "redirect:/user/profile";
        }
        // Обновление пароля
        user.setPassword(
                passwordEncoder.encode(newPassword)
        );
        userRepository.save(user);
        redirectAttributes.addFlashAttribute(
                "passwordSuccess",
                true
        );
        return "redirect:/user/profile";
    }

    // Проверка сложности пароля
    private boolean isPasswordStrong(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
        );
    }
}