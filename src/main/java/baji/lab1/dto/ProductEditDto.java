package baji.lab1.dto;

import jakarta.validation.constraints.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProductEditDto {
    @NotNull(message = "ID является обязательным")
    private Long id;

    @NotBlank(message = "Название товара обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    @DecimalMin(value = "0.01", message = "Цена должна быть не менее 0.01")
    private Double price;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Max(value = 100000, message = "Слишком большое количество")
    private Integer stock;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    @NotNull(message = "Бренд обязателен")
    private Long brandId;

    private MultipartFile imageFile;
    private MultipartFile imageFile2;
    private MultipartFile imageFile3;
    private MultipartFile imageFile4;

    public ProductEditDto() {}

    public ProductEditDto(Long id, String name, Double price, String description, Integer stock, Long categoryId, Long brandId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    public MultipartFile getImageFile2() {
        return imageFile2;
    }

    public void setImageFile2(MultipartFile imageFile2) {
        this.imageFile2 = imageFile2;
    }

    public MultipartFile getImageFile3() {
        return imageFile3;
    }

    public void setImageFile3(MultipartFile imageFile3) {
        this.imageFile3 = imageFile3;
    }

    public MultipartFile getImageFile4() {
        return imageFile4;
    }

    public void setImageFile4(MultipartFile imageFile4) {
        this.imageFile4 = imageFile4;
    }


    // Для атрибутов
    private Map<Long, String> attributeValues = new HashMap<>();

    public Map<Long, String> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(Map<Long, String> attributeValues) {
        this.attributeValues = attributeValues;
    }
}