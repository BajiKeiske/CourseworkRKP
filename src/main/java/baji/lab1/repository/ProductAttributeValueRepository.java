package baji.lab1.repository;

import baji.lab1.entity.Product;
import baji.lab1.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    List<ProductAttributeValue> findByProduct(Product product);
    void deleteByProduct(Product product);
}