package baji.lab1.controller;

import baji.lab1.entity.User;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Профиль пользователя
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("orders", orderRepository.findByUserOrderByOrderDateDesc(user));

        model.addAttribute("user", user);
        return "user/profile";
    }

    // Список заказов пользователя
    @GetMapping("/orders")
    public String userOrders(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("orders", orderRepository.findByUserOrderByOrderDateDesc(user));
        return "user/orders";
    }

}