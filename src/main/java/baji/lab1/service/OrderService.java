package baji.lab1.service;

import baji.lab1.entity.*;
import baji.lab1.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    // ========= ВСПОМОГАТЕЛЬНЫЙ МЕТОД: ОТПРАВКА ПИСЬМА =========
    private void sendStatusNotification(Order order, String newStatus) {
        try {
            String subject = "Статус заказа №" + order.getId() + " изменён";
            String text = "Здравствуйте, " + order.getUser().getUsername() + "!\n\n" +
                    "Ваш заказ №" + order.getId() + " теперь имеет статус: " + newStatus + ".\n\n" +
                    "Спасибо, что выбираете Vinyl!";
            emailService.sendEmail(order.getUser().getEmail(), subject, text);
        } catch (Exception e) {
            System.out.println("Не удалось отправить письмо: " + e.getMessage());
        }
    }

    // ========= ПОЛУЧИТЬ ЗАКАЗ ПО ID =========
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    // ========= ВСЕ ЗАКАЗЫ (для админа) =========
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    // ========= ЗАКАЗЫ ПО СТАТУСУ =========
    public List<Order> getOrdersByStatus(String status) {
        if (status == null || status.isBlank()) {
            return orderRepository.findAllByOrderByOrderDateDesc();
        }
        return orderRepository.findByStatus(OrderStatus.valueOf(status));
    }

    // ========= ЗАКАЗЫ КОНКРЕТНОГО ПОЛЬЗОВАТЕЛЯ =========
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    // ========= ГЛАВНЫЙ МЕТОД: СОЗДАНИЕ ЗАКАЗА ИЗ КОРЗИНЫ =========
    @Transactional
    public Order createOrder(User user, Basket basket, String recipientName, String phone,
                             String deliveryType, String deliveryAddress, String paymentMethod,
                             String comment, String city, String street, String house, String apartment) {

        // 1. Проверка: корзина не пустая (смотрим cartItems)
        if (basket.getCartItems() == null || basket.getCartItems().isEmpty()) {
            throw new RuntimeException("Корзина пустая");
        }

        // 2. Проверка наличия товаров на складе (для обычных товаров)
        for (BasketItem item : basket.getCartItems()) {
            if ("PRODUCT".equals(item.getType()) && item.getProduct() != null) {
                Product product = item.getProduct();
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                if (currentStock < item.getQuantity()) {
                    throw new RuntimeException("Товара «" + product.getName() + "» осталось " + currentStock + " шт.");
                }
            }
            // Для комплектов (BUNDLE) проверяем отдельно
            if ("BUNDLE".equals(item.getType()) && item.getBundle() != null) {
                for (BundleItem bundleItem : item.getBundle().getItems()) {
                    Product product = bundleItem.getProduct();
                    int currentStock = product.getStock() != null ? product.getStock() : 0;
                    int needed = bundleItem.getQuantity() * item.getQuantity();
                    if (currentStock < needed) {
                        throw new RuntimeException("Для комплекта «" + item.getBundle().getName() + "» не хватает товара: " + product.getName());
                    }
                }
            }
        }

        // 3. Формируем адрес доставки (если курьер)
        String finalDeliveryAddress = deliveryAddress;
        if (deliveryType.equals("КУРЬЕР") && (deliveryAddress == null || deliveryAddress.isBlank())) {
            finalDeliveryAddress = city + ", ул. " + street + ", д. " + house;
            if (apartment != null && !apartment.isBlank()) {
                finalDeliveryAddress += ", кв. " + apartment;
            }
        }

        // 4. Рассчитываем общую сумму заказа
        BigDecimal total = BigDecimal.ZERO;
        for (BasketItem item : basket.getCartItems()) {
            total = total.add(BigDecimal.valueOf(item.getPriceAtAdd() * item.getQuantity()));
        }

        // 5. Создаём заказ
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .totalAmount(total)
                .status(OrderStatus.NEW)
                .recipientName(recipientName)
                .phone(phone)
                .deliveryType(deliveryType)
                .deliveryAddress(finalDeliveryAddress)
                .paymentMethod(paymentMethod)
                .comment(comment)
                .build();

        // 6. Добавляем товары в заказ
        for (BasketItem item : basket.getCartItems()) {
            if ("PRODUCT".equals(item.getType()) && item.getProduct() != null) {
                // Обычный товар
                OrderItem orderItem = OrderItem.builder()
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .priceAtTime(BigDecimal.valueOf(item.getPriceAtAdd()))
                        .build();
                order.addOrderItem(orderItem);
            }

            if ("BUNDLE".equals(item.getType()) && item.getBundle() != null) {
                // Комплект: разбираем на отдельные товары
                for (BundleItem bundleItem : item.getBundle().getItems()) {
                    OrderItem orderItem = OrderItem.builder()
                            .product(bundleItem.getProduct())
                            .quantity(bundleItem.getQuantity() * item.getQuantity())
                            .priceAtTime(BigDecimal.valueOf(bundleItem.getProduct().getPrice()))
                            .build();
                    order.addOrderItem(orderItem);
                }
            }
        }

        // 7. Сохраняем заказ
        Order saved = orderRepository.save(order);

        // 8. СПИСЫВАЕМ ТОВАРЫ СО СКЛАДА
        for (BasketItem item : basket.getCartItems()) {
            if ("PRODUCT".equals(item.getType()) && item.getProduct() != null) {
                // Обычный товар
                Product product = item.getProduct();
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }

            if ("BUNDLE".equals(item.getType()) && item.getBundle() != null) {
                // Комплект: списываем каждый товар внутри
                for (BundleItem bundleItem : item.getBundle().getItems()) {
                    Product product = bundleItem.getProduct();
                    product.setStock(product.getStock() - (bundleItem.getQuantity() * item.getQuantity()));
                    productRepository.save(product);
                }
            }
        }

        // 9. ОЧИЩАЕМ КОРЗИНУ
        basket.getCartItems().clear();
        basketRepository.save(basket);

        // 10. Отправляем письмо
        sendStatusNotification(saved, "НОВЫЙ");

        return saved;
    }

    // ========= МЕТОДЫ АДМИНА =========

    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new RuntimeException("Можно подтвердить только новый заказ");
        }
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        sendStatusNotification(order, "ОПЛАЧЕН");
    }

    @Transactional
    public void startAssembly(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Заказ можно отправить в сборку только из статуса NEW или PAID");
        }
        order.setStatus(OrderStatus.ASSEMBLY);
        orderRepository.save(order);
        sendStatusNotification(order, "В СБОРКЕ");
    }

    @Transactional
    public void readyForDelivery(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.ASSEMBLY) {
            throw new RuntimeException("Заказ должен быть в сборке");
        }
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        orderRepository.save(order);
        sendStatusNotification(order, "ГОТОВ К ДОСТАВКЕ");
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Отменённый заказ нельзя завершить");
        }
        if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
            throw new RuntimeException("Завершить можно только заказ со статусом 'Готов к доставке'");
        }
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        sendStatusNotification(order, "ВЫПОЛНЕН");
    }

    @Transactional
    public void cancelOrderByAdmin(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Заказ уже отменён");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Выполненный заказ нельзя отменить");
        }
        if (order.getStatus() == OrderStatus.ASSEMBLY || order.getStatus() == OrderStatus.READY_FOR_DELIVERY) {
            throw new RuntimeException("Заказ в процессе выполнения нельзя отменить");
        }

        // Возвращаем товары на склад
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        String subject = "Заказ №" + order.getId() + " отменён администратором";
        String text = "Здравствуйте, " + order.getUser().getUsername() + "!\n\n" +
                "Ваш заказ №" + order.getId() + " был отменён администратором.\n" +
                "Причина: " + reason + "\n\n" +
                "Если у вас есть вопросы, свяжитесь с поддержкой.\n\n" +
                "С уважением, Vinyl Store";
        emailService.sendEmail(order.getUser().getEmail(), subject, text);
    }

    // ========= МЕТОДЫ ПОКУПАТЕЛЯ =========

    @Transactional
    public void cancelOrderByUser(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Заказ можно отменить только до начала сборки");
        }

        // Возвращаем товары на склад
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        sendStatusNotification(order, "ОТМЕНЁН (покупателем)");
    }
}