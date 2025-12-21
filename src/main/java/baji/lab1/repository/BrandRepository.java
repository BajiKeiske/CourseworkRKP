package baji.lab1.repository;

import baji.lab1.entity.Brand;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByNameContainingIgnoreCase(String name);

}