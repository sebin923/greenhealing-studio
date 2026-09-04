package com.greenhealing.studio.product.repository;

import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.studio.domain.Studio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContaining(String keyword);
    List<Product> findByCategoryAndNameContaining(String category, String keyword);
    List<Product> findByStudio(Studio studio);

    // ---------- 목록 화면 페이징용 (12개씩) ----------
    // Page<Product>를 반환하면 Spring Data JPA가 "전체 개수, 총 페이지 수" 같은 걸 알아서 계산해줌
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByNameContaining(String keyword, Pageable pageable);
    Page<Product> findByCategoryAndNameContaining(String category, String keyword, Pageable pageable);

    // ---------- 왼쪽 카테고리 목록에 "(12)" 같은 개수 표시용 ----------
    long countByCategory(String category);
}