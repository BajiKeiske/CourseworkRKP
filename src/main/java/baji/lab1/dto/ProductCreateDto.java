package baji.lab1.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class ProductCreateDto {

    @NotBlank(message = "Название товара обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    @DecimalMin(value = "0.01", message = "Цена должна быть не менее 0.01")
    private Double price;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Max(value = 100000, message = "Слишком большое количество")
    private Integer stock;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    @NotNull(message = "Бренд обязателен")
    private Long brandId;

    // НОВОЕ ПОЛЕ для загрузки файла
    private MultipartFile imageFile;

    // Геттеры и сеттеры
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public ProductCreateDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    // НОВЫЕ геттер и сеттер для файла
    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
}