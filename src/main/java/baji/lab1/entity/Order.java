package baji.lab1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime orderDate;
    private BigDecimal totalAmount;

    @Column(name = "status")
    private String status;

    @OneToMany
    private List<Product> products = new ArrayList<>();

    // Конструкторы
    public Order() {}

    public Order(User user, LocalDateTime orderDate, BigDecimal totalAmount, String status, List<Product> products) {
        this.user = user;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.products = products;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() {
        return status;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}