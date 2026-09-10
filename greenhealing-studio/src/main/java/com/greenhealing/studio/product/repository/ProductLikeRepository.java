package com.greenhealing.studio.product.repository;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.domain.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
    List<ProductLike> findByUser(User user);
    Optional<ProductLike> findByUserAndProduct(User user, Product product);
}