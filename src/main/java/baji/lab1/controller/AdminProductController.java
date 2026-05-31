package baji.lab1.controller;

import baji.lab1.dto.ProductCreateDto;
import baji.lab1.dto.ProductEditDto;
import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Product;
import baji.lab1.repository.OrderItemRepository;
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
import org.springframework.web.multipart.MultipartFile;
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
    @Autowired
    private OrderItemRepository orderItemRepository;


    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("product") ProductCreateDto productDto,
                         BindingResult result,
                         Model model) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/add_product";
        }

        String uploadDir = System.getProperty("user.dir") + "/uploads/products/";

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Product product = new Product();

        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setStock(productDto.getStock());

        product.setCategory(
                categoryRepository.findById(productDto.getCategoryId()).orElseThrow()
        );

        product.setBrand(
                brandRepository.findById(productDto.getBrandId()).orElseThrow()
        );

        product.setImageUrl(saveImage(productDto.getImageFile(), uploadPath));
        product.setImageUrl2(saveImage(productDto.getImageFile2(), uploadPath));
        product.setImageUrl3(saveImage(productDto.getImageFile3(), uploadPath));
        product.setImageUrl4(saveImage(productDto.getImageFile4(), uploadPath));

        productRepository.save(product);

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
                         BindingResult result,
                         Model model) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());

            Product currentProduct =
                    productRepository.findById(productDto.getId()).orElse(null);

            model.addAttribute("currentProduct", currentProduct);

            return "admin/edit_product";
        }

        Product product =
                productRepository.findById(productDto.getId()).orElseThrow();

        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setStock(productDto.getStock());

        product.setCategory(
                categoryRepository.findById(productDto.getCategoryId()).orElseThrow()
        );

        product.setBrand(
                brandRepository.findById(productDto.getBrandId()).orElseThrow()
        );

        String uploadDir = System.getProperty("user.dir") + "/uploads/products/";

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
            product.setImageUrl(saveImage(productDto.getImageFile(), uploadPath));
        }

        if (productDto.getImageFile2() != null && !productDto.getImageFile2().isEmpty()) {
            product.setImageUrl2(saveImage(productDto.getImageFile2(), uploadPath));
        }

        if (productDto.getImageFile3() != null && !productDto.getImageFile3().isEmpty()) {
            product.setImageUrl3(saveImage(productDto.getImageFile3(), uploadPath));
        }

        if (productDto.getImageFile4() != null && !productDto.getImageFile4().isEmpty()) {
            product.setImageUrl4(saveImage(productDto.getImageFile4(), uploadPath));
        }

        productRepository.save(product);

        return "redirect:/admin/products";
    }

    // удалить товар
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id,
                         RedirectAttributes redirectAttributes) {

        try {

            Product product = productRepository.findById(id).orElse(null);

            if (product != null) {

                // Проверяем, есть ли товар в заказах через OrderItem
                boolean isInOrders = orderItemRepository.existsByProductId(id);

                if (isInOrders) {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage",
                            "Нельзя удалить товар '" + product.getName() + "', так как он присутствует в заказах!"
                    );
                } else {
                    deleteImage(product.getImageUrl());
                    deleteImage(product.getImageUrl2());
                    deleteImage(product.getImageUrl3());
                    deleteImage(product.getImageUrl4());

                    productRepository.deleteById(id);

                    redirectAttributes.addFlashAttribute(
                            "successMessage",
                            "Товар '" + product.getName() + "' удален!"
                    );
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Ошибка при удалении: " + e.getMessage()
            );
        }

        return "redirect:/admin/products";
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {

            String fileName = imageUrl.replace("/images/products/", "");

            Path filePath = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    "products",
                    fileName
            );

            Files.deleteIfExists(filePath);

        } catch (Exception ignored) {
        }
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

    //сохранение изображений
    private String saveImage(MultipartFile file, Path uploadPath) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("Некорректное имя файла");
        }

        String extension = originalName
                .substring(originalName.lastIndexOf("."))
                .toLowerCase();

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".webp")) {

            throw new IllegalArgumentException(
                    "Разрешены только JPG, JPEG, PNG и WEBP"
            );
        }
        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png")
                        && !contentType.equals("image/webp"))) {

            throw new IllegalArgumentException(
                    "Разрешены только изображения"
            );
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Размер файла не должен превышать 5 МБ"
            );
        }

        String fileName =
                System.currentTimeMillis()
                        + "_"
                        + java.util.UUID.randomUUID()
                        + extension;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );
        return "/images/products/" + fileName;
    }
}