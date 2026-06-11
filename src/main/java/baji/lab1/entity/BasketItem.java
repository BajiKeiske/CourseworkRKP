package baji.lab1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "basket_items")
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "basket_id")
    private Basket basket;

    // Ссылка на товар (если это обычный товар)
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Ссылка на комплект (если это комплект)
    @ManyToOne
    @JoinColumn(name = "bundle_id")
    private Bundle bundle;

    private int quantity;

    // Цена на момент добавления (со скидкой для комплекта)
    private double priceAtAdd;

    // Тип позиции: PRODUCT или BUNDLE
    private String type;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Basket getBasket() { return basket; }
    public void setBasket(Basket basket) { this.basket = basket; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Bundle getBundle() { return bundle; }
    public void setBundle(Bundle bundle) { this.bundle = bundle; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtAdd() { return priceAtAdd; }
    public void setPriceAtAdd(double priceAtAdd) { this.priceAtAdd = priceAtAdd; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BasketItem() {}
}