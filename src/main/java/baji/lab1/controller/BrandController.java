package baji.lab1.controller;

import baji.lab1.entity.Brand;
import baji.lab1.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/admin/brands")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    // Главная страница брендов
    @GetMapping("")
    public String mainPage(Model model) {
        model.addAttribute("items", brandRepository.findAll());
        model.addAttribute("title", "Управление брендами");
        model.addAttribute("itemName", "бренд");
        model.addAttribute("createUrl", "/admin/brands/create");
        model.addAttribute("updateUrl", "/admin/brands/update");
        model.addAttribute("deleteUrl", "/admin/brands/delete");
        return "admin/management_main";
    }

    // Форма создания бренда
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("item", new Brand());
        model.addAttribute("title", "Добавление бренда");
        model.addAttribute("itemName", "бренда");
        model.addAttribute("actionUrl", "/admin/brands/create");
        model.addAttribute("buttonText", "Добавить");
        model.addAttribute("backUrl", "/admin/brands");
        return "admin/management_form";
    }

    // Сохранить бренд
    @PostMapping("/create")
    public String create(@ModelAttribute("item") Brand brand) {
        brandRepository.save(brand);
        return "redirect:/admin/brands";
    }

    // Форма редактирования бренда
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Brand> optionalBrand = brandRepository.findById(id);
        if (optionalBrand.isEmpty()) {
            return "redirect:/admin/brands";
        }
        model.addAttribute("item", optionalBrand.get());
        model.addAttribute("title", "Редактирование бренда");
        model.addAttribute("itemName", "бренда");
        model.addAttribute("actionUrl", "/admin/brands/update");
        model.addAttribute("buttonText", "Сохранить");
        model.addAttribute("backUrl", "/admin/brands");
        return "admin/management_form";
    }

    // Обновить бренд
    @PostMapping("/update")
    public String update(@ModelAttribute("item") Brand brand) {
        brandRepository.save(brand);
        return "redirect:/admin/brands";
    }

    // удалить бренд
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (brandRepository.existsById(id)) {
            brandRepository.deleteById(id);
        }
        return "redirect:/admin/brands";
    }
}