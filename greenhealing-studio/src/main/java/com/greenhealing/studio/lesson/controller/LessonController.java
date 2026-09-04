package com.greenhealing.studio.lesson.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.lesson.domain.ClassSchedule;
import com.greenhealing.studio.lesson.domain.Reservation;
import com.greenhealing.studio.lesson.service.ReservationService;
import com.greenhealing.studio.lesson.util.CalendarUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 클래스 목록/상세 "조회" 화면 담당.
 * 실제 예약/취소 "처리"는 ReservationController가 따로 담당함.
 */
@Controller
@RequiredArgsConstructor
public class LessonController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    /**
     * 클래스 목록 (누구나 조회 가능).
     * 화면 위쪽엔 이번 달 달력을, 아래쪽엔 기존처럼 카드 그리드를 같이 보여줌.
     * 달력은 "날짜별로 클래스가 몇 개 있는지"만 보여주는 용도고,
     * 실제 날짜 클릭 필터링은 화면(JS)에서 처리함.
     */
    @GetMapping("/classes")
    public String list(Model model) {
        List<ClassSchedule> classes = reservationService.getUpcomingClasses();
        model.addAttribute("classes", classes);

        // 날짜별로 클래스가 몇 개인지 세어서, 그 결과를 달력 그리는 유틸에 넘겨줌
        Map<LocalDate, Integer> countsByDate = classes.stream()
                .collect(Collectors.groupingBy(ClassSchedule::getClassDate, Collectors.summingInt(c -> 1)));

        LocalDate thisMonth = LocalDate.now();
        model.addAttribute("calendarWeeks", CalendarUtil.buildMonthCalendar(thisMonth, countsByDate));
        model.addAttribute("calendarMonthLabel", thisMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")));

        return "lesson/list";
    }

    /** 클래스 상세 (누구나 조회 가능, 로그인한 사람이면 "이미 예약했는지"도 같이 보여줌) */
    @GetMapping("/classes/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        ClassSchedule classSchedule = reservationService.getClass(id);
        model.addAttribute("classItem", classSchedule);

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                Reservation myReservation = reservationService.findMyReservation(user, classSchedule);
                model.addAttribute("myReservation", myReservation);
            }
        }
        return "lesson/detail";
    }
}