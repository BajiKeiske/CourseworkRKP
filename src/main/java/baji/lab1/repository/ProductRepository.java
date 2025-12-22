package baji.lab1.repository;

import baji.lab1.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends
        JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // поиск по имени категории
    List<Product> findByCategoryName(String name);

    // поиск по бренду
    List<Product> findByBrandName(String name);

    // поиск по цене
    List<Product> findByPriceLessThanEqual(Double price);

    // поиск по названию
    List<Product> findByNameContainingIgnoreCase(String name);

    // поиск по количеству на складе
    List<Product> findByStockGreaterThanEqual(Integer stock);

    // поиск по части названия бренда (без учёта регистра)
    List<Product> findByBrandNameContainingIgnoreCase(String brandNamePart);

    // поиск по части названия категории (без учёта регистра)
    List<Product> findByCategoryNameContainingIgnoreCase(String categoryNamePart);
}