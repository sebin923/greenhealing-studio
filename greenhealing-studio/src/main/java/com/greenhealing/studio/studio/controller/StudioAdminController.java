package com.greenhealing.studio.studio.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.studio.service.StudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 공방 관리자(STUDIO_ADMIN) 전용 화면의 진입점 + 공방 정보 수정.
 * SecurityConfig에 "/studio-admin/**" 는 STUDIO_ADMIN 권한만 접근 가능하도록
 * 이미 막아뒀기 때문에, 다른 권한(CUSTOMER, SUPER_ADMIN)으로 로그인한 사람이나
 * 비로그인 사용자가 이 주소로 들어오면 자동으로 403(권한없음) 또는 로그인 화면으로 이동함.
 */
@Controller
@RequiredArgsConstructor
public class StudioAdminController {

    private final StudioService studioService;
    private final UserRepository userRepository;

    @GetMapping("/studio-admin")
    public String home(Authentication authentication, Model model) {
        model.addAttribute("studio", studioService.getMyStudio(currentUser(authentication)));
        return "studio/admin-home";
    }

    @GetMapping("/studio-admin/info")
    public String infoForm(Authentication authentication, Model model) {
        model.addAttribute("studio", studioService.getMyStudio(currentUser(authentication)));
        return "studio/info-edit";
    }

    @PostMapping("/studio-admin/info")
    public String updateInfo(Authentication authentication,
                             @RequestParam String name,
                             @RequestParam(required = false) String description) {
        studioService.updateMyStudio(currentUser(authentication), name, description);
        return "redirect:/studio-admin/info?updated=success";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}