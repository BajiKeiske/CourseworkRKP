package baji.lab1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import baji.lab1.entity.Product;

import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendOrderConfirmation(String to, String customerName, Long orderId,
                                      Double totalAmount, List<String> products) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Заказ #" + orderId + " оформлен в Vinyl");

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("totalAmount", totalAmount);
            context.setVariable("products", products);
            context.setVariable("orderDate", java.time.LocalDateTime.now());

            String htmlContent = templateEngine.process("email/order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Ошибка отправки письма: " + e.getMessage());
        }
    }

    // уведомление о появлении товара
    public void sendProductBackInStock(String to, String userName, Product product) {
        String subject = "Товар в избранном появился в наличии! - Vinyl";
        String text = String.format(
                "Здравствуйте, %s!\n\n" +
                        "Товар \"%s\" снова в наличии!\n" +
                        "Цена: %.2f руб.\n\n" +
                        "Перейдите в избранное, чтобы добавить товар в корзину:\n" +
                        "http://localhost:8080/user/wishlist\n\n" +
                        "С уважением,\nКоманда Vinyl",
                userName,
                product.getName(),
                product.getPrice()
        );

        sendEmail(to, subject, text);
    }
}