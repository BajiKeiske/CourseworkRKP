package baji.lab1.repository;

import java.util.List;

import baji.lab1.entity.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BundleRepository extends JpaRepository<Bundle, Long> {

    @Query("SELECT b FROM Bundle b JOIN b.items i WHERE i.product.id = :productId")
    List<Bundle> findByProductId(@Param("productId") Long productId);
}