package baji.lab1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bundles")
@Getter
@Setter
public class Bundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;

    private String discountType;

    // значение скидки
    private Double discountValue;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BundleItem> items = new ArrayList<>();

    @Transient
    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
    }

    @Transient
    public double getFinalPrice() {
        double total = getTotalPrice();

        if (discountType == null || discountValue == null) {
            return total;
        }

        if ("PERCENT".equals(discountType)) {
            return total - (total * discountValue / 100.0);
        }

        if ("FIXED".equals(discountType)) {
            return total - discountValue;
        }

        return total;
    }
}