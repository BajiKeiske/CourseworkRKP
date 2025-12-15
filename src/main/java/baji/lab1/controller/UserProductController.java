package baji.lab1.controller;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user/products")
public class UserProductController {

    @Autowired
    private ProductRepository productRepository;

    // Каталог для пользователей
    @GetMapping("/catalog")
    public String catalog(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "user/catalog";
    }

    // Детали товара для пользователей
    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/user/products/catalog";
        }
        model.addAttribute("product", optionalProduct.get());
        model.addAttribute("reviewDto", new ReviewCreateDto());
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
        return "user/catalog";
    }
}