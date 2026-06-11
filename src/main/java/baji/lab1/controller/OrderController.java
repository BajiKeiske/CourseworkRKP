package baji.lab1.controller;

import baji.lab1.entity.*;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.UserRepository;
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

    // ==================== ПОКУПАТЕЛЬ ====================

    @GetMapping("/checkout")
    public String checkoutForm(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Basket basket = basketRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        model.addAttribute("basket", basket);
        model.addAttribute("user", user);
        return "user/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String recipientName,
            @RequestParam String phone,
            @RequestParam String deliveryType,
            @RequestParam(required = false) String deliveryAddress,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String street,
            @RequestParam(required = false) String house,
            @RequestParam(required = false) String apartment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Basket basket = basketRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Корзина пуста"));

            Order order = orderService.createOrder(user, basket, recipientName, phone,
                    deliveryType, deliveryAddress, paymentMethod, comment,
                    city, street, house, apartment);

            if ("КАРТА".equals(paymentMethod)) {
                return "redirect:/payment/demo?orderId=" + order.getId();
            }

            redirectAttributes.addFlashAttribute("successMessage", "Заказ №" + order.getId() + " успешно оформлен");
            return "redirect:/user/orders";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("recipientName", recipientName);
            redirectAttributes.addFlashAttribute("phone", phone);
            redirectAttributes.addFlashAttribute("deliveryType", deliveryType);
            redirectAttributes.addFlashAttribute("paymentMethod", paymentMethod);
            redirectAttributes.addFlashAttribute("city", city);
            redirectAttributes.addFlashAttribute("street", street);
            redirectAttributes.addFlashAttribute("house", house);
            redirectAttributes.addFlashAttribute("apartment", apartment);
            return "redirect:/order/checkout";
        }
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = orderService.getOrderById(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к заказу");
        }

        orderService.cancelOrderByUser(orderId);
        return "redirect:/user/orders?cancelled=true";
    }

    @GetMapping("/details/{orderId}")
    public String orderDetails(@PathVariable Long orderId, Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = orderService.getOrderById(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к заказу");
        }

        model.addAttribute("order", order);
        return "user/order-details";
    }

    // ==================== АДМИНИСТРАТОР ====================

    @GetMapping("/admin/all")
    public String getAllOrders(@RequestParam(required = false) String status,
                               @RequestParam(required = false) Long open,
                               Model model) {
        model.addAttribute("orders", orderService.getOrdersByStatus(status));
        if (open != null) {
            model.addAttribute("selectedOrder", orderService.getOrderById(open));
        }
        model.addAttribute("selectedStatus", status);
        return "admin/orders";
    }

    @PostMapping("/admin/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return "redirect:/order/admin/all";
    }

    @PostMapping("/admin/{orderId}/assembly")
    public String startAssembly(@PathVariable Long orderId) {
        orderService.startAssembly(orderId);
        return "redirect:/order/admin/all";
    }

    @PostMapping("/admin/{orderId}/ready")
    public String readyForDelivery(@PathVariable Long orderId) {
        orderService.readyForDelivery(orderId);
        return "redirect:/order/admin/all";
    }

    @PostMapping("/admin/{orderId}/complete")
    public String completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return "redirect:/order/admin/all";
    }

    @PostMapping("/admin/{orderId}/cancel")
    public String cancelOrderByAdmin(@PathVariable Long orderId,
                                     @RequestParam String reason,
                                     @RequestParam(required = false) String status) {
        orderService.cancelOrderByAdmin(orderId, reason);
        return "redirect:/order/admin/all?status=" + (status != null ? status : "") + "&open=" + orderId;
    }
}