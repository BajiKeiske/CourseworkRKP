package baji.lab1.controller;

import baji.lab1.entity.Order;
import baji.lab1.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/payment/demo")
    public String paymentDemoPage(@RequestParam Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "user/payment-demo";
    }

    @PostMapping("/payment/demo")
    public String processPayment(@RequestParam Long orderId, RedirectAttributes redirectAttributes) {
        try {
            // Меняем статус заказа с NEW на PAID
            orderService.confirmOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Заказ №" + orderId + " успешно оплачен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка оплаты: " + e.getMessage());
        }
        return "redirect:/user/orders";
    }
}