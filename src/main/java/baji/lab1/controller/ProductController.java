package baji.lab1.controller;

import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    // Главная страница
    @GetMapping("/main")
    public String mainPage(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "main";
    }

    // детали товара
    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/products/main";
        }
        model.addAttribute("product", optionalProduct.get());
        return "details";
    }

    // форма добавления товара
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "add_product";
    }

    // принять данные из формы добавления
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Product product, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add_product"; // Возвращаем форму с ошибками
        }
        productRepository.save(product);
        return "redirect:/products/main";
    }

    // форма редактирования товара
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/products/main";
        }
        model.addAttribute("product", optionalProduct.get());
        return "edit_product";
    }

    // принять данные
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute Product product, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "edit_product"; // Возвращаем форму с ошибками
        }
        productRepository.save(product);
        return "redirect:/products/main";
    }

    // удалить
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
        return "redirect:/products/main";
    }

    // поиск
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
            results = (List<Product>) productRepository.findAll();
        }

        model.addAttribute("products", results);
        return "main";
    }
}
