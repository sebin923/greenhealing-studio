package com.greenhealing.studio.lesson.repository;

import com.greenhealing.studio.lesson.domain.ClassSchedule;
import com.greenhealing.studio.studio.domain.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByClassDateBetween(LocalDate start, LocalDate end);
    List<ClassSchedule> findByStudio(Studio studio);
}