package baji.lab1.controller;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Bundle;
import baji.lab1.entity.Category;
import baji.lab1.entity.Product;
import baji.lab1.entity.User;
import baji.lab1.repository.*;
import baji.lab1.service.BasketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.security.Principal;

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

    private List<Long> getAllCategoryIds(Category category) {
        List<Long> ids = new ArrayList<>();
        ids.add(category.getId());
        for (Category child : category.getChildren()) {
            ids.addAll(getAllCategoryIds(child));
        }
        return ids;
    }

    // Каталог с фильтрацией и пагинацией
    @GetMapping("/catalog")
    public String catalog(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {

        // Получаем все категории и бренды для фильтров
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("rootCategories", categoryRepository.findByParentIsNull());

        // Создаем спецификацию для фильтрации
        Specification<Product> spec = (root, query, cb) -> null;
//        spec = spec.and((root, query, cb) ->
//                cb.greaterThan(root.get("stock"), 0));

        // Фильтрация по категории (включая подкатегории)
        if (categoryId != null) {
            Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
            if (optionalCategory.isPresent()) {
                List<Long> categoryIds = getAllCategoryIds(optionalCategory.get());
                spec = spec.and((root, query, cb) ->
                        root.get("category").get("id").in(categoryIds));
            }
        }

        // Фильтрация по бренду
        if (brandId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("brand").get("id"), brandId));
        }

        // Фильтрация по минимальной цене
        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        // Фильтрация по максимальной цене
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        // Пагинация
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        // Сохраняем параметры фильтрации для пагинации
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "user/catalog";
    }

    // Детали товара для пользователей
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

        // Проверка, покупал ли пользователь этот товар
        boolean hasPurchased = false;
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null && user.getOrders() != null) {
                hasPurchased = user.getOrders().stream()
                        .flatMap(order -> order.getOrderItems().stream())
                        .anyMatch(item -> item.getProduct().getId().equals(product.getId()));
            }
        }
        model.addAttribute("hasPurchased", hasPurchased);

        List<Bundle> bundles = bundleRepository.findByProductId(product.getId());

        model.addAttribute("bundles", bundles);

        return "details";
    }

    // Поиск товаров для пользователей
    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String brand,
                                 @RequestParam(required = false) Double maxPrice,
                                 Model model) {
        List<Product> results;

        if (name != null && !name.isEmpty()) {
            results = productRepository.findByNameContainingIgnoreCase(name);
        } else if (brand != null && !brand.isEmpty()) {
            results = productRepository.findByBrandName(brand);
        } else if (maxPrice != null) {
            results = productRepository.findByPriceLessThanEqual(maxPrice);
        } else {
            results = productRepository.findAll();
        }

        model.addAttribute("products", results);
        model.addAttribute("rootCategories", categoryRepository.findByParentIsNull());
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