package baji.lab1.service;

import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testSaveProduct_Success() {
        Product product = new Product();
        product.setName("Тестовый товар");
        product.setPrice(1000.0);
        product.setStock(10);

        Product saved = productService.save(product);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Тестовый товар", saved.getName());

        productService.deleteById(saved.getId());
    }

    @Test
    public void testDeleteById_Success() {
        Product product = new Product();
        product.setName("Тестовый для удаления");
        product.setPrice(500.0);
        product.setStock(5);

        Product saved = productService.save(product);
        Long id = saved.getId();

        productService.deleteById(id);

        Product deleted = productService.findById(id);
        assertNull(deleted);
    }
}