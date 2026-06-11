package baji.lab1.service;

import baji.lab1.entity.*;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;
    @Autowired
    private ProductRepository productRepository;

    public BasketService() {}

    /**
     * Найти корзину по ID
     */
    public Basket findById(Long id) {
        return basketRepository.findById(id).orElse(null);
    }

    /**
     * Найти корзину по ID пользователя
     */
    public Optional<Basket> findByUserId(Long id) {
        return basketRepository.findByUserId(id);
    }

    /**
     * Сохранить корзину
     */
    public Basket save(Basket basket) {
        return basketRepository.save(basket);
    }

    /**
     * Удалить корзину по ID
     */
    public void deleteById(Long id) {
        basketRepository.deleteById(id);
    }

    /**
     * ПОСЧИТАТЬ ОБЩЕЕ КОЛИЧЕСТВО ТОВАРОВ ДЛЯ БЕЙДЖА
     * Суммирует quantity у всех позиций (и товаров, и комплектов)
     */
    public int getTotalQuantity(User user) {
        Optional<Basket> basketOpt = basketRepository.findByUserId(user.getId());
        if (basketOpt.isEmpty()) return 0;
        Basket basket = basketOpt.get();

        // Считаем сумму quantity у всех позиций в cartItems
        return basket.getCartItems().stream()
                .mapToInt(BasketItem::getQuantity)
                .sum();
    }


    public void addProductToBasket(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (product.getStock() == null || product.getStock() <= 0) {
            throw new RuntimeException("Товар закончился");
        }

        Basket basket = basketRepository.findByUser(user).orElseGet(() -> {
            Basket b = new Basket();
            b.setUser(user);
            return b;
        });

        BasketItem existingItem = basket.getCartItems().stream()
                .filter(item -> "PRODUCT".equals(item.getType())
                        && item.getProduct() != null
                        && item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        int currentQty = existingItem != null ? existingItem.getQuantity() : 0;

        if (currentQty + quantity > product.getStock()) {
            throw new RuntimeException("В наличии осталось только " + product.getStock() + " шт.");
        }

        if (existingItem != null) {
            existingItem.setQuantity(currentQty + quantity);
        } else {
            basket.addProductItem(product, quantity, product.getPrice());
        }

        basketRepository.save(basket);
    }
}