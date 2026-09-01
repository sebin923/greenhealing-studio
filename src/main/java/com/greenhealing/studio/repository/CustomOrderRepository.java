package com.greenhealing.studio.repository;

import com.greenhealing.studio.domain.CustomOrder;
import com.greenhealing.studio.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {
    List<CustomOrder> findByUser(User user);
    List<CustomOrder> findByStatus(CustomOrder.Status status);
}
