package baji.lab1.repository;

import baji.lab1.entity.Category;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

     List<Category> findByNameContainingIgnoreCase(String name);
}