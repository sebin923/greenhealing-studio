package com.greenhealing.studio.lesson.repository;

import com.greenhealing.studio.lesson.domain.ClassSchedule;
import com.greenhealing.studio.lesson.domain.Reservation;
import com.greenhealing.studio.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);

    // 특정 사용자가 특정 클래스를 이미 예약(취소 안 한 상태로)했는지 확인할 때 씀
    Optional<Reservation> findByUserAndClassScheduleAndStatus(User user, ClassSchedule classSchedule, Reservation.Status status);
}