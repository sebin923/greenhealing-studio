package com.greenhealing.studio.studio.controller;

import com.greenhealing.studio.auth.service.EmailVerificationService;
import com.greenhealing.studio.studio.service.StudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 공방(STUDIO_ADMIN) 회원가입 처리.
 *
 * 화면(GET)은 소비자 회원가입과 완전히 같은 페이지(auth/signup.html)를 재사용해.
 * 그 페이지 안에 "일반 회원가입 / 공방 회원가입" 탭이 있고, JS로 폼만 바뀌는 구조라서
 * 여기서도 return "auth/signup" 을 그대로 씀 (다른 html 파일이 아님!).
 * 대신 initialType="studio" 를 넘겨서, 페이지를 열자마자 공방 탭이 선택된 상태로 보이게 해줌.
 *
 * 제출(POST)은 소비자와 받는 값이 다르므로(공방명 등) 그대로 별도 주소로 유지함.
 */
@Controller
@RequiredArgsConstructor
public class StudioSignupController {

    private final StudioService studioService;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/studio-signup")
    public String signupPage(Model model) {
        model.addAttribute("initialType", "studio"); // 공방 탭을 기본으로 보여줌
        return "auth/signup";
    }

    @PostMapping("/studio-signup")
    public String signup(@RequestParam String name,
                         @RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(required = false) String phone,
                         @RequestParam String studioName,
                         @RequestParam(required = false) String studioDescription,
                         Model model) {
        if (!emailVerificationService.isVerified(email)) {
            model.addAttribute("error", "이메일 인증을 먼저 완료해 주세요.");
            model.addAttribute("initialType", "studio"); // 에러로 되돌아가도 공방 탭 유지
            return "auth/signup";
        }
        try {
            studioService.signupStudio(name, username, email, password, phone, studioName, studioDescription);
            return "redirect:/login?studioSignup=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("initialType", "studio");
            return "auth/signup";
        }
    }
}