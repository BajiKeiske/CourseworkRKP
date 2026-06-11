package baji.lab1.controller;

import baji.lab1.entity.Order;
import baji.lab1.entity.User;
import baji.lab1.entity.Wishlist;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.UserRepository;
import baji.lab1.service.WishlistService;
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

    @Autowired
    private WishlistService wishlistService;

    // Профиль пользователя
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User currentUser, Model model) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        BigDecimal totalSpent = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                totalSpent = totalSpent.add(order.getTotalAmount());
            }
        }
        Wishlist wishlist = wishlistService.getWishlist(user);

        model.addAttribute("user", user);
        model.addAttribute("products", wishlist.getProducts());
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

    // Страница редактирования профиля
    @GetMapping("/profile/edit")
    public String editProfile(@AuthenticationPrincipal User currentUser, Model model) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    // Обновление данных профиля
    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Данные обновлены");
        return "redirect:/user/profile/edit";
    }

    // Смена пароля
    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal User currentUser,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Старый пароль неверный");
            return "redirect:/user/profile/edit";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароли не совпадают");
            return "redirect:/user/profile/edit";
        }

        if (!isPasswordStrong(newPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароль должен содержать минимум 8 символов, заглавную и строчную букву, цифру");
            return "redirect:/user/profile/edit";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно изменён");
        return "redirect:/user/profile/edit";
    }

    // Проверка сложности пароля
    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    }
}