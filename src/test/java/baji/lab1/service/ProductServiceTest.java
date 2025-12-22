package baji.lab1.service;

import baji.lab1.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    public ProductService productService;

    private final Product testProduct = new Product(null, "test", 12.5, "test",
            45, null, null, "test", null);

    @Test
    public void testSaveProduct_Success() {
        Product savedProduct = productService.save(testProduct);

        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());

        productService.deleteById(savedProduct.getId());
    }

    @Test
    public void testDeleteById_Success() {
        Product savedProduct = productService.save(testProduct);
        productService.deleteById(savedProduct.getId());

        Product product = productService.findById(savedProduct.getId());
        assertNull(product);
    }
}