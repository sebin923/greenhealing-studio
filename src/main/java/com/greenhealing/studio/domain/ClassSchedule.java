package com.greenhealing.studio.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "class_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 예: 터프팅 원데이클래스

    @Column(nullable = false)
    private LocalDate classDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int capacity; // 정원

    @Column(nullable = false)
    private int reservedCount; // 현재 예약 인원

    @Column(nullable = false)
    private int price;

    @Builder
    public ClassSchedule(String title, LocalDate classDate, LocalTime startTime, LocalTime endTime,
                          int capacity, int price) {
        this.title = title;
        this.classDate = classDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.reservedCount = 0;
        this.price = price;
    }

    public boolean isFull() {
        return this.reservedCount >= this.capacity;
    }

    public void reserve() {
        if (isFull()) {
            throw new IllegalStateException("정원이 마감된 클래스입니다.");
        }
        this.reservedCount++;
    }

    public void cancelReservation() {
        if (this.reservedCount > 0) {
            this.reservedCount--;
        }
    }
}
