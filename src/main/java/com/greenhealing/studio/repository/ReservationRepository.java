package com.greenhealing.studio.repository;

import com.greenhealing.studio.domain.Reservation;
import com.greenhealing.studio.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
}
