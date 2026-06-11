package baji.lab1.service;

import baji.lab1.entity.Category;
import baji.lab1.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Сервис для работы с категориями товаров.
 * Предоставляет методы для получения категорий в иерархическом виде и списком.
 *
 * @author Student
 * @version 1.0
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Возвращает дерево категорий (только корневые категории).
     * Корневые категории — это категории, у которых нет родителя.
     * Используется для отображения иерархической структуры навигации.
     *
     * @return список корневых категорий
     */
    public List<Category> getCategoryTree() {
        return categoryRepository.findByParentIsNull();
    }

    /**
     * Возвращает все категории плоским списком без иерархии.
     * Используется для выпадающих списков и фильтрации.
     *
     * @return список всех категорий
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}