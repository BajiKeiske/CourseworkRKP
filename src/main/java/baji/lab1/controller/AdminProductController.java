package baji.lab1.controller;

import baji.lab1.dto.ProductCreateDto;
import baji.lab1.dto.ProductEditDto;
import baji.lab1.dto.ReviewCreateDto;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;


    // СОЗДАНИЕ ТОВАРА (с загрузкой изображения)
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("product") ProductCreateDto productDto,
                         BindingResult result, Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/add_product";
        }

        // 1. Сохраняем изображение, если оно есть
        String imageUrl = null;
        if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
            String originalFileName = productDto.getImageFile().getOriginalFilename();
            String fileExtension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Уникальное имя файла
            String fileName = "product_" + System.currentTimeMillis() + fileExtension;

            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + "/src/main/resources/static/images/products/";

            // Создаем папку
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating directory: " + uploadPath);
                Files.createDirectories(uploadPath);
            }

            // Сохраняем файл
            Path filePath = uploadPath.resolve(fileName);
            System.out.println("Сохранить: " + filePath);
            productDto.getImageFile().transferTo(filePath.toFile());
            System.out.println("Файл загружен");

            // URL для сохранения в БД
            imageUrl = "/images/products/" + fileName;
        }

        // 2. Создаем продукт
        Product product = new Product();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setStock(productDto.getStock());
        product.setCategory(categoryRepository.findById(productDto.getCategoryId()).orElseThrow());
        product.setBrand(brandRepository.findById(productDto.getBrandId()).orElseThrow());
        product.setImageUrl(imageUrl);

        productRepository.save(product);
        return "redirect:/admin/products";
    }

    // Остальные методы остаются без изменений
    @GetMapping("")
    public String manageProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "admin/products";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductCreateDto());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        return "admin/add_product";
    }


    // редактирование товара
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/admin/products";
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

    // обновить товар
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
        return "redirect:/admin/products";
    }

    // удалить товар
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
        return "redirect:/admin/products";
    }


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
        return "admin/products";  // возвращаем админскую страницу
    }


    // просмотр
    @GetMapping("/details/{id}")
    public String viewProduct(@PathVariable("id") Long id, Model model) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/admin/products";
        }

        Product product = optionalProduct.get();
        model.addAttribute("product", product);
        model.addAttribute("reviewDto", new ReviewCreateDto());

        return "details";
    }
}