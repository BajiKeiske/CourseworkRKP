package baji.lab1.controller;

import baji.lab1.entity.Role;
import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // страница входа
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // страница регистрации
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    // обработка регистрации
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        // проверка на существование пользователя
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            return "register";
        }

        // проверка email
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "register";
        }

        // Создание нового пользователя
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        return "redirect:/login?registered=true";
    }
}