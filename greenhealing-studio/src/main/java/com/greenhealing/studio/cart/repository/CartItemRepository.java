package com.greenhealing.studio.cart.repository;

import com.greenhealing.studio.cart.domain.CartItem;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);

    // 상품을 삭제하기 전에, 그 상품이 담긴 다른 사람들의 장바구니 항목부터 먼저 지우기 위함
    void deleteByProduct(Product product);
}