package baji.lab1.service;

import baji.lab1.entity.Basket;
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
public class BasketServiceTest {

    @Autowired
    private BasketService basketService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveBasket_Success() {
        // Создаём пользователя со ВСЕМИ обязательными полями
        User user = new User();
        user.setUsername("baskettest_" + System.currentTimeMillis());
        user.setPassword("password123");
        user.setEmail("baskettest@test.com");
        user.setFullName("Тестовый Пользователь");  // ОБЯЗАТЕЛЬНО
        user.setPhone("+79991234567");              // ОБЯЗАТЕЛЬНО (если NOT NULL)
        user.setRole(Role.ROLE_USER);
        user.setBlocked(false);

        User savedUser = userRepository.save(user);

        // Создаём корзину
        Basket basket = new Basket();
        basket.setUser(savedUser);

        Basket savedBasket = basketService.save(basket);

        assertNotNull(savedBasket);
        assertNotNull(savedBasket.getId());
        assertEquals(savedUser.getId(), savedBasket.getUser().getId());

        // Очистка
        basketService.deleteById(savedBasket.getId());
        userRepository.deleteById(savedUser.getId());
    }

    @Test
    public void testDeleteBasket_Success() {
        // Создаём пользователя со ВСЕМИ обязательными полями
        User user = new User();
        user.setUsername("basketdel_" + System.currentTimeMillis());
        user.setPassword("password123");
        user.setEmail("basketdel@test.com");
        user.setFullName("Тестовый Пользователь"); 
        user.setPhone("+79991234567");
        user.setRole(Role.ROLE_USER);
        user.setBlocked(false);

        User savedUser = userRepository.save(user);

        // Создаём корзину
        Basket basket = new Basket();
        basket.setUser(savedUser);
        Basket savedBasket = basketService.save(basket);
        Long basketId = savedBasket.getId();

        // Удаляем
        basketService.deleteById(basketId);

        // Проверяем
        Basket deletedBasket = basketService.findById(basketId);
        assertNull(deletedBasket);

        // Очистка
        userRepository.deleteById(savedUser.getId());
    }
}