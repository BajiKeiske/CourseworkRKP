package baji.lab1.controller;

import baji.lab1.entity.Brand;
import baji.lab1.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    // Главная страница брендов
    @GetMapping("/main")
    public String mainPage(Model model) {
        model.addAttribute("items", brandRepository.findAll());
        model.addAttribute("title", "Управление брендами");
        model.addAttribute("itemName", "бренда");
        model.addAttribute("createUrl", "/brands/create");
        model.addAttribute("updateUrl", "/brands/update");
        model.addAttribute("deleteUrl", "/brands/delete");
        return "management_main";
    }

    // Форма создания бренда
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("item", new Brand());
        model.addAttribute("title", "Добавление бренда");
        model.addAttribute("itemName", "бренда");
        model.addAttribute("actionUrl", "/brands/create");
        model.addAttribute("buttonText", "Добавить");
        model.addAttribute("backUrl", "/brands/main");
        return "management_form";
    }

    // Сохранить бренд
    @PostMapping("/create")
    public String create(@ModelAttribute("item") Brand brand) {
        brandRepository.save(brand);
        return "redirect:/brands/main";
    }

    // Форма редактирования бренда
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Brand> optionalBrand = brandRepository.findById(id);
        if (optionalBrand.isEmpty()) {
            return "redirect:/brands/main";
        }
        model.addAttribute("item", optionalBrand.get());
        model.addAttribute("title", "Редактирование бренда");
        model.addAttribute("itemName", "бренда");
        model.addAttribute("actionUrl", "/brands/update");
        model.addAttribute("buttonText", "Сохранить");
        model.addAttribute("backUrl", "/brands/main");
        return "management_form";
    }

    // Обновить бренд
    @PostMapping("/update")
    public String update(@ModelAttribute("item") Brand brand) {
        brandRepository.save(brand);
        return "redirect:/brands/main";
    }

    // Удалить бренд
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (brandRepository.existsById(id)) {
            brandRepository.deleteById(id);
        }
        return "redirect:/brands/main";
    }
}