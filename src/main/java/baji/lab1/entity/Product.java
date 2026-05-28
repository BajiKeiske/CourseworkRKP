package baji.lab1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Товар" для интернет-магазина музыкальных инструментов.
 * Представляет музыкальный инструмент или аксессуар с характеристиками:
 * цена, наличие на складе, категория, бренд, отзывы и рейтинг.
 *
 * @author (Хафизова М.М.)
 * @version 1.0
 * @since 2026-03-05
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название товара (например, "Электрогитара Fender Stratocaster") */
    @NotBlank(message = "Название товара обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    /** Цена товара в рублях. Должна быть положительной. */
    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    @DecimalMin(value = "0.01", message = "Цена должна быть не менее 0.01")
    private Double price;

    /** Подробное описание товара (характеристики, комплектация) */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    /** Количество единиц товара на складе. Не может быть отрицательным. */
    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Max(value = 10000, message = "Слишком большое количество")
    private Integer stock;

    /** Категория товара (связь Many-to-One с сущностью Category) */
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "category_id")
    private Category category;

    /** Бренд товара (связь Many-to-One с сущностью Brand) */
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /** URL изображения товара */
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_url2")
    private String imageUrl2;

    @Column(name = "image_url3")
    private String imageUrl3;

    @Column(name = "image_url4")
    private String imageUrl4;

    /** Список отзывов на товар (связь One-to-Many с сущностью Review) */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    /** Список заказов, содержащих этот товар (связь Many-to-Many с Order) */
    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    /** Средний рейтинг товара (от 0.00 до 5.00) */
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    /** Количество оставленных отзывов */
    @Column(name = "review_count")
    private Integer reviewCount = 0;

    /**
     * Конструктор по умолчанию (требуется JPA)
     */
    public Product() {}

    /**
     * Конструктор для создания товара с основными параметрами.
     *
     * @param id          уникальный идентификатор товара
     * @param name        название товара
     * @param price       цена товара
     * @param description описание товара
     * @param stock       количество на складе
     * @param category    категория товара
     * @param brand       бренд товара
     * @param imageUrl    URL изображения
     * @param reviews     список отзывов
     */
    public Product(Long id, String name, Double price, String description, Integer stock,
                   Category category, Brand brand, String imageUrl, List<Review> reviews) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.category = category;
        this.brand = brand;
        this.imageUrl = imageUrl;
        if (reviews != null) {
            this.reviews = reviews;
        }
    }

    /**
     * Возвращает ID товара.
     *
     * @return уникальный идентификатор товара
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает ID товара.
     *
     * @param id уникальный идентификатор товара
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает название товара.
     *
     * @return название товара
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название товара.
     *
     * @param name название товара
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает цену товара.
     *
     * @return цена товара в рублях
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Устанавливает цену товара.
     *
     * @param price цена товара в рублях
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Возвращает описание товара.
     *
     * @return описание товара
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание товара.
     *
     * @param description описание товара
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает количество товара на складе.
     *
     * @return количество на складе
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * Устанавливает количество товара на складе.
     *
     * @param stock количество на складе
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * Возвращает бренд товара.
     *
     * @return бренд товара
     */
    public Brand getBrand() {
        return brand;
    }

    /**
     * Устанавливает бренд товара.
     *
     * @param brand бренд товара
     */
    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    /**
     * Возвращает категорию товара.
     *
     * @return категория товара
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Устанавливает категорию товара.
     *
     * @param category категория товара
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Возвращает URL изображения товара.
     *
     * @return URL изображения
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Устанавливает URL изображения товара.
     *
     * @param imageUrl URL изображения
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }

    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    public String getImageUrl4() {
        return imageUrl4;
    }

    public void setImageUrl4(String imageUrl4) {
        this.imageUrl4 = imageUrl4;
    }

    /**
     * Возвращает список отзывов на товар.
     *
     * @return список отзывов
     */
    public List<Review> getReviews() {
        return reviews;
    }

    /**
     * Устанавливает список отзывов на товар.
     *
     * @param reviews список отзывов
     */
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    /**
     * Возвращает список заказов, содержащих этот товар.
     *
     * @return список заказов
     */
    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Устанавливает список заказов, содержащих этот товар.
     *
     * @param orders список заказов
     */
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    /**
     * Возвращает средний рейтинг товара.
     *
     * @return средний рейтинг (0.00 - 5.00)
     */
    public BigDecimal getAverageRating() {
        return averageRating;
    }

    /**
     * Устанавливает средний рейтинг товара.
     *
     * @param averageRating средний рейтинг
     */
    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * Возвращает количество отзывов на товар.
     *
     * @return количество отзывов
     */
    public Integer getReviewCount() {
        return reviewCount;
    }

    /**
     * Устанавливает количество отзывов на товар.
     *
     * @param reviewCount количество отзывов
     */
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
}