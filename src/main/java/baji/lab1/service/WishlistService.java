package baji.lab1.service;

import baji.lab1.entity.Product;
import baji.lab1.entity.User;
import baji.lab1.entity.Wishlist;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public void addToWishlist(User user, Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist w = new Wishlist();
                    w.setUser(user);
                    return wishlistRepository.save(w);
                });

        wishlist.getProducts().add(product);

        wishlistRepository.save(wishlist);
    }

    public void removeFromWishlist(User user, Long productId) {

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Избранное не найдено"));

        wishlist.getProducts()
                .removeIf(product -> product.getId().equals(productId));

        wishlistRepository.save(wishlist);
    }

    public boolean isInWishlist(User user, Long productId) {

        return wishlistRepository.findByUser(user)
                .map(wishlist -> wishlist.getProducts()
                        .stream()
                        .anyMatch(product -> product.getId().equals(productId)))
                .orElse(false);
    }

    public Wishlist getWishlist(User user) {

        return wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);
                    return wishlistRepository.save(wishlist);
                });
    }
}