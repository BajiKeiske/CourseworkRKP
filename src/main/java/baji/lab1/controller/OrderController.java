package baji.lab1.controller;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Order;
import baji.lab1.entity.Product;
import baji.lab1.entity.User;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.UserRepository;
import baji.lab1.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import baji.lab1.service.EmailService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    // ========== ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ==========
    @GetMapping("/checkout")
    public String checkoutForm(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        BigDecimal totalAmount = basket.getProducts().stream()
                .map(product -> BigDecimal.valueOf(product.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("basket", basket);
        model.addAttribute("totalAmount", totalAmount);

        return "user/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam String deliveryAddress,
                                  @RequestParam String paymentMethod,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        if (basket.getProducts().isEmpty()) {
            return "redirect:/basket?error=empty";
        }

        BigDecimal totalAmount = basket.getProducts().stream()
                .map(product -> BigDecimal.valueOf(product.getPrice())) // если price - Double
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setProducts(new ArrayList<>(basket.getProducts()));
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("НОВЫЙ");
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);

        orderRepository.save(order);
        basket.getProducts().clear();
        basketRepository.save(basket);

        try {
            String emailText = "Здравствуйте, " + user.getUsername() + "!\n\n" +
                    "Ваш заказ #" + order.getId() + " успешно оформлен.\n" +
                    "Сумма: " + totalAmount + " руб.\n" +
                    "Статус: НОВЫЙ\n" +
                    "Адрес доставки: " + deliveryAddress + "\n\n" +
                    "Спасибо за покупку в Vinyl Shop!";

            emailService.sendEmail(user.getEmail(),
                    "Заказ #" + order.getId() + " оформлен",
                    emailText);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Заказ оформлен! Подробности отправлены на вашу почту " + user.getEmail());
        } catch (Exception e) {
            System.out.println("Не удалось отправить письмо: " + e.getMessage());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Заказ #" + order.getId() + " успешно оформлен!");
        }


        return "redirect:/user/orders?success=true";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет прав для отмены этого заказа");
        }

        order.setStatus("ОТМЕНЁН");
        orderRepository.save(order);

        return "redirect:/user/orders?cancelled=true";
    }

    // ========== ДЛЯ АДМИНИСТРАТОРОВ ==========
    @GetMapping("/admin/all")
    public String getAllOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @PostMapping("/admin/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus("ПОДТВЕРЖДЁН");
        orderRepository.save(order);

        try {
            String emailText = "Ваш заказ #" + order.getId() + " подтверждён администратором.\n" +
                    "Статус изменён на: ПОДТВЕРЖДЁН\n" +
                    "Скоро с вами свяжутся для уточнения деталей.";

            emailService.sendEmail(order.getUser().getEmail(),
                    "Заказ #" + order.getId() + " подтверждён",
                    emailText);
        } catch (Exception e) {
            System.out.println("Не удалось отправить письмо: " + e.getMessage());
        }

        return "redirect:/order/admin/all";
    }

    @PostMapping("/admin/{orderId}/cancel")
    public String cancelOrderByAdmin(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus("ОТМЕНЁН");
        orderRepository.save(order);

        try {
            String emailText = "Ваш заказ #" + order.getId() + " отменён администратором.\n" +
                    "Статус изменён на: ОТМЕНЁН\n" +
                    "По всем вопросам обращайтесь в поддержку.";

            emailService.sendEmail(order.getUser().getEmail(),
                    "Заказ #" + order.getId() + " отменён",
                    emailText);
        } catch (Exception e) {
            System.out.println("Не удалось отправить письмо: " + e.getMessage());
        }

        return "redirect:/order/admin/all";
    }
}