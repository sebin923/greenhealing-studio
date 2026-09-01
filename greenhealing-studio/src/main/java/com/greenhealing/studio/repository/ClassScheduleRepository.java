package com.greenhealing.studio.repository;

import com.greenhealing.studio.domain.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByClassDateBetween(LocalDate start, LocalDate end);
}
