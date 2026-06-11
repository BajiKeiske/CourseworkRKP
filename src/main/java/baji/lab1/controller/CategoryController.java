package baji.lab1.controller;

import baji.lab1.entity.Category;
import baji.lab1.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("")
    public String mainPage(Model model) {
        model.addAttribute("items", categoryRepository.findAll());
        model.addAttribute("title", "Управление категориями");
        model.addAttribute("itemName", "категории");
        model.addAttribute("itemType", "category");  // ДОБАВИТЬ
        model.addAttribute("createUrl", "/admin/categories/create");
        model.addAttribute("updateUrl", "/admin/categories/update");
        model.addAttribute("deleteUrl", "/admin/categories/delete");
        return "admin/management_main";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("item", new Category());
        model.addAttribute("title", "Добавление категории");
        model.addAttribute("itemName", "категории");
        model.addAttribute("itemType", "category");  // ДОБАВИТЬ
        model.addAttribute("actionUrl", "/admin/categories/create");
        model.addAttribute("buttonText", "Добавить");
        model.addAttribute("backUrl", "/admin/categories");

        List<Category> allCategories = categoryRepository.findAll();
        model.addAttribute("allCategories", allCategories);

        return "admin/management_form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("item") Category category) {
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElse(null);
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/update/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            return "redirect:/admin/categories";
        }
        model.addAttribute("item", optionalCategory.get());
        model.addAttribute("title", "Редактирование категории");
        model.addAttribute("itemName", "категории");
        model.addAttribute("itemType", "category");  // ДОБАВИТЬ
        model.addAttribute("actionUrl", "/admin/categories/update");
        model.addAttribute("buttonText", "Сохранить");
        model.addAttribute("backUrl", "/admin/categories");

        List<Category> allCategories = categoryRepository.findAll();
        model.addAttribute("allCategories", allCategories);

        return "admin/management_form";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("item") Category category) {
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElse(null);
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
        }
        return "redirect:/admin/categories";
    }

    @ModelAttribute("rootCategories")
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }
}