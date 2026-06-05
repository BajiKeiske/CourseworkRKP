package baji.lab1.service;

import baji.lab1.entity.Category;
import baji.lab1.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getCategoryTree() {
        return categoryRepository.findByParentIsNull();
    }
}