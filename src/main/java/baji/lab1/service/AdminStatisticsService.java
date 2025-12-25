package baji.lab1.service;

import baji.lab1.entity.Order;
import baji.lab1.entity.Product;
import baji.lab1.entity.User;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminStatisticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Простая статистика - считаем в коде
        List<Order> allOrders = orderRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        List<User> allUsers = userRepository.findAll();

        // 1. Базовая статистика
        stats.put("totalProducts", allProducts.size());
        stats.put("totalUsers", allUsers.size());
        stats.put("totalOrders", allOrders.size());

        // 2. Финансы
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : allOrders) {
            if (order.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(order.getTotalAmount());
            }
        }
        stats.put("totalRevenue", totalRevenue);

        BigDecimal averageOrder = allOrders.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(allOrders.size()), 2, RoundingMode.HALF_UP);
        stats.put("averageOrderValue", averageOrder);

        // 3. Популярные товары (первые 5 товаров из БД)
        List<Map<String, Object>> topProducts = new ArrayList<>();
        int limit = Math.min(5, allProducts.size());
        for (int i = 0; i < limit; i++) {
            Product p = allProducts.get(i);
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", p.getName());
            productData.put("price", p.getPrice() + " руб.");
            productData.put("ordersCount", 0); // временно
            topProducts.add(productData);
        }
        stats.put("topProducts", topProducts);

        // 4. Последние заказы (10 последних)
        List<Order> sortedOrders = orderRepository.findAllByOrderByOrderDateDesc();
        List<Map<String, Object>> recentOrders = new ArrayList<>();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        int orderLimit = Math.min(10, sortedOrders.size());
        for (int i = 0; i < orderLimit; i++) {
            Order order = sortedOrders.get(i);
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", order.getId());
            orderData.put("orderDate", order.getOrderDate() != null ?
                    order.getOrderDate().format(dateFormat) : "Нет даты");
            orderData.put("totalAmount", order.getTotalAmount() != null ?
                    order.getTotalAmount() + " руб." : "0 руб.");
            orderData.put("status", order.getStatus() != null ? order.getStatus() : "НОВЫЙ");
            orderData.put("productsCount", order.getProducts() != null ?
                    order.getProducts().size() : 0);
            recentOrders.add(orderData);
        }
        stats.put("recentOrders", recentOrders);

        // 5. Активные пользователи (первые 5)
        List<Map<String, Object>> activeUsers = new ArrayList<>();
        int userLimit = Math.min(5, allUsers.size());
        for (int i = 0; i < userLimit; i++) {
            User user = allUsers.get(i);
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("ordersCount", 0); // временно
            activeUsers.add(userData);
        }
        stats.put("activeUsers", activeUsers);

        // 6. Дата отчета
        stats.put("reportDate", LocalDateTime.now());
        stats.put("orderStatusStats", getOrderStatusStats());
        stats.put("monthlyRevenue", getMonthlyRevenue());

        return stats;
    }


    // Дополнительные методы для отчетов
    public Map<String, Long> getOrderStatusStats() {
        List<Order> allOrders = orderRepository.findAll();
        Map<String, Long> statusMap = new HashMap<>();

        for (Order order : allOrders) {
            String status = order.getStatus() != null ? order.getStatus() : "НОВЫЙ";
            statusMap.put(status, statusMap.getOrDefault(status, 0L) + 1);
        }
        return statusMap;
    }

    public Map<String, BigDecimal> getMonthlyRevenue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixMonthsAgo = now.minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0);

        List<Order> recentOrders = orderRepository.findByOrderDateAfter(sixMonthsAgo);

        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("ru"));

        // Инициализируем последние 6 месяцев
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String monthKey = month.format(monthFormatter);
            revenueByMonth.put(monthKey, BigDecimal.ZERO);
        }

        // Заполняем данными
        for (Order order : recentOrders) {
            if (order.getTotalAmount() != null && order.getOrderDate() != null) {
                String monthKey = order.getOrderDate().format(monthFormatter);
                BigDecimal current = revenueByMonth.getOrDefault(monthKey, BigDecimal.ZERO);
                revenueByMonth.put(monthKey, current.add(order.getTotalAmount()));
            }
        }

        return revenueByMonth;
    }


    // Добавьте этот метод
    public Map<String, Object> getReportData() {
        Map<String, Object> stats = getStatistics(); // ваш текущий метод

        // Простая статистика по статусам (для визуализации)
        List<Order> orders = orderRepository.findAll();
        Map<String, Integer> statusCount = new LinkedHashMap<>();

        for (Order order : orders) {
            String status = order.getStatus() != null ? order.getStatus() : "НОВЫЙ";
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }

        // Форматируем дату красиво
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy г.", new Locale("ru"));
        stats.put("formattedDate", LocalDateTime.now().format(dtf));
        stats.put("orderStatuses", statusCount);

        return stats;
    }

}