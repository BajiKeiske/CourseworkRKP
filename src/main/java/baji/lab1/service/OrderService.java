package baji.lab1.service;

import baji.lab1.entity.*;
import baji.lab1.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(User user,
                             Basket basket,
                             String recipientName,
                             String phone,
                             String deliveryType,
                             String deliveryAddress,
                             String paymentMethod,
                             String comment) {

        // 1. Проверка корзины
        if (basket.getItems() == null || basket.getItems().isEmpty()) {
            throw new RuntimeException("Корзина пустая");
        }

        // 2. Проверка наличия товаров
        for (Map.Entry<Product, Integer> entry : basket.getItems().entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();

            Product current = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден: " + product.getName()));

            if (current.getStock() < quantity) {
                throw new RuntimeException("Недостаточно товара: " + current.getName() +
                        " (нужно " + quantity + ", есть " + current.getStock() + ")");
            }
        }

        // 3. Расчет суммы
        BigDecimal total = basket.getItems().entrySet().stream()
                .map(entry -> BigDecimal.valueOf(entry.getKey().getPrice() * entry.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Создание заказа
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.NEW);
        order.setRecipientName(recipientName);
        order.setPhone(phone);
        order.setDeliveryType(deliveryType);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);
        order.setComment(comment);

        // 5. Добавление товаров в заказ
        for (Map.Entry<Product, Integer> entry : basket.getItems().entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPriceAtTime(BigDecimal.valueOf(product.getPrice()));

            order.addOrderItem(orderItem);
        }

        // 6. Сохранение заказа
        Order saved = orderRepository.save(order);

        // 7. Списание товаров со склада
        for (Map.Entry<Product, Integer> entry : basket.getItems().entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();

            Product current = productRepository.findById(product.getId()).orElseThrow();
            current.setStock(current.getStock() - quantity);
            productRepository.save(current);
        }

        // 8. Очистка корзины
        basket.getItems().clear();
        basketRepository.save(basket);

        return saved;
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new RuntimeException("Можно подтвердить только новый заказ");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Завершенный заказ нельзя отменить");
        }

        // Возврат товаров на склад
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> getOrdersByStatus(String status) {
        if (status == null || status.isBlank()) {
            return orderRepository.findAllByOrderByOrderDateDesc();
        }
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    private void validateCanChange(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Заказ нельзя изменить");
        }
    }
}