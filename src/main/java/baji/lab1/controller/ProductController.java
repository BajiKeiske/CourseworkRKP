package baji.lab1.controller;

import baji.lab1.dto.ProductCreateDto;
import baji.lab1.dto.ProductEditDto;
import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.CategoryRepository;
import baji.lab1.repository.BrandRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;
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

    // Главная страница товаров
    @GetMapping("/main")
    public String mainPage(org.springframework.security.core.Authentication authentication, Model model) {
        model.addAttribute("products", productRepository.findAll());

        if (authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            return "admin/products";
        } else {
            return "user/catalog";
        }
    }

    // Детали товара
    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/products/main";
        }
        model.addAttribute("product", optionalProduct.get());
        return "details";
    }

    // Форма создания товара
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductCreateDto());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        return "admin/add_product";
    }

    // Сохранить новый товар
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("product") ProductCreateDto productDto,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/add_product";
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setStock(productDto.getStock());
        product.setCategory(categoryRepository.findById(productDto.getCategoryId()).orElseThrow());
        product.setBrand(brandRepository.findById(productDto.getBrandId()).orElseThrow());

        productRepository.save(product);
        return "redirect:/products/main";
    }

    // Форма редактирования товара
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/products/main";
        }

        Product product = optionalProduct.get();
        ProductEditDto productDto = new ProductEditDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPrice(product.getPrice());
        productDto.setDescription(product.getDescription());
        productDto.setStock(product.getStock());
        productDto.setCategoryId(product.getCategory().getId());
        productDto.setBrandId(product.getBrand().getId());

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("product", productDto);
        return "admin/edit_product";
    }

    // Обновить товар
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("product") ProductEditDto productDto,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/edit_product";
        }

        Product existingProduct = productRepository.findById(productDto.getId()).orElseThrow();
        existingProduct.setName(productDto.getName());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setStock(productDto.getStock());
        existingProduct.setCategory(categoryRepository.findById(productDto.getCategoryId()).orElseThrow());
        existingProduct.setBrand(brandRepository.findById(productDto.getBrandId()).orElseThrow());

        productRepository.save(existingProduct);
        return "redirect:/products/main";
    }

    // Удалить товар
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
        return "redirect:/products/main";
    }

    // Поиск товаров
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

        // Определяем какую страницу показать
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"))) {
            return "admin/products";
        } else {
            return "user/catalog";
        }
    }
}