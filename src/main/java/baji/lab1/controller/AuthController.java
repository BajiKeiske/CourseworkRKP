package baji.lab1.controller;

import baji.lab1.entity.Role;
import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import baji.lab1.service.CategoryService;
import baji.lab1.service.EmailService;
import baji.lab1.service.ProductService;
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

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // главная
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newProducts", productService.getNewProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("rootCategories", categoryService.getCategoryTree());
        return "index";
    }

    @GetMapping("/catalog")
    public String catalog() {
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
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            HttpSession session) {

        // пользователь уже существует
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "register";
        }

        // email уже существует
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "register";
        }

        // пароли не совпадают
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "register";
        }

        // слабый пароль
        if (!isPasswordStrong(password)) {
            model.addAttribute("error", "Пароль должен содержать минимум 8 символов, большую и маленькую букву и цифру");
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "register";
        }

        // генерация кода
        String verificationCode = generateVerificationCode();

        // сохраняем данные временно в session (теперь и телефон)
        session.setAttribute("register_fullName", fullName);
        session.setAttribute("register_username", username);
        session.setAttribute("register_email", email);
        session.setAttribute("register_phone", phone);
        session.setAttribute("register_password", passwordEncoder.encode(password));
        session.setAttribute("register_code", verificationCode);
        session.setAttribute("register_time", System.currentTimeMillis());

        // отправка email
        try {
            String text = "Здравствуйте, " + username + "!\n\n" +
                    "Ваш код подтверждения:\n\n" +
                    verificationCode + "\n\n" +
                    "Код действует 5 минут.";
            emailService.sendEmail(email, "Подтверждение регистрации Vinyl", text);
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось отправить письмо");
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
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
        user.setFullName(
                (String) session.getAttribute("register_fullName")
        );
        user.setUsername(
                (String) session.getAttribute("register_username")
        );
        user.setEmail((String) session.getAttribute("register_email"));
        user.setPhone((String) session.getAttribute("register_phone"));
        user.setPassword((String) session.getAttribute("register_password"));
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

    @PostMapping("/resend-code")
    @ResponseBody
    public String resendCode(
            @RequestParam String email,
            HttpSession session) {

        // проверяем session
        String sessionEmail =
                (String) session.getAttribute("register_email");

        if (sessionEmail == null ||
                !sessionEmail.equals(email)) {

            return "error";
        }

        // cooldown 60 секунд
        Long lastResendTime =
                (Long) session.getAttribute("last_resend_time");

        if (lastResendTime != null) {

            long seconds =
                    (System.currentTimeMillis() - lastResendTime) / 1000;

            if (seconds < 60) {

                return "Подождите 60 секунд перед повторной отправкой";
            }
        }

        // новый код
        String newCode = generateVerificationCode();

        // обновляем session
        session.setAttribute("register_code", newCode);

        session.setAttribute(
                "register_time",
                System.currentTimeMillis()
        );

        session.setAttribute(
                "last_resend_time",
                System.currentTimeMillis()
        );

        // отправляем письмо
        String username =
                (String) session.getAttribute("register_username");

        String text =
                "Здравствуйте, " + username +
                        "!\n\nВаш новый код подтверждения:\n\n" +
                        newCode +
                        "\n\nКод действует 10 минут.";

        try {

            emailService.sendEmail(
                    email,
                    "Новый код подтверждения Vinyl",
                    text
            );

            return "success";

        } catch (Exception e) {

            return "error";
        }
    }

    //забыли пароль
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {

        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(

            @RequestParam String email,
            HttpSession session,
            Model model) {

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {

            model.addAttribute(
                    "error",
                    "Пользователь не найден"
            );

            return "forgot-password";
        }
        String code =
                generateVerificationCode();
        session.setAttribute(
                "reset_email",
                email
        );
        session.setAttribute(
                "reset_code",
                code
        );
        session.setAttribute(
                "reset_time",
                System.currentTimeMillis()
        );
        try {
            String text =
                    "Ваш код восстановления пароля:\n\n"
                            + code +
                            "\n\nКод действует 10 минут.";
            emailService.sendEmail(
                    email,
                    "Восстановление пароля Vinyl",
                    text
            );
        } catch (Exception e) {
            model.addAttribute(
                    "error",
                    "Не удалось отправить письмо"
            );
            return "forgot-password";
        }
        return "redirect:/reset-password";
    }

    // страница сброса пароля
    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    // сброс пароля
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String code,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {
        String sessionCode =
                (String) session.getAttribute("reset_code");
        String email =
                (String) session.getAttribute("reset_email");
        Long time =
                (Long) session.getAttribute("reset_time");
        if (sessionCode == null ||
                email == null ||
                time == null) {
            return "redirect:/forgot-password";
        }
        long tenMinutes =
                10 * 60 * 1000;
        long currentTime =
                System.currentTimeMillis();
        if (currentTime - time > tenMinutes) {
            model.addAttribute(
                    "error",
                    "Код истёк"
            );
            return "reset-password";
        }
        if (!sessionCode.equals(code)) {
            model.addAttribute(
                    "error",
                    "Неверный код"
            );
            return "reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute(
                    "error",
                    "Пароли не совпадают"
            );
            return "reset-password";
        }
        if (!isPasswordStrong(password)) {

            model.addAttribute(
                    "error",
                    "Слабый пароль"
            );

            return "reset-password";
        }

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {

            return "redirect:/forgot-password";
        }

        user.setPassword(
                passwordEncoder.encode(password)
        );

        userRepository.save(user);

        session.removeAttribute("reset_email");
        session.removeAttribute("reset_code");
        session.removeAttribute("reset_time");

        return "redirect:/login?passwordChanged=true";
    }
}