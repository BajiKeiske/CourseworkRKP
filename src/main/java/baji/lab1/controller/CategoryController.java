package baji.lab1.controller;

import baji.lab1.entity.Category;
import baji.lab1.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // Главная страница категорий
    @GetMapping("/main")
    public String mainPage(Model model) {
        model.addAttribute("items", categoryRepository.findAll());
        model.addAttribute("title", "Управление категориями");
        model.addAttribute("itemName", "категории");
        model.addAttribute("createUrl", "/categories/create");
        model.addAttribute("updateUrl", "/categories/update");
        model.addAttribute("deleteUrl", "/categories/delete");
        return "management_main";
    }

    // Форма создания категории
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("item", new Category());
        model.addAttribute("title", "Добавление категории");
        model.addAttribute("itemName", "категории");
        model.addAttribute("actionUrl", "/categories/create");
        model.addAttribute("buttonText", "Добавить");
        model.addAttribute("backUrl", "/categories/main");
        return "management_form";
    }

    // Сохранить категорию
    @PostMapping("/create")
    public String create(@ModelAttribute("item") Category category) {
        categoryRepository.save(category);
        return "redirect:/categories/main";
    }

    // Форма редактирования категории
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            return "redirect:/categories/main";
        }
        model.addAttribute("item", optionalCategory.get());
        model.addAttribute("title", "Редактирование категории");
        model.addAttribute("itemName", "категории");
        model.addAttribute("actionUrl", "/categories/update");
        model.addAttribute("buttonText", "Сохранить");
        model.addAttribute("backUrl", "/categories/main");
        return "management_form";
    }

    // Обновить категорию
    @PostMapping("/update")
    public String update(@ModelAttribute("item") Category category) {
        categoryRepository.save(category);
        return "redirect:/categories/main";
    }

    // Удалить категорию
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
        }
        return "redirect:/categories/main";
    }
}