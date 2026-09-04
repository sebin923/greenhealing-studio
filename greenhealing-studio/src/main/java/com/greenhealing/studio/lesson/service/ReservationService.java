package com.greenhealing.studio.lesson.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.lesson.domain.ClassSchedule;
import com.greenhealing.studio.lesson.domain.Reservation;
import com.greenhealing.studio.lesson.repository.ClassScheduleRepository;
import com.greenhealing.studio.lesson.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 클래스 예약/취소의 실제 로직을 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ClassScheduleRepository classScheduleRepository;
    private final ReservationRepository reservationRepository;

    /** 오늘부터 2개월 이내의 클래스 일정을 전체 공방 통틀어 조회 (소비자용 목록) */
    @Transactional(readOnly = true)
    public List<ClassSchedule> getUpcomingClasses() {
        return classScheduleRepository.findByClassDateBetween(LocalDate.now(), LocalDate.now().plusMonths(2));
    }

    @Transactional(readOnly = true)
    public ClassSchedule getClass(Long classScheduleId) {
        return classScheduleRepository.findById(classScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클래스입니다."));
    }

    /** 이 사용자가 이 클래스를 이미 예약(취소 안 한 상태로) 했는지 조회. 없으면 null 반환 */
    @Transactional(readOnly = true)
    public Reservation findMyReservation(User user, ClassSchedule classSchedule) {
        return reservationRepository
                .findByUserAndClassScheduleAndStatus(user, classSchedule, Reservation.Status.CONFIRMED)
                .orElse(null);
    }

    /**
     * 클래스 예약.
     * ClassSchedule.reserve() 안에서 정원이 다 찼으면 예외를 던지도록 이미 구현되어 있어서,
     * 여기서는 "중복 예약 방지"만 추가로 검사해주면 됨.
     */
    @Transactional
    public Reservation reserve(User user, Long classScheduleId) {
        ClassSchedule classSchedule = getClass(classScheduleId);

        if (findMyReservation(user, classSchedule) != null) {
            throw new IllegalStateException("이미 예약한 클래스입니다.");
        }

        classSchedule.reserve(); // 정원 초과 시 여기서 IllegalStateException 발생 -> 트랜잭션 롤백
        return reservationRepository.save(Reservation.builder()
                .user(user)
                .classSchedule(classSchedule)
                .build());
    }

    /** 예약 취소. 취소하면 클래스의 reservedCount도 함께 1 줄어들어서 다른 사람이 예약할 수 있게 됨 */
    @Transactional
    public void cancel(User user, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 예약만 취소할 수 있습니다.");
        }
        if (reservation.getStatus() == Reservation.Status.CANCELED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        reservation.cancel();
        reservation.getClassSchedule().cancelReservation();
    }

    /** 내 예약 내역 전체 조회 (취소된 것도 포함) */
    @Transactional(readOnly = true)
    public List<Reservation> getMyReservations(User user) {
        return reservationRepository.findByUser(user);
    }
}