package baji.lab1.controller;

import baji.lab1.dto.ProductCreateDto;
import baji.lab1.dto.ProductEditDto;
import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Product;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.CategoryRepository;
import baji.lab1.repository.BrandRepository;
import baji.lab1.service.ExcelReportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ExcelReportService excelReportService;


    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("product") ProductCreateDto productDto,
                         BindingResult result, Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/add_product";
        }

        // 1. Сохраняем изображение
        String imageUrl = "/images/products/placeholder.png";

        if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
            // Получаем расширение файла
            String originalFileName = productDto.getImageFile().getOriginalFilename();
            String fileExtension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Генерируем уникальное имя файла
            String fileName = "product_" + System.currentTimeMillis() + fileExtension;

            // ===== ОСНОВНОЙ ПУТЬ: target (рабочая папка Spring) =====
            String targetDir = "target/classes/static/images/products/";
            Path targetPath = Paths.get(targetDir).toAbsolutePath().normalize();

            System.out.println("=== НАЧАЛО СОХРАНЕНИЯ ===");
            System.out.println("Рабочая директория: " + System.getProperty("user.dir"));
            System.out.println("Путь target: " + targetPath);

            // Создаем папку если не существует
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
                System.out.println("Создана папка target: " + targetPath);
            }

            // Сохраняем файл в target
            Path targetFilePath = targetPath.resolve(fileName);

            try (InputStream inputStream = productDto.getImageFile().getInputStream()) {
                Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ Файл сохранен в target: " + targetFilePath);
            }

            // ===== КОПИРУЕМ В src для истории =====
            String srcDir = "src/main/resources/static/images/products/";
            Path srcPath = Paths.get(srcDir).toAbsolutePath().normalize();

            if (!Files.exists(srcPath)) {
                Files.createDirectories(srcPath);
                System.out.println("Создана папка src: " + srcPath);
            }

            Path srcFilePath = srcPath.resolve(fileName);
            Files.copy(targetFilePath, srcFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Файл скопирован в src: " + srcFilePath);

            // URL для БД
            imageUrl = "/images/products/" + fileName;
            System.out.println(" URL изображения: " + imageUrl);
            System.out.println("=== СОХРАНЕНИЕ ЗАВЕРШЕНО ===");
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
        System.out.println("✅ Товар сохранен в БД с ID: " + product.getId());

        return "redirect:/admin/products";
    }

    @GetMapping("")
    public String manageProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("allBrands", brandRepository.findAll());
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
        model.addAttribute("currentProduct", product); // ДОБАВИТЬ ЭТО
        return "admin/edit_product";
    }

    // обновить товар с возможностью изменения картинки
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("product") ProductEditDto productDto,
                         BindingResult result, Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/edit_product";
        }

        Product existingProduct = productRepository.findById(productDto.getId()).orElseThrow();

        // Обновляем основную информацию
        existingProduct.setName(productDto.getName());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setStock(productDto.getStock());
        existingProduct.setCategory(categoryRepository.findById(productDto.getCategoryId()).orElseThrow());
        existingProduct.setBrand(brandRepository.findById(productDto.getBrandId()).orElseThrow());

        // ОБРАБОТКА НОВОЙ КАРТИНКИ
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

            // Создаем папку если её нет
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Сохраняем файл
            Path filePath = uploadPath.resolve(fileName);
            productDto.getImageFile().transferTo(filePath.toFile());

            // Обновляем URL в БД
            existingProduct.setImageUrl("/images/products/" + fileName);
        }

        productRepository.save(existingProduct);
        return "redirect:/admin/products";
    }

    // удалить товар
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(id).orElse(null);
            if (product != null) {
                // Проверяем, есть ли заказы
                if (product.getOrders() != null && !product.getOrders().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Нельзя удалить товар '" + product.getName() +
                                    "', так как он присутствует в " + product.getOrders().size() + " заказах!");
                } else {
                    productRepository.deleteById(id);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Товар '" + product.getName() + "' удален!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении: " + e.getMessage());
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
        model.addAttribute("allBrands", brandRepository.findAll());
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

    //    отчеты
    @GetMapping("/report/excel")
    public void downloadExcelReport(HttpServletResponse response) throws IOException {
        byte[] excelBytes = excelReportService.generateExcelReport();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=vinyl_shop_report_" + LocalDate.now() + ".xlsx");
        response.setContentLength(excelBytes.length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(excelBytes);
            os.flush();
        }
    }
}