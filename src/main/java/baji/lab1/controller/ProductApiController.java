package baji.lab1.controller;

import baji.lab1.dto.ProductCreateDto;
import baji.lab1.dto.ProductEditDto;
import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.CategoryRepository;
import baji.lab1.repository.BrandRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    // GET все товары
    @GetMapping
    public List<Product> getAllProducts() {
        return (List<Product>) productRepository.findAll();
    }

    // GET товар по ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST создать товар
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateDto productDto) {
        try {
            Product product = new Product();
            product.setName(productDto.getName());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            product.setStock(productDto.getStock());

            // Находим бренд и категорию
            product.setBrand(brandRepository.findById(productDto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Бренд не найден")));
            product.setCategory(categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена")));

            Product savedProduct = productRepository.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT обновить товар
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductEditDto productDto) {
        try {
            Optional<Product> optionalProduct = productRepository.findById(id);
            if (optionalProduct.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Product product = optionalProduct.get();
            product.setName(productDto.getName());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            product.setStock(productDto.getStock());

            // Обновляем бренд и категорию
            product.setBrand(brandRepository.findById(productDto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Бренд не найден")));
            product.setCategory(categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена")));

            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE удалить товар
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Функция +10р к цене
    @GetMapping("/{id}/add-price")
    public ResponseEntity<Product> addPrice(@PathVariable Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = optionalProduct.get();
        product.setPrice(product.getPrice() + 10);
        Product updatedProduct = productRepository.save(product);

        return ResponseEntity.ok(updatedProduct);
    }
}