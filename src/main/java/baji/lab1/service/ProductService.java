package baji.lab1.service;

import baji.lab1.entity.Basket;
import baji.lab1.entity.Product;
import baji.lab1.entity.User;
import baji.lab1.entity.Wishlist;
import baji.lab1.repository.BasketRepository;
import baji.lab1.repository.BundleRepository;
import baji.lab1.repository.OrderItemRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final BasketRepository basketRepository;
    private final BundleRepository bundleRepository;
    private final WishlistRepository wishlistRepository;
    private final EmailService emailService;

    // ========= НАЙТИ ТОВАР ПО ID =========
    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // ========= СОХРАНИТЬ ТОВАР =========
    public Product save(Product product) {
        return productRepository.save(product);
    }

    // ========= УДАЛИТЬ ТОВАР ПО ID =========
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    // ========= УДАЛИТЬ ТОВАР С ПРОВЕРКАМИ =========
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // 1. Проверка: есть ли товар в заказах?
        if (orderItemRepository.existsByProductId(id)) {
            throw new RuntimeException("Нельзя удалить товар: он есть в заказах");
        }

        // 2. Проверка: используется ли товар в комплектах?
        if (!bundleRepository.findByProductId(id).isEmpty()) {
            throw new RuntimeException("Нельзя удалить товар: он используется в наборах");
        }

        // 3. Удаляем товар из ВСЕХ корзин (из cartItems, где есть этот товар)
        List<Basket> baskets = basketRepository.findAll();
        for (Basket basket : baskets) {
            // Удаляем позиции, где товар = наш продукт
            basket.getCartItems().removeIf(item ->
                    "PRODUCT".equals(item.getType())
                            && item.getProduct() != null
                            && item.getProduct().getId().equals(id)
            );
        }
        basketRepository.saveAll(baskets);

        // 4. Удаляем сам товар
        productRepository.delete(product);
    }

    // ========= НОВЫЕ ТОВАРЫ (для главной страницы) =========
    public List<Product> getNewProducts() {
        return productRepository.findTop4ByOrderByIdDesc();
    }

    // ========= ОБНОВЛЕНИЕ КОЛИЧЕСТВА НА СКЛАДЕ =========
    @Transactional
    public Product updateStock(Long productId, int newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        int oldStock = product.getStock();
        product.setStock(newStock);
        Product saved = productRepository.save(product);

        // Если товар БЫЛ в наличии 0, а СТАЛ > 0 — уведомляем всех, у кого в избранном
        if (oldStock == 0 && newStock > 0) {
            notifyUsersWhoWishlisted(saved);
        }

        return saved;
    }

    // ========= УВЕДОМЛЕНИЕ ПОЛЬЗОВАТЕЛЯМ О ПОЯВЛЕНИИ ТОВАРА =========
    private void notifyUsersWhoWishlisted(Product product) {
        // Находим все списки избранного, где есть этот товар
        List<Wishlist> wishlists = wishlistRepository.findByProducts_Id(product.getId());

        for (Wishlist wishlist : wishlists) {
            User user = wishlist.getUser();
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendProductBackInStock(user.getEmail(), user.getUsername(), product);
            }
        }
    }
}