package baji.lab1.config;

import baji.lab1.entity.Role;
import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            // есть ли юзеры
            if (userRepository.count() == 0) {
                // Создаем админа
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@mail.ru");
                admin.setPassword(passwordEncoder.encode("admin123")); //хешируем
                admin.setRole(Role.ROLE_ADMIN);
                userRepository.save(admin);

                // создание юзера
                User user = new User();
                user.setUsername("user");
                user.setEmail("user@mail.ru");
                user.setPassword(passwordEncoder.encode("user123")); // хешируем
                user.setRole(Role.ROLE_USER);
                userRepository.save(user);

                System.out.println("Созданы тестовые пользователи:");
                System.out.println("Админ: admin / admin123");
                System.out.println("Пользователь: user / user123");
            }
        };
    }
}