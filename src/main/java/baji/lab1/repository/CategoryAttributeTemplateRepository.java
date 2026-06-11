package baji.lab1.repository;

import baji.lab1.entity.Category;
import baji.lab1.entity.CategoryAttributeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryAttributeTemplateRepository extends JpaRepository<CategoryAttributeTemplate, Long> {
    List<CategoryAttributeTemplate> findByCategory(Category category);
    List<CategoryAttributeTemplate> findByCategoryId(Long categoryId);
    void deleteByCategory(Category category);
}