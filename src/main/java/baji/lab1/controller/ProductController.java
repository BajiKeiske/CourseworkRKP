package baji.lab1.controller;

import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/main")
    public String mainPage(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "main";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/products/main";
        }
        model.addAttribute("product", optionalProduct.get());
        return "details";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "add_product";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/products/main";
    }

    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/products/main";
        }
        model.addAttribute("product", optionalProduct.get());
        return "edit_product";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/products/main";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
        return "redirect:/products/main";
    }
}