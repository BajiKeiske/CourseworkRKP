package baji.lab1.repository;

import baji.lab1.entity.BundleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BundleItemRepository extends JpaRepository<BundleItem, Long> {
}