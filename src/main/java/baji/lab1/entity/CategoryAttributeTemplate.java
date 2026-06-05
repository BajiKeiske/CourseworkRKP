package baji.lab1.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

//Шаблон атрибутов категории
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "category_attribute_templates")
public class CategoryAttributeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String attributeType; // STRING, NUMBER, BOOLEAN

    private Boolean required = false;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}