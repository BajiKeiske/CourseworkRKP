package baji.lab1.service;

import baji.lab1.entity.Basket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BasketServiceTest {

    @Autowired
    public BasketService basketService;

    @Autowired
    public UserService userService;

    private final Basket testBasket = new Basket(null, null, null);

    @Test
    public void testSaveBasket_Success() {
        Basket savedBasket = basketService.save(testBasket);

        assertNotNull(savedBasket);
        assertNotNull(savedBasket.getId());

        basketService.deleteById(savedBasket.getId());
    }

    @Test
    public void testDeleteBasket_Success() {
        Basket savedBasket = basketService.save(testBasket);
        basketService.deleteById(savedBasket.getId());

        Basket basket = basketService.findById(savedBasket.getId());

        assertNull(basket);
    }
}