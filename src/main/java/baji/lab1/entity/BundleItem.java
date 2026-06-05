package baji.lab1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bundle_items")
public class BundleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Bundle bundle;

    @ManyToOne
    private Product product;

    private Integer quantity;

    public BundleItem() {}

    public Long getId() { return id; }

    public Bundle getBundle() { return bundle; }
    public void setBundle(Bundle bundle) { this.bundle = bundle; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}