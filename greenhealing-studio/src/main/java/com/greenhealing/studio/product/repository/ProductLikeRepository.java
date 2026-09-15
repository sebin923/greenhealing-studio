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

    // 상품을 삭제하기 전에, 그 상품을 찜해둔 기록부터 먼저 지우기 위함
    void deleteByProduct(Product product);
}