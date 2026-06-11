package baji.lab1.service;

import baji.lab1.entity.Role;
import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveUser_Success() {
        // Создаём уникального пользователя (с timestamp, чтобы избежать дублирования)
        long timestamp = System.currentTimeMillis();
        String uniqueUsername = "testuser_" + timestamp;
        String uniqueEmail = "test_" + timestamp + "@test.com";

        User user = new User();
        user.setUsername(uniqueUsername);
        user.setPassword("password123");
        user.setEmail(uniqueEmail);
        user.setFullName("Тестовый Пользователь");
        user.setPhone("+79991234567");
        user.setRole(Role.ROLE_USER);
        user.setBlocked(false);

        // Сохраняем пользователя
        User savedUser = userService.save(user);

        // Проверяем, что сохранился успешно
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals(uniqueUsername, savedUser.getUsername());
        assertEquals(uniqueEmail, savedUser.getEmail());

        // Очищаем тестовые данные
        userService.deleteById(savedUser.getId());
    }
}