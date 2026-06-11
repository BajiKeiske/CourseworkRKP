package baji.lab1.controller;

import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import baji.lab1.service.BasketService;
import baji.lab1.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import baji.lab1.entity.Wishlist;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;
    private final BasketService basketService;

    //страница избранного
    @GetMapping
    public String wishlist(Authentication authentication,
                           Model model) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Wishlist wishlist = wishlistService.getWishlist(user);

        model.addAttribute("products", wishlist.getProducts());

        return "user/wishlist";
    }

    //добавить товар в избранное
    @PostMapping("/add/{productId}")
    public String addToWishlist(@PathVariable Long productId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            wishlistService.addToWishlist(user, productId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Товар добавлен в избранное"
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Не удалось добавить в избранное"
            );
        }

        return "redirect:/user/products/details/" + productId;
    }

    //удалить товар из избранного
    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(@PathVariable Long productId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            wishlistService.removeFromWishlist(user, productId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Товар удалён из избранного"
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Не удалось удалить из избранного"
            );
        }

        return "redirect:/user/products/details/" + productId;
    }

    //из избранного в корзину + удалить
    @PostMapping("/move-to-cart/{productId}")
    public String moveToCart(@PathVariable Long productId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // 1. Добавляем в корзину (переиспользуем сервис)
            basketService.addProductToBasket(user, productId, 1);

            // 2. Удаляем из избранного
            wishlistService.removeFromWishlist(user, productId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Товар добавлен в корзину и удалён из избранного");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/basket";
    }
}