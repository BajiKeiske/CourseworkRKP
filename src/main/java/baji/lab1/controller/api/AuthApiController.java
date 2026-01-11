package baji.lab1.controller.api;

import baji.lab1.dto.UserRegisterDto;
import baji.lab1.dto.UserLoginDto;
import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthApiController.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Регистрация
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDto dto) {

        log.info("REGISTER REQUEST: username={}, email={}",
                dto.getUsername(), dto.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            log.warn("REGISTER FAILED: email already exists {}", dto.getEmail());

            Map<String, String> error = new HashMap<>();
            error.put("message", "Email уже используется");
            return ResponseEntity.badRequest().body(error);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(baji.lab1.entity.Role.ROLE_USER);

        userRepository.save(user);

        log.info("REGISTER SUCCESS: user id={}", user.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Пользователь создан");
        return ResponseEntity.ok(response);
    }


    // Вход
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto) {

        Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());

        if (userOpt.isEmpty() ||
                !passwordEncoder.matches(dto.getPassword(), userOpt.get().getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Неверный логин или пароль");
        }

        User user = userOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

}