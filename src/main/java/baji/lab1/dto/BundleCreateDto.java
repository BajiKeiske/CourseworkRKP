package baji.lab1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class BundleCreateDto {

    @NotBlank(message = "Название комплекта обязательно")
    private String name;

    private String description;
    private String imageUrl;

    @NotBlank(message = "Тип скидки обязателен")
    private String discountType;

    @NotNull(message = "Значение скидки обязательно")
    @Positive(message = "Скидка должна быть положительной")
    private Double discountValue;

    private LocalDate startDate;
    private LocalDate endDate;

    private Map<Long, Integer> products;
}