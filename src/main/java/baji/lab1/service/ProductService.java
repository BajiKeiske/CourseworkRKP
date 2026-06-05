package baji.lab1.service;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Product;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.BundleRepository;
import baji.lab1.repository.OrderItemRepository;
import baji.lab1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private BasketRepository basketRepository;
    @Autowired
    private BundleRepository bundleRepository;

    public ProductService() {}

    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // 1. БЛОК: есть ли в заказах
        if (orderItemRepository.existsByProductId(id)) {
            throw new RuntimeException("Нельзя удалить товар: он есть в заказах");
        }
        if (!bundleRepository.findByProductId(id).isEmpty()) {
            throw new RuntimeException(
                    "Нельзя удалить товар: он используется в наборах"
            );
        }

        // 2. УДАЛЯЕМ ИЗ КОРЗИН ВСЕХ ПОЛЬЗОВАТЕЛЕЙ
        List<Basket> baskets = basketRepository.findAll();

        for (Basket basket : baskets) {
            basket.getItems().remove(product);
        }

        basketRepository.saveAll(baskets);

        // 3. УДАЛЯЕМ КАРТИНКИ

        productRepository.delete(product);
    }
}
