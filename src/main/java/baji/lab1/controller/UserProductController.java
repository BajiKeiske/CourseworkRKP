package baji.lab1.controller;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Bundle;
import baji.lab1.entity.Category;
import baji.lab1.entity.Product;
import baji.lab1.entity.User;
import baji.lab1.repository.*;
import baji.lab1.service.BasketService;
import baji.lab1.service.ReviewService;
import baji.lab1.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import baji.lab1.entity.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user/products")
public class UserProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BundleRepository bundleRepository;

    @Autowired
    private BasketService basketService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired private WishlistService wishlistService;

    private List<Long> getAllCategoryIds(Category category) {
        List<Long> ids = new ArrayList<>();
        ids.add(category.getId());
        for (Category child : category.getChildren()) {
            ids.addAll(getAllCategoryIds(child));
        }
        return ids;
    }

    @GetMapping("/catalog")
    public String catalog(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("rootCategories", categoryRepository.findByParentIsNull());

        Specification<Product> spec = (root, query, cb) -> null;

        if (categoryId != null) {
            Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
            if (optionalCategory.isPresent()) {
                List<Long> categoryIds = getAllCategoryIds(optionalCategory.get());
                spec = spec.and((root, query, cb) ->
                        root.get("category").get("id").in(categoryIds));
            }
        }

        if (brandId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("brand").get("id"), brandId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        // Получаем все товары по спецификации
        List<Product> allProducts = productRepository.findAll(spec);

        // Сортируем: сначала в наличии (stock > 0), потом нет в наличии
        allProducts.sort((p1, p2) -> {
            int stockCompare = Boolean.compare(p2.getStock() > 0, p1.getStock() > 0);
            if (stockCompare != 0) return stockCompare;

            // Если статус наличия одинаковый, сортируем по выбранному полю
            int result = 0;
            if (sort.equals("name")) {
                result = p1.getName().compareToIgnoreCase(p2.getName());
            } else if (sort.equals("price")) {
                result = p1.getPrice().compareTo(p2.getPrice());
            }
            return direction.equals("asc") ? result : -result;
        });

        // Пагинация вручную
        int start = page * size;
        int end = Math.min(start + size, allProducts.size());
        List<Product> pagedProducts = allProducts.subList(start, end);

        model.addAttribute("products", pagedProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allProducts.size() / size));
        model.addAttribute("totalItems", allProducts.size());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "user/catalog";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id,
                          Authentication authentication,
                          Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/user/products/catalog";
        }

        Product product = optionalProduct.get();
        model.addAttribute("product", product);
        model.addAttribute("reviewDto", new ReviewCreateDto());
        model.addAttribute("approvedReviews", reviewService.getApprovedReviewsByProduct(product.getId()));

        boolean inWishlist = false;
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                inWishlist = wishlistService.isInWishlist(user, id);
            }
        }
        model.addAttribute("inWishlist", inWishlist);
        boolean hasPurchased = false;
        Review userReview = null;

        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                // Проверка, покупал ли товар (только заказы со статусом COMPLETED)
                if (user.getOrders() != null) {
                    hasPurchased = user.getOrders().stream()
                            .filter(order -> order.getStatus() == baji.lab1.entity.OrderStatus.COMPLETED)
                            .flatMap(order -> order.getOrderItems().stream())
                            .anyMatch(item -> item.getProduct().getId().equals(product.getId()));
                }
                // Проверка, оставлял ли отзыв
                userReview = reviewRepository.findByUserAndProduct(user, product).orElse(null);
            }
        }

        model.addAttribute("hasPurchased", hasPurchased);
        model.addAttribute("userReview", userReview);

        // Фильтруем комплекты: показываем только те, где все товары в наличии
        List<Bundle> allBundles = bundleRepository.findByProductId(product.getId());
        List<Bundle> availableBundles = new ArrayList<>();
        for (Bundle bundle : allBundles) {
            boolean allInStock = true;
            for (baji.lab1.entity.BundleItem item : bundle.getItems()) {
                if (item.getProduct().getStock() == null || item.getProduct().getStock() <= 0) {
                    allInStock = false;
                    break;
                }
            }
            if (allInStock) {
                availableBundles.add(bundle);
            }
        }
        model.addAttribute("bundles", availableBundles);

        return "details";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String query,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 @RequestParam(defaultValue = "name") String sort,
                                 @RequestParam(defaultValue = "asc") String direction,
                                 Model model) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Specification<Product> spec = (root, q, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + query.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("brand").get("name")), "%" + query.toLowerCase() + "%")
                );

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("rootCategories", categoryRepository.findByParentIsNull());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "user/catalog";
    }

    @GetMapping("/basket/size")
    @ResponseBody
    public int getBasketSize(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return 0;
        return basketService.getTotalQuantity(user);
    }
}