package com.greenhealing.studio.repository;

import com.greenhealing.studio.domain.Order;
import com.greenhealing.studio.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
