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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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

    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                               Authentication authentication) throws IOException {

        if (file.isEmpty()) {
            return "redirect:/user/profile?error=empty";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Имя файла
        String originalName = file.getOriginalFilename();
        String extension = "";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = "avatar_" + username + "_" + System.currentTimeMillis() + extension;

        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/images/avatars/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Сохраняем файл
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        // Обновляем пользователя
        user.setAvatarUrl("/images/avatars/" + fileName);
        userRepository.save(user);

        return "redirect:/user/profile?success=true";
    }

}