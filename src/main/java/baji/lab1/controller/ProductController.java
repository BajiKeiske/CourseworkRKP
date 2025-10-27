package baji.lab1.controller;

import baji.lab1.dto.ProductCreateDto;
import baji.lab1.dto.ProductEditDto;
import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import baji.lab1.repository.CategoryRepository;
import baji.lab1.repository.BrandRepository;

import java.util.List;
import java.util.Optional;



@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    // главная страница
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

    // форма создания
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductCreateDto());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        return "add_product";
    }

    // сохранить новый товар
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("product") ProductCreateDto productDto,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "add_product";
        }

        //  DTO в Entity
        Product product = new Product();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setStock(productDto.getStock());

        product.setCategory(categoryRepository.findById(productDto.getCategoryId()).get());
        product.setBrand(brandRepository.findById(productDto.getBrandId()).get());

        productRepository.save(product);
        return "redirect:/products/main";
    }

    // форма редактирования
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/products/main";
        }

        Product product = optionalProduct.get();
        // Entity в DTO
        ProductEditDto productDto = new ProductEditDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStock()
        );
        model.addAttribute("categories", categoryRepository.findAll()); // ← ДОБАВИТЬ
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("product", productDto);
        return "edit_product";
    }

    // обновить товар
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("product") ProductEditDto productDto,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "edit_product";
        }

        // DTO в Entity
        Product product = new Product();
        product.setId(productDto.getId());
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setStock(productDto.getStock());

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