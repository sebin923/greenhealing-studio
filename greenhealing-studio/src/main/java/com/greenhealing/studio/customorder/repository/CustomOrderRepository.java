package com.greenhealing.studio.customorder.repository;

import com.greenhealing.studio.customorder.domain.CustomOrder;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {
    List<CustomOrder> findByUser(User user);
    List<CustomOrder> findByStatus(CustomOrder.Status status);
    List<CustomOrder> findByStudio(Studio studio);
}