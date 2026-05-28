package baji.lab1.controller;

import baji.lab1.entity.Role;
import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import baji.lab1.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // главная
    @GetMapping("/")
    public String home() {
        return "redirect:/user/products/catalog";
    }

    // вход
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // регистрация
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            HttpSession session) {

        // пользователь уже существует
        if (userRepository.findByUsername(username).isPresent()) {

            model.addAttribute("error",
                    "Пользователь с таким логином уже существует");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }

        // email уже существует
        if (userRepository.findByEmail(email).isPresent()) {

            model.addAttribute("error",
                    "Пользователь с таким email уже существует");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }

        // пароли не совпадают
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error",
                    "Пароли не совпадают");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }

        // слабый пароль
        if (!isPasswordStrong(password)) {
            model.addAttribute("error",
                    "Пароль должен содержать минимум 8 символов, большую и маленькую букву и цифру");

            model.addAttribute("username", username);
            model.addAttribute("email", email);

            return "register";
        }

        // генерация кода
        String verificationCode = generateVerificationCode();

        // сохраняем данные временно в session
        session.setAttribute("register_username", username);

        session.setAttribute("register_email", email);

        session.setAttribute(
                "register_password",
                passwordEncoder.encode(password)
        );

        session.setAttribute(
                "register_code",
                verificationCode
        );

        session.setAttribute(
                "register_time",
                System.currentTimeMillis()
        );

        // отправка email
        try {

            String text =
                    "Здравствуйте, " + username + "!\n\n" +
                            "Ваш код подтверждения:\n\n" +
                            verificationCode + "\n\n" +
                            "Код действует 5 минут.";

            emailService.sendEmail(
                    email,
                    "Подтверждение регистрации Vinyl",
                    text
            );

        } catch (Exception e) {

            model.addAttribute("error",
                    "Не удалось отправить письмо");

            return "register";
        }

        return "redirect:/verify?email=" + email;
    }

    // подтверждение почты

    @GetMapping("/verify")
    public String verifyPage(
            @RequestParam String email,
            Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify")
    public String verifyCode(
            @RequestParam String email,
            @RequestParam String code,
            Model model,
            HttpSession session) {
        String sessionCode =
                (String) session.getAttribute("register_code");

        Long sessionTime =
                (Long) session.getAttribute("register_time");

        // session исчезла
        if (sessionCode == null || sessionTime == null) {

            model.addAttribute(
                    "error",
                    "Код подтверждения истёк"
            );

            model.addAttribute("email", email);
            return "verify";
        }

        // прошло больше 10 минут
        long currentTime = System.currentTimeMillis();
        long tenMinutes  = 10 * 60 * 1000;

        if (currentTime - sessionTime > tenMinutes ) {

            session.invalidate();

            model.addAttribute(
                    "error",
                    "Время подтверждения истекло"
            );

            model.addAttribute("email", email);

            return "verify";
        }

        // неправильный код
        if (!code.equals(sessionCode)) {

            model.addAttribute(
                    "error",
                    "Неверный код подтверждения"
            );

            model.addAttribute("email", email);

            return "verify";
        }

        // создание пользователя
        User user = new User();

        user.setUsername(
                (String) session.getAttribute("register_username")
        );

        user.setEmail(
                (String) session.getAttribute("register_email")
        );

        user.setPassword(
                (String) session.getAttribute("register_password")
        );

        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        // очищаем session
        session.invalidate();

        return "redirect:/login?verified=true";
    }

    private boolean isPasswordStrong(String password) {

        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$"
        );
    }

    private String generateVerificationCode() {

        Random random = new Random();

        int code =
                100000 + random.nextInt(900000);

        return String.valueOf(code);
    }

    //прислать код еще раз
    @PostMapping("/resend-code")
    @ResponseBody
    public String resendCode(@RequestParam String email, HttpSession session) {
        // проверяем, есть ли сессия с этим email
        String sessionEmail = (String) session.getAttribute("register_email");
        if (sessionEmail == null || !sessionEmail.equals(email)) {
            return "error";
        }

        // новый код
        String newCode = generateVerificationCode();

        // обновляем сессию
        session.setAttribute("register_code", newCode);
        session.setAttribute("register_time", System.currentTimeMillis());

        // отправляем письмо
        String username = (String) session.getAttribute("register_username");
        String text = "Здравствуйте, " + username + "!\n\nВаш новый код подтверждения:\n\n" + newCode + "\n\nКод действует 5 минут.";

        try {
            emailService.sendEmail(email, "Новый код подтверждения Vinyl", text);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}