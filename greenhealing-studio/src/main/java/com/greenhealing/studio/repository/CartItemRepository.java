package com.greenhealing.studio.repository;

import com.greenhealing.studio.domain.CartItem;
import com.greenhealing.studio.domain.Product;
import com.greenhealing.studio.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
}
