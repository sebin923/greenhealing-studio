package com.greenhealing.studio.product.repository;

import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.studio.domain.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContaining(String keyword);
    List<Product> findByCategoryAndNameContaining(String category, String keyword);
    List<Product> findByStudio(Studio studio);
}