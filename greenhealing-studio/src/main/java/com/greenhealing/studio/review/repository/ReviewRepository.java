package com.greenhealing.studio.review.repository;

import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
}
