package baji.lab1.controller;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Product;
import baji.lab1.repository.BasketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import baji.lab1.repository.ProductRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/basket")
public class BasketController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BasketRepository basketRepository;

    // Добавить товар в корзину
    @PostMapping("/add/{productId}")
    public String addToBasket(@PathVariable Long productId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {  // ДОБАВИЛ

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseGet(() -> {
                    Basket newBasket = new Basket();
                    newBasket.setUser(user);
                    return newBasket;
                });

        // Добавляем товар
        basket.addProduct(product);
        basketRepository.save(basket);

        // ДОБАВИЛ ЭТУ СТРОЧКУ ДЛЯ УВЕДОМЛЕНИЯ:
        redirectAttributes.addFlashAttribute("successMessage",
                "Товар '" + product.getName() + "' добавлен в корзину!");

        return "redirect:/user/products/catalog";
    }

    // Просмотр корзины
    @GetMapping("")
    public String viewBasket(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseGet(() -> {
                    Basket newBasket = new Basket();
                    newBasket.setUser(user);
                    return basketRepository.save(newBasket);
                });

        model.addAttribute("basket", basket);
        model.addAttribute("totalPrice", calculateTotal(basket));

        return "user/basket";
    }

    private double calculateTotal(Basket basket) {
        return basket.getProducts().stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }

    // Удаление из корзины
    @PostMapping("/remove/{productId}")
    public String removeFromBasket(@PathVariable Long productId,
                                   Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        basket.getProducts().remove(product);
        basketRepository.save(basket);

        return "redirect:/basket";
    }

    // Очистка корзины
    @PostMapping("/clear")
    public String clearBasket(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        basket.getProducts().clear();
        basketRepository.save(basket);

        return "redirect:/basket";
    }
}