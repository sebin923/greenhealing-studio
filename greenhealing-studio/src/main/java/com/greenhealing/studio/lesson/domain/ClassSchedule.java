package com.greenhealing.studio.lesson.domain;

import com.greenhealing.studio.common.config.BaseTimeEntity;
import com.greenhealing.studio.studio.domain.Studio;
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

    // 이 클래스를 운영하는 공방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

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
    public ClassSchedule(Studio studio, String title, LocalDate classDate, LocalTime startTime, LocalTime endTime,
                         int capacity, int price) {
        this.studio = studio;
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

    /**
     * 공방 관리자가 클래스 정보를 수정할 때 씀.
     * capacity(정원)를 이미 예약된 인원(reservedCount)보다 적게는 못 줄이게 막아둠
     * (이미 5명이 예약했는데 정원을 3명으로 줄이면 앞뒤가 안 맞으니까).
     */
    public void update(String title, LocalDate classDate, LocalTime startTime, LocalTime endTime,
                       int capacity, int price) {
        if (capacity < this.reservedCount) {
            throw new IllegalArgumentException("정원은 현재 예약 인원(" + this.reservedCount + "명)보다 적게 설정할 수 없습니다.");
        }
        this.title = title;
        this.classDate = classDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.price = price;
    }

    /** 이 클래스가 정말 이 공방(studio) 소유가 맞는지 확인할 때 씀 */
    public boolean belongsTo(Studio studio) {
        return this.studio.getId().equals(studio.getId());
    }
}