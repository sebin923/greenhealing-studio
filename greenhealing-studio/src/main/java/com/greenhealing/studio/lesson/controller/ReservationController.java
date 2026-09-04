package com.greenhealing.studio.lesson.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.lesson.domain.Reservation;
import com.greenhealing.studio.lesson.service.ReservationService;
import com.greenhealing.studio.lesson.util.CalendarUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 예약 생성/취소/내역 조회를 담당하는 컨트롤러. */
@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    /** 클래스 예약하기 (POST /reservations) */
    @PostMapping("/reservations")
    public String reserve(Authentication authentication, @RequestParam Long classScheduleId) {
        try {
            reservationService.reserve(currentUser(authentication), classScheduleId);
            return "redirect:/classes/" + classScheduleId + "?reserved=success";
        } catch (IllegalStateException e) {
            return "redirect:/classes/" + classScheduleId + "?error=" + e.getMessage();
        }
    }

    /**
     * 내 예약 내역 (GET /reservations).
     * 왼쪽엔 "예약이 있는 날짜만 표시되는" 이번 달 달력, 오른쪽엔 예약 상세 목록을 같이 보여줌.
     */
    @GetMapping("/reservations")
    public String myReservations(Authentication authentication, Model model) {
        List<Reservation> reservations = reservationService.getMyReservations(currentUser(authentication));
        model.addAttribute("reservations", reservations);

        // 취소되지 않은(CONFIRMED) 예약만 달력에 점으로 표시함
        Map<LocalDate, Integer> countsByDate = reservations.stream()
                .filter(r -> r.getStatus() == Reservation.Status.CONFIRMED)
                .collect(Collectors.groupingBy(r -> r.getClassSchedule().getClassDate(), Collectors.summingInt(r -> 1)));

        LocalDate thisMonth = LocalDate.now();
        model.addAttribute("calendarWeeks", CalendarUtil.buildMonthCalendar(thisMonth, countsByDate));
        model.addAttribute("calendarMonthLabel", thisMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")));

        return "lesson/my-reservations";
    }

    /** 예약 취소 (POST /reservations/{id}/cancel) */
    @PostMapping("/reservations/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication) {
        reservationService.cancel(currentUser(authentication), id);
        return "redirect:/reservations";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}