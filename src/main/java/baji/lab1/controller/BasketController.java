package baji.lab1.controller;

import baji.lab1.entity.*;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.UserRepository;
import baji.lab1.service.BasketService;
import baji.lab1.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import baji.lab1.repository.BundleRepository;

@Controller
@RequestMapping("/basket")
public class BasketController {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BasketRepository basketRepository;
    @Autowired private BundleRepository bundleRepository;
    @Autowired private WishlistService wishlistService;
    @Autowired private BasketService basketService;

    // ========= 1. ДОБАВЛЕНИЕ ОБЫЧНОГО ТОВАРА =========
    @PostMapping("/add/{productId}")
    public String addToBasket(@PathVariable Long productId,
                              @RequestParam(defaultValue = "1") int quantity,
                              Authentication auth,
                              RedirectAttributes ra) {
        try {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            basketService.addProductToBasket(user, productId, quantity);
            ra.addFlashAttribute("successMessage", "Товар добавлен в корзину");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/products/details/" + productId;
        }
        return "redirect:/basket";
    }

    // ========= 2. ОБНОВЛЕНИЕ КОЛИЧЕСТВА (для обычных товаров) =========
    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam int quantity,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var product = productRepository.findById(productId).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();

        // Находим товар в cartItems
        BasketItem item = basket.getCartItems().stream()
                .filter(i -> "PRODUCT".equals(i.getType())
                        && i.getProduct() != null
                        && i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            ra.addFlashAttribute("errorMessage", "Товар не найден в корзине");
            return "redirect:/basket";
        }

        if (quantity > product.getStock()) {
            ra.addFlashAttribute("errorMessage",
                    "Недостаточно товара. В наличии: " + product.getStock());
            return "redirect:/basket";
        }

        if (quantity <= 0) {
            basket.getCartItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        basketRepository.save(basket);
        return "redirect:/basket";
    }

    // ========= 3. УДАЛЕНИЕ ОБЫЧНОГО ТОВАРА =========
    @PostMapping("/remove/{productId}")
    public String removeFromBasket(@PathVariable Long productId, Authentication auth) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();

        // Удаляем товар из cartItems
        basket.getCartItems().removeIf(item ->
                "PRODUCT".equals(item.getType())
                        && item.getProduct() != null
                        && item.getProduct().getId().equals(productId));

        basketRepository.save(basket);
        return "redirect:/basket";
    }

    // ========= 4. ОЧИСТИТЬ ВСЮ КОРЗИНУ =========
    @PostMapping("/clear")
    public String clearBasket(Authentication auth) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();
        basket.clear(); // очищает cartItems
        basketRepository.save(basket);
        return "redirect:/basket";
    }

    // ========= 5. ПОКАЗАТЬ КОРЗИНУ =========
    @GetMapping("")
    public String viewBasket(Authentication auth, Model model) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseGet(() -> {
            var b = new Basket();
            b.setUser(user);
            return basketRepository.save(b);
        });

        boolean changed = false;

        // Проверяем остатки только для обычных товаров (PRODUCT)
        for (BasketItem item : basket.getCartItems()) {
            if ("PRODUCT".equals(item.getType()) && item.getProduct() != null) {
                int availableStock = item.getProduct().getStock() != null ? item.getProduct().getStock() : 0;
                if (availableStock <= 0) {
                    basket.getCartItems().remove(item);
                    changed = true;
                } else if (item.getQuantity() > availableStock) {
                    item.setQuantity(availableStock);
                    changed = true;
                }
            }
            // Для BUNDLE (комплектов) остатки не проверяем
        }

        if (changed) {
            basket = basketRepository.save(basket);
        }

        model.addAttribute("basket", basket);
        model.addAttribute("totalPrice", basket.getTotalAmount());
        return "user/basket";
    }

    // ========= 6. ДОБАВЛЕНИЕ КОМПЛЕКТА =========
    @PostMapping("/add-bundle")
    public String addBundleToBasket(@RequestParam Long bundleId,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Bundle bundle = bundleRepository.findById(bundleId)
                    .orElseThrow(() -> new RuntimeException("Комплект не найден"));

            Basket basket = basketRepository.findByUser(user).orElseGet(() -> {
                Basket b = new Basket();
                b.setUser(user);
                return b;
            });

            // 1. сначала удаляем дублирующиеся товары
            for (BundleItem bundleItem : bundle.getItems()) {
                basket.getCartItems().removeIf(item ->
                        "PRODUCT".equals(item.getType())
                                && item.getProduct() != null
                                && item.getProduct().getId().equals(bundleItem.getProduct().getId())
                );
            }

            // 2. потом добавляем комплект
            basket.addBundleItem(bundle, 1, bundle.getFinalPrice());

            // 3. и только потом сохраняем ОДИН раз
            basketRepository.save(basket);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Комплект \"" + bundle.getName() + "\" добавлен в корзину");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }
        return "redirect:/basket";
    }

    // ========= 7. УДАЛЕНИЕ ЛЮБОЙ ПОЗИЦИИ ПО ID (и товара, и комплекта) =========
    @PostMapping("/remove-cart-item/{id}")
    public String removeCartItem(@PathVariable Long id, Authentication auth) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();
        basket.getCartItems().removeIf(item -> item.getId().equals(id));
        basketRepository.save(basket);
        return "redirect:/basket";
    }

    // ========= 8. ОБНОВЛЕНИЕ КОЛИЧЕСТВА ЛЮБОЙ ПОЗИЦИИ =========
    @PostMapping("/update-cart-item/{id}")
    @ResponseBody
    public String updateCartItemQuantity(@PathVariable Long id,
                                         @RequestParam int quantity,
                                         Authentication auth) {
        var user = userRepository.findByUsername(auth.getName()).orElseThrow();
        var basket = basketRepository.findByUser(user).orElseThrow();

        BasketItem item = basket.getCartItems().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (item == null) return "error";

        // Проверка для товаров
        if ("PRODUCT".equals(item.getType()) && item.getProduct() != null) {
            if (quantity < 1) return "error";
            if (quantity > item.getProduct().getStock()) return "error";
        }

        // Для комплектов - любое количество (или запрети, если не нужно)
        if ("BUNDLE".equals(item.getType())) {
            return "error"; // или разреши quantity >= 1
        }

        item.setQuantity(quantity);
        basketRepository.save(basket);
        return "ok";
    }

    // ========= 9. БЕЙДЖ — ПОЛУЧИТЬ КОЛИЧЕСТВО ТОВАРОВ =========
    @GetMapping("/size")
    @ResponseBody
    public int getBasketSize(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return 0;
        return basketService.getTotalQuantity(user);
    }
}