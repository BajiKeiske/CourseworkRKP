package baji.lab1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data // Lombok: генерирует геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor // Lombok: конструктор без аргументов
@AllArgsConstructor // Lombok: конструктор со всеми аргументами
@Builder // Lombok: паттерн Builder для удобного создания объектов
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с пользователем (автор отзыва)
    @NotNull(message = "Пользователь обязателен")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Связь с товаром
    @NotNull(message = "Товар обязателен")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Текст отзыва
    @Size(min = 10, max = 2000, message = "Отзыв должен быть от 10 до 2000 символов")
    @Column(nullable = false, length = 2000)
    private String text;

    // Рейтинг от 1 до 5
    @Min(value = 1, message = "Рейтинг не может быть меньше 1")
    @Max(value = 5, message = "Рейтинг не может быть больше 5")
    @Column(nullable = false)
    private Integer rating;

    // Дата создания (автоматически устанавливается при сохранении)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Автоматическая установка даты перед сохранением
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}