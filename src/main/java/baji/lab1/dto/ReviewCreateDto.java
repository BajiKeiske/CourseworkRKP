package baji.lab1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewCreateDto {

    @NotNull(message = "ID товара обязателен")
    private Long productId;

    @Size(min = 10, max = 2000, message = "Отзыв должен быть от 10 до 2000 символов")
    private String text;

    @Min(value = 1, message = "Рейтинг не может быть меньше 1")
    @Max(value = 5, message = "Рейтинг не может быть больше 5")
    private Integer rating;
}