package com.greenhealing.studio.auth.controller;

import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.auth.service.EmailVerificationService;
import com.greenhealing.studio.auth.service.PasswordResetService;
import com.greenhealing.studio.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // templates/auth/login.html
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("initialType", "customer"); // 페이지 처음 열릴 때 "일반 회원가입" 탭을 기본으로 보여줌
        return "auth/signup"; // templates/auth/signup.html
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                         @RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(required = false) String phone,
                         Model model) {
        if (!emailVerificationService.isVerified(email)) {
            model.addAttribute("error", "이메일 인증을 먼저 완료해 주세요.");
            model.addAttribute("initialType", "customer"); // 에러로 되돌아갔을 때도 같은 탭이 보이게
            return "auth/signup";
        }
        try {
            userService.signup(name, username, email, password, phone);
            return "redirect:/login?signup=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("initialType", "customer");
            return "auth/signup";
        }
    }

    // ---------- 아이디 중복확인 (AJAX) ----------
    @GetMapping("/api/auth/check-username")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        boolean available = !userRepository.existsByUsername(username);
        return Map.of("available", available);
    }

    // ---------- 이메일 중복확인 (AJAX) ----------
    @GetMapping("/api/auth/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@RequestParam String email) {
        boolean available = !userRepository.existsByEmail(email);
        return Map.of("available", available);
    }

    // ---------- 이메일 인증 (AJAX) ----------
    @PostMapping("/api/auth/send-verification")
    @ResponseBody
    public Map<String, Object> sendVerification(@RequestParam String email) {
        if (userRepository.existsByEmail(email)) {
            return Map.of("success", false, "message", "이미 가입된 이메일입니다.");
        }
        emailVerificationService.sendVerificationCode(email);
        return Map.of("success", true, "message", "인증번호를 발송했습니다.");
    }

    @PostMapping("/api/auth/verify-code")
    @ResponseBody
    public Map<String, Object> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = emailVerificationService.verifyCode(email, code);
        if (verified) {
            return Map.of("success", true, "message", "이메일 인증이 완료되었습니다.");
        }
        return Map.of("success", false, "message", "인증번호가 올바르지 않거나 만료되었습니다.");
    }

    // ---------- 비밀번호 찾기 ----------
    @GetMapping("/password-reset")
    public String passwordResetRequestPage() {
        return "auth/password-reset-request";
    }

    @PostMapping("/password-reset")
    public String passwordResetRequest(@RequestParam String email, Model model) {
        passwordResetService.requestReset(email);
        // 가입 여부와 무관하게 동일한 안내 (계정 존재 여부 노출 방지)
        model.addAttribute("message", "입력하신 이메일로 재설정 링크를 보냈습니다. (가입된 이메일인 경우에만 발송됩니다)");
        return "auth/password-reset-request";
    }

    @GetMapping("/password-reset/confirm")
    public String passwordResetConfirmPage(@RequestParam String token, Model model) {
        boolean valid = passwordResetService.isValidToken(token);
        model.addAttribute("valid", valid);
        model.addAttribute("token", token);
        return "auth/password-reset-confirm";
    }

    @PostMapping("/password-reset/confirm")
    public String passwordResetConfirm(@RequestParam String token,
                                       @RequestParam String newPassword,
                                       Model model) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            return "redirect:/login?reset=success";
        } catch (RuntimeException e) {
            model.addAttribute("valid", false);
            model.addAttribute("error", e.getMessage());
            return "auth/password-reset-confirm";
        }
    }
}