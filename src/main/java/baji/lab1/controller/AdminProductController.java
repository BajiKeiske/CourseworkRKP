package baji.lab1.controller;

import baji.lab1.dto.ProductCreateDto;
import baji.lab1.dto.ProductEditDto;
import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.*;
import baji.lab1.repository.*;
import baji.lab1.service.CategoryService;
import baji.lab1.service.EmailService;
import baji.lab1.service.ExcelReportService;
import baji.lab1.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.*;
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
    @Autowired
    private BundleRepository bundleRepository;
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryAttributeTemplateRepository categoryAttributeTemplateRepository;

    @Autowired
    private ProductAttributeValueRepository productAttributeValueRepository;

    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private EmailService emailService;

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("product") ProductCreateDto productDto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // Если ошибки валидации
        if (result.hasErrors()) {
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("categoryTree", categoryService.getCategoryTree());
            model.addAttribute("categoryAttributes", new ArrayList<>());
            redirectAttributes.addFlashAttribute("errorMessage", "Проверьте правильность заполнения полей");
            return "admin/add_product";
        }

        try {
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

            Category category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
            if (category == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Категория не найдена");
                return "redirect:/admin/products/create";
            }
            product.setCategory(category);

            Brand brand = brandRepository.findById(productDto.getBrandId()).orElse(null);
            if (brand == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Бренд не найден");
                return "redirect:/admin/products/create";
            }
            product.setBrand(brand);

            // Сохраняем фото
            if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
                String imageUrl = saveImage(productDto.getImageFile(), uploadPath);
                if (imageUrl != null) product.setImageUrl(imageUrl);
            }
            if (productDto.getImageFile2() != null && !productDto.getImageFile2().isEmpty()) {
                String imageUrl = saveImage(productDto.getImageFile2(), uploadPath);
                if (imageUrl != null) product.setImageUrl2(imageUrl);
            }
            if (productDto.getImageFile3() != null && !productDto.getImageFile3().isEmpty()) {
                String imageUrl = saveImage(productDto.getImageFile3(), uploadPath);
                if (imageUrl != null) product.setImageUrl3(imageUrl);
            }
            if (productDto.getImageFile4() != null && !productDto.getImageFile4().isEmpty()) {
                String imageUrl = saveImage(productDto.getImageFile4(), uploadPath);
                if (imageUrl != null) product.setImageUrl4(imageUrl);
            }

            productRepository.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "Товар успешно добавлен");
            return "redirect:/admin/products";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    @GetMapping("")
    public String manageProducts(@RequestParam(required = false) Long categoryId, Model model) {
        List<Product> products;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                List<Long> categoryIds = getAllCategoryIds(category);
                products = productRepository.findByCategoryIdIn(categoryIds);
            } else {
                products = productRepository.findAll();
            }
        } else {
            products = productRepository.findAll();
        }
        model.addAttribute("products", products);
        model.addAttribute("allBrands", brandRepository.findAll());
        model.addAttribute("rootCategories", categoryRepository.findByParentIsNull());
        return "admin/products";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductCreateDto());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("categoryTree", categoryService.getCategoryTree());
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

        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("product", productDto);
        model.addAttribute("currentProduct", product);
        model.addAttribute("categoryTree", categoryService.getCategoryTree());

        // Загружаем существующие атрибуты товара
        Map<Long, String> existingAttributes = new HashMap<>();
        for (ProductAttributeValue pav : productAttributeValueRepository.findByProduct(product)) {
            existingAttributes.put(pav.getAttributeTemplate().getId(), pav.getValue());
        }
        productDto.setAttributeValues(existingAttributes);

        // Загружаем атрибуты категории для отображения в форме
        model.addAttribute("categoryAttributes", product.getCategory().getAttributeTemplates());

        return "admin/edit_product";
    }

    // обновить товар с возможностью изменения картинки
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("product") ProductEditDto productDto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("categoryTree", categoryService.getCategoryTree());  // ЭТО КЛЮЧЕВОЕ
            model.addAttribute("categoryAttributes", new ArrayList<>());

            Product currentProduct = productRepository.findById(productDto.getId()).orElse(null);
            model.addAttribute("currentProduct", currentProduct);
            return "admin/edit_product";
        }

        Product product =
                productRepository.findById(productDto.getId()).orElseThrow();

        Integer oldStock = product.getStock();

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

        // обновляем атрибуты
        productAttributeValueRepository.deleteByProduct(product);

        if (productDto.getAttributeValues() != null) {
            for (CategoryAttributeTemplate template :
                    product.getCategory().getAttributeTemplates()) {

                String value = productDto.getAttributeValues().get(template.getId());

                if (value != null && !value.trim().isEmpty()) {
                    ProductAttributeValue attrValue = new ProductAttributeValue();
                    attrValue.setProduct(product);
                    attrValue.setAttributeTemplate(template);
                    attrValue.setValue(value);
                    productAttributeValueRepository.save(attrValue);
                }
            }
        }

        //  EMAIL УВЕДОМЛЕНИЕ (товар снова в наличии)
        boolean becameAvailable =
                oldStock != null && oldStock <= 0 && product.getStock() > 0;

        if (becameAvailable) {

            List<Wishlist> wishlists = wishlistRepository.findByProducts_Id(product.getId());

            for (Wishlist wishlist : wishlists) {

                boolean hasProduct = wishlist.getProducts()
                        .stream()
                        .anyMatch(p -> p.getId().equals(product.getId()));

                if (hasProduct && wishlist.getUser().getEmail() != null) {

                    emailService.sendEmail(
                            wishlist.getUser().getEmail(),
                            "Товар снова в наличии",
                            "Товар \"" + product.getName() + "\" снова доступен!"
                    );
                }
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Товар успешно отредактирован");

        return "redirect:/admin/products";
    }

    // удалить товар
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {

        try {
            productService.deleteProduct(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Товар удален"
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
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

        List<Bundle> bundles = bundleRepository.findByProductId(id);
        model.addAttribute("bundles", bundles);

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

    @PostMapping("/bundles/create")
    public String createBundle(@ModelAttribute Bundle bundle,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        bundle.setStartDate(startDate);
        bundle.setEndDate(endDate);
        bundleRepository.save(bundle);
        return "redirect:/admin/bundles";
    }

    @GetMapping("/bundles/{id}")
    public String bundleDetails(@PathVariable Long id, Model model) {

        Bundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        model.addAttribute("bundle", bundle);

        return "bundle_details";
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

        String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!extension.equals(".jpg") && !extension.equals(".jpeg") && !extension.equals(".png") && !extension.equals(".webp")) {
            throw new IllegalArgumentException("Разрешены только JPG, JPEG, PNG и WEBP");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5 МБ. Выберите другое изображение.");
        }

        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/images/products/" + fileName;
    }


    @GetMapping("/categories/{categoryId}/attributes")
    @ResponseBody
    public List<CategoryAttributeTemplate> getCategoryAttributes(@PathVariable Long categoryId) {
        return categoryAttributeTemplateRepository.findByCategoryId(categoryId);
    }

    private List<Long> getAllCategoryIds(Category category) {
        List<Long> ids = new ArrayList<>();
        ids.add(category.getId());
        for (Category child : category.getChildren()) {
            ids.addAll(getAllCategoryIds(child));
        }
        return ids;
    }
}