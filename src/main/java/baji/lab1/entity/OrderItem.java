package baji.lab1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data // генерирует геттеры, сеттеры, equals, hashCode, toString
@NoArgsConstructor // пустой конструктор
@AllArgsConstructor // конструктор со всеми полями
@Builder // паттерн builder (опционально)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;
    private BigDecimal priceAtTime;
}