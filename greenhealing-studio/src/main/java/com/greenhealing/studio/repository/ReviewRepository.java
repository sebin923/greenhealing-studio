package com.greenhealing.studio.repository;

import com.greenhealing.studio.domain.Product;
import com.greenhealing.studio.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
}
