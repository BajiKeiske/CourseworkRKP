package baji.lab1.controller;

import baji.lab1.dto.BundleCreateDto;
import baji.lab1.entity.Bundle;
import baji.lab1.entity.BundleItem;
import baji.lab1.entity.Product;
import baji.lab1.repository.BundleRepository;
import baji.lab1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/bundles")
public class BundleController {

    @Autowired
    private BundleRepository bundleRepository;

    @Autowired
    private ProductRepository productRepository;

    // ================= LIST =================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("bundles", bundleRepository.findAll());
        return "admin/bundles";
    }

    // ================= CREATE FORM =================
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("bundle", new BundleCreateDto());
        return "admin/bundle_create";
    }

    // ================= CREATE =================
    @PostMapping("/create")
    public String create(@ModelAttribute BundleCreateDto dto) {

        Bundle bundle = new Bundle();
        bundle.setName(dto.getName());
        bundle.setDescription(dto.getDescription());
        bundle.setImageUrl(dto.getImageUrl());
        bundle.setDiscountType(dto.getDiscountType());
        bundle.setDiscountValue(dto.getDiscountValue());

        if (dto.getProducts() != null) {
            for (var entry : dto.getProducts().entrySet()) {

                if (entry.getValue() == null || entry.getValue() <= 0) continue;

                Product product = productRepository.findById(entry.getKey()).orElse(null);
                if (product == null) continue;

                BundleItem item = new BundleItem();
                item.setBundle(bundle);
                item.setProduct(product);
                item.setQuantity(entry.getValue());

                bundle.getItems().add(item);
            }
        }

        bundleRepository.save(bundle);
        return "redirect:/admin/bundles";
    }

    // ================= VIEW =================
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {

        Bundle bundle = bundleRepository.findById(id).orElse(null);

        if (bundle == null) {
            return "redirect:/admin/bundles";
        }

        model.addAttribute("bundle", bundle);
        return "/bundle_details";
    }

    // ================= DELETE =================
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        bundleRepository.deleteById(id);
        return "redirect:/admin/bundles";
    }

    // ================= EDIT FORM =================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {

        Bundle bundle = bundleRepository.findById(id).orElse(null);

        if (bundle == null) {
            return "redirect:/admin/bundles";
        }

        BundleCreateDto dto = new BundleCreateDto();
        dto.setName(bundle.getName());
        dto.setDescription(bundle.getDescription());
        dto.setImageUrl(bundle.getImageUrl());
        dto.setDiscountType(bundle.getDiscountType());
        dto.setDiscountValue(bundle.getDiscountValue());

        // товары комплекта
        java.util.Map<Long, Integer> bundleProducts = new java.util.HashMap<>();

        for (BundleItem item : bundle.getItems()) {
            bundleProducts.put(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }

        model.addAttribute("bundle", dto);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("bundleProducts", bundleProducts);
        model.addAttribute("bundleId", id);

        return "admin/bundle_edit";
    }

    // ================= UPDATE =================
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute BundleCreateDto dto) {

        Bundle bundle = bundleRepository.findById(id).orElse(null);

        if (bundle == null) {
            return "redirect:/admin/bundles";
        }

        bundle.setName(dto.getName());
        bundle.setDescription(dto.getDescription());
        bundle.setImageUrl(dto.getImageUrl());
        bundle.setDiscountType(dto.getDiscountType());
        bundle.setDiscountValue(dto.getDiscountValue());

        bundle.getItems().clear();

        if (dto.getProducts() != null) {
            for (var entry : dto.getProducts().entrySet()) {

                if (entry.getValue() == null || entry.getValue() <= 0) continue;

                Product product = productRepository.findById(entry.getKey()).orElse(null);
                if (product == null) continue;

                BundleItem item = new BundleItem();
                item.setBundle(bundle);
                item.setProduct(product);
                item.setQuantity(entry.getValue());

                bundle.getItems().add(item);
            }
        }

        bundleRepository.save(bundle);

        return "redirect:/admin/bundles";
    }


    // ================= USER VIEW =================
    @GetMapping("/public/{id}")
    public String publicView(@PathVariable Long id, Model model) {

        Bundle bundle = bundleRepository.findById(id).orElse(null);

        if (bundle == null) {
            return "redirect:/user/products/catalog";
        }

        model.addAttribute("bundle", bundle);

        return "bundle_details";
    }
}