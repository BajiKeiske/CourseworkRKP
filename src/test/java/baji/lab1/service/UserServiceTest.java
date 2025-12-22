package baji.lab1.service;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Role;
import baji.lab1.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    public UserService userService;

    private final User userTest = new User("test", "test", "test", Role.ROLE_USER);

    @Test
    public void testSaveBasket_Success() {
        User savedUser = userService.save(userTest);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());

        userService.deleteById(savedUser.getId());
    }
}