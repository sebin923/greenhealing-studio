package com.greenhealing.studio.lesson.domain;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_schedule_id", nullable = false)
    private ClassSchedule classSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Builder
    public Reservation(User user, ClassSchedule classSchedule) {
        this.user = user;
        this.classSchedule = classSchedule;
        this.status = Status.CONFIRMED;
    }

    public void cancel() {
        this.status = Status.CANCELED;
    }

    public enum Status {
        CONFIRMED, CANCELED
    }
}
