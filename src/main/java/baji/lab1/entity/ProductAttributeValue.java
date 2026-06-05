package baji.lab1.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

//Значение атрибута товара
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_attribute_values")
public class ProductAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "attribute_template_id", nullable = false)
    private CategoryAttributeTemplate attributeTemplate;

    @Column(columnDefinition = "TEXT")
    private String value;
}