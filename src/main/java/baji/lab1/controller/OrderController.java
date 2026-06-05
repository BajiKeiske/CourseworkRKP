package baji.lab1.controller;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Order;
import baji.lab1.entity.User;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.UserRepository;
import baji.lab1.service.EmailService;
import baji.lab1.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    // страница оформления заказа
    @GetMapping("/checkout")
    public String checkoutForm(Model model, Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        model.addAttribute("basket", basket);

        // ФИО пользователя
        model.addAttribute("user", user);

        return "user/checkout";
    }

    // обработка оформления заказа
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String recipientName,
            @RequestParam String phone,
            @RequestParam String deliveryType,
            @RequestParam(required = false) String deliveryAddress,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Basket basket = basketRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Корзина пуста"));

            Order order = orderService.createOrder(
                    user,
                    basket,
                    recipientName,
                    phone,
                    deliveryType,
                    deliveryAddress,
                    paymentMethod,
                    comment
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Заказ №" + order.getId() + " успешно оформлен"
            );

            return "redirect:/user/orders";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/order/checkout";
        }
    }

    // отмена заказа пользователем
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId,
                              Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = orderService.getOrderById(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к заказу");
        }

        orderService.cancelOrder(orderId);

        return "redirect:/user/orders?cancelled=true";
    }

    // детали заказа
    @GetMapping("/details/{orderId}")
    public String orderDetails(@PathVariable Long orderId,
                               Authentication authentication,
                               Model model) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = orderService.getOrderById(orderId);

        // проверка доступа
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к заказу");
        }

        model.addAttribute("order", order);

        return "user/order-details";
    }

    // список всех заказов для админа
    @GetMapping("/admin/all")
    public String getAllOrders(@RequestParam(required = false) String status,
                               @RequestParam(required = false) Long open,
                               Model model) {

        // комментарий: получаем заказы с фильтром
        model.addAttribute("orders", orderService.getOrdersByStatus(status));

        // комментарий: открытый заказ в drawer
        if (open != null) {
            model.addAttribute("selectedOrder",
                    orderService.getOrderById(open));
        }

        model.addAttribute("selectedStatus", status);

        return "admin/orders";
    }

    // подтверждение заказа админом
    @PostMapping("/admin/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId) {

        orderService.confirmOrder(orderId);

        return "redirect:/order/admin/all";
    }

    // отмена заказа админом
    @PostMapping("/admin/{orderId}/cancel")
    public String cancelOrderByAdmin(@PathVariable Long orderId) {

        orderService.cancelOrder(orderId);

        return "redirect:/order/admin/all";
    }
}