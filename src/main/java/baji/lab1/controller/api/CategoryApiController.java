package baji.lab1.controller.api;

import baji.lab1.entity.Category;
import baji.lab1.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryApiController {

    @Autowired
    private CategoryRepository categoryRepository;

    // GET все категории
    @GetMapping
    public List<Category> getAllCategories() {
        return (List<Category>) categoryRepository.findAll();
    }

    // GET категорию по ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST создать категорию
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    // PUT обновить категорию
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Category category = optionalCategory.get();
        category.setName(categoryDetails.getName()); // добавь другие поля, если есть

        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    // DELETE удалить категорию
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}