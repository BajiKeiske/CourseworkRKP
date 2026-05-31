package baji.lab1.entity;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "basket")
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection
    @CollectionTable(name = "basket_items", joinColumns = @JoinColumn(name = "basket_id"))
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> items = new HashMap<>();

    public void addProduct(Product product, int quantity) {
        int current = items.getOrDefault(product, 0);
        int maxAdd = Math.min(quantity, product.getStock() - current);
        if (maxAdd > 0) {
            items.put(product, current + maxAdd);
        }
    }

    public Basket() {
    }

    public Basket(Long id, User user, Map<Product, Integer> items) {
        this.id = id;
        this.user = user;
        this.items = items;
    }

    public void removeProduct(Product product) {
        items.remove(product);
    }

    public void updateQuantity(Product product, int quantity) {
        if (quantity <= 0) {
            items.remove(product);
        } else {
            int validQuantity = Math.min(quantity, product.getStock());
            items.put(product, validQuantity);
        }
    }

    public void clear() {
        items.clear();
    }

    public int getQuantity(Product product) {
        return items.getOrDefault(product, 0);
    }

    public double getTotalPrice() {
        return items.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Map<Product, Integer> getItems() { return items; }
    public void setItems(Map<Product, Integer> items) { this.items = items; }
}