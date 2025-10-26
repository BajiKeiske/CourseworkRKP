package baji.lab1.repository;

import baji.lab1.entity.Product;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {
    // поиск по имени категории
    List<Product> findByCategoryName(String name);

    // поиск по имени бренда
    List<Product> findByBrandName(String name);

    // поиск по цене
    List<Product> findByPriceLessThanEqual(Double price);
}