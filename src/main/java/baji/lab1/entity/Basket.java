package baji.lab1.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "basket")
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ЕДИНСТВЕННОЕ хранилище товаров в корзине
    // Сюда попадают и обычные товары (type = "PRODUCT")
    // И комплекты (type = "BUNDLE")
    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BasketItem> cartItems = new ArrayList<>();

    // ========= МЕТОДЫ ДЛЯ РАБОТЫ =========

    // Добавить обычный товар
    public void addProductItem(Product product, int quantity, double price) {
        BasketItem item = new BasketItem();
        item.setBasket(this);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPriceAtAdd(price);
        item.setType("PRODUCT");
        cartItems.add(item);
    }

    // Добавить комплект
    public void addBundleItem(Bundle bundle, int quantity, double price) {
        BasketItem item = new BasketItem();
        item.setBasket(this);
        item.setBundle(bundle);
        item.setQuantity(quantity);
        item.setPriceAtAdd(price);
        item.setType("BUNDLE");
        cartItems.add(item);
    }

    // Очистить всю корзину
    public void clear() {
        cartItems.clear();
    }

    // Посчитать общую сумму
    public double getTotalAmount() {
        return cartItems.stream()
                .mapToDouble(i -> i.getPriceAtAdd() * i.getQuantity())
                .sum();
    }

    // ========= ГЕТТЕРЫ И СЕТТЕРЫ =========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<BasketItem> getCartItems() { return cartItems; }
    public void setCartItems(List<BasketItem> cartItems) { this.cartItems = cartItems; }

    public Basket() {}
}