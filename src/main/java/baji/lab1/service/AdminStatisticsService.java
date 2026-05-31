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

    // ================= ОСНОВНАЯ СТАТИСТИКА =================
    public Map<String, Object> getStatistics() {

        Map<String, Object> stats = new HashMap<>();

        List<Order> orders = orderRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();

        stats.put("totalProducts", products.size());
        stats.put("totalUsers", users.size());
        stats.put("totalOrders", orders.size());

        // ================= ВЫРУЧКА =================
        BigDecimal revenue = BigDecimal.ZERO;

        for (Order o : orders) {
            if (o.getTotalAmount() != null) {
                revenue = revenue.add(o.getTotalAmount());
            }
        }

        stats.put("totalRevenue", revenue);

        BigDecimal avg = orders.isEmpty()
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        stats.put("averageOrderValue", avg);

        // ================= ПОСЛЕДНИЕ ЗАКАЗЫ =================
        List<Map<String, Object>> recentOrders = new ArrayList<>();

        List<Order> sorted = orderRepository.findAllByOrderByOrderDateDesc();

        int limit = Math.min(10, sorted.size());

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (int i = 0; i < limit; i++) {

            Order o = sorted.get(i);

            Map<String, Object> map = new HashMap<>();

            map.put("id", o.getId());
            map.put("orderDate", o.getOrderDate() != null
                    ? o.getOrderDate().format(df)
                    : "нет даты");

            map.put("totalAmount", o.getTotalAmount());
            map.put("status", o.getStatus() != null
                    ? o.getStatus().name()
                    : "NEW");

            map.put("productsCount",
                    o.getOrderItems() != null ? o.getOrderItems().size() : 0);

            recentOrders.add(map);
        }

        stats.put("recentOrders", recentOrders);

        // ================= ПОЛЬЗОВАТЕЛИ =================
        List<Map<String, Object>> activeUsers = new ArrayList<>();

        int uLimit = Math.min(5, users.size());

        for (int i = 0; i < uLimit; i++) {

            User u = users.get(i);

            Map<String, Object> map = new HashMap<>();
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("ordersCount", 0);

            activeUsers.add(map);
        }

        stats.put("activeUsers", activeUsers);

        // ================= ДОП ДАННЫЕ =================
        stats.put("reportDate", LocalDateTime.now());
        stats.put("orderStatusStats", getOrderStatusStats());
        stats.put("monthlyRevenue", getMonthlyRevenue());

        return stats;
    }

    // ================= СТАТУСЫ =================
    public Map<String, Long> getOrderStatusStats() {

        List<Order> orders = orderRepository.findAll();

        Map<String, Long> map = new HashMap<>();

        for (Order o : orders) {

            String status = o.getStatus() != null
                    ? o.getStatus().name()
                    : "NEW";

            map.put(status, map.getOrDefault(status, 0L) + 1);
        }

        return map;
    }

    // ================= ВЫРУЧКА ПО МЕСЯЦАМ =================
    public Map<String, BigDecimal> getMonthlyRevenue() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusMonths(5).withDayOfMonth(1);

        List<Order> orders = orderRepository.findByOrderDateAfter(from);

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("MMM yyyy", new Locale("ru"));

        for (int i = 5; i >= 0; i--) {

            LocalDateTime m = now.minusMonths(i);
            result.put(m.format(fmt), BigDecimal.ZERO);
        }

        for (Order o : orders) {

            if (o.getOrderDate() == null || o.getTotalAmount() == null)
                continue;

            String key = o.getOrderDate().format(fmt);

            result.put(key,
                    result.getOrDefault(key, BigDecimal.ZERO)
                            .add(o.getTotalAmount()));
        }

        return result;
    }

    // ================= ДОП ОТЧЁТ =================
    public Map<String, Object> getReportData() {

        Map<String, Object> stats = getStatistics();

        Map<String, Integer> statusCount = new LinkedHashMap<>();

        for (Order o : orderRepository.findAll()) {

            String status = o.getStatus() != null
                    ? o.getStatus().name()
                    : "NEW";

            statusCount.put(status,
                    statusCount.getOrDefault(status, 0) + 1);
        }

        stats.put("orderStatuses", statusCount);
        stats.put("formattedDate",
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"))
                ));

        return stats;
    }
}