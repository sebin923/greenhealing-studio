package com.greenhealing.studio.lesson.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.lesson.domain.ClassSchedule;
import com.greenhealing.studio.lesson.service.ClassAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 공방 관리자 전용 "내 클래스 관리" 화면.
 * /studio-admin/classes/** 는 SecurityConfig에서 이미 STUDIO_ADMIN 권한만
 * 접근 가능하도록 막아뒀음.
 */
@Controller
@RequestMapping("/studio-admin/classes")
@RequiredArgsConstructor
public class ClassAdminController {

    private final ClassAdminService classAdminService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Authentication authentication, Model model) {
        User me = currentUser(authentication);
        model.addAttribute("classes", classAdminService.getMyClasses(me));
        model.addAttribute("studio", classAdminService.getMyStudio(me));
        return "studio/lesson/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("formAction", "/studio-admin/classes");
        model.addAttribute("submitLabel", "등록하기");
        return "studio/lesson/form";
    }

    @PostMapping
    public String create(Authentication authentication,
                         @RequestParam String title,
                         @RequestParam LocalDate classDate,
                         @RequestParam LocalTime startTime,
                         @RequestParam LocalTime endTime,
                         @RequestParam int capacity,
                         @RequestParam int price) {
        classAdminService.createClass(currentUser(authentication), title, classDate, startTime, endTime, capacity, price);
        return "redirect:/studio-admin/classes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        ClassSchedule classSchedule = classAdminService.getMyClass(currentUser(authentication), id);
        model.addAttribute("classItem", classSchedule);
        model.addAttribute("formAction", "/studio-admin/classes/" + id);
        model.addAttribute("submitLabel", "수정하기");
        return "studio/lesson/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         Authentication authentication,
                         @RequestParam String title,
                         @RequestParam LocalDate classDate,
                         @RequestParam LocalTime startTime,
                         @RequestParam LocalTime endTime,
                         @RequestParam int capacity,
                         @RequestParam int price,
                         Model model) {
        try {
            classAdminService.updateClass(currentUser(authentication), id, title, classDate, startTime, endTime, capacity, price);
            return "redirect:/studio-admin/classes";
        } catch (IllegalArgumentException e) {
            // 정원을 예약인원보다 적게 넣으려 한 경우 등, 에러 메시지와 함께 폼으로 되돌아감
            ClassSchedule classSchedule = classAdminService.getMyClass(currentUser(authentication), id);
            model.addAttribute("classItem", classSchedule);
            model.addAttribute("formAction", "/studio-admin/classes/" + id);
            model.addAttribute("submitLabel", "수정하기");
            model.addAttribute("error", e.getMessage());
            return "studio/lesson/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        classAdminService.deleteClass(currentUser(authentication), id);
        return "redirect:/studio-admin/classes";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}