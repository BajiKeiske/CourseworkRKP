package baji.lab1.controller;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Product;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/basket")
public class BasketController {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BasketRepository basketRepository;

    @PostMapping("/add/{productId}")
    public String addToBasket(@PathVariable Long productId,
                              @RequestParam(defaultValue = "1") int quantity,
                              Authentication auth,
                              RedirectAttributes ra) {

        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var product = productRepository.findById(productId).orElseThrow();

        if (product.getStock() == null || product.getStock() <= 0) {
            ra.addFlashAttribute(
                    "errorMessage",
                    "Товар закончился и сейчас недоступен для покупки"
            );
            return "redirect:/user/products/details/" + productId;
        }

        var basket = basketRepository.findByUser(user).orElseGet(() -> {
            var b = new Basket();
            b.setUser(user);
            return b;
        });

        synchronized (this) {

            int currentQty = basket.getItems().getOrDefault(product, 0);

            if (currentQty + quantity > product.getStock()) {

                ra.addFlashAttribute(
                        "errorMessage",
                        "В наличии осталось только "
                                + product.getStock()
                                + " шт."
                );

                return "redirect:/user/products/details/" + productId;
            }

            basket.addProduct(product, quantity);
            basketRepository.save(basket);
        }

        ra.addFlashAttribute(
                "successMessage",
                "Товар добавлен в корзину"
        );

        return "redirect:/user/products/catalog";
    }

    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam int quantity,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var product = productRepository.findById(productId).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();

        if (quantity > product.getStock()) {
            ra.addFlashAttribute("errorMessage",
                    "Недостаточно товара. В наличии: " + product.getStock());
            return "redirect:/basket";
        }

        basket.updateQuantity(product, quantity);
        basketRepository.save(basket);
        return "redirect:/basket";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromBasket(@PathVariable Long productId, Authentication auth) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var product = productRepository.findById(productId).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();
        basket.removeProduct(product);
        basketRepository.save(basket);
        return "redirect:/basket";
    }

    @PostMapping("/clear")
    public String clearBasket(Authentication auth) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();
        basket.clear();
        basketRepository.save(basket);
        return "redirect:/basket";
    }

    @GetMapping("")
    public String viewBasket(Authentication auth, Model model) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseGet(() -> {
            var b = new Basket();
            b.setUser(user);
            return basketRepository.save(b);
        });
        model.addAttribute("basket", basket);
        model.addAttribute("totalPrice", basket.getTotalPrice());

        model.addAttribute("errorMessage", null);
        model.addAttribute("successMessage", null);
        return "user/basket";
    }
}