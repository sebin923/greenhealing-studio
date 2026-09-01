package com.greenhealing.studio.controller;

import com.greenhealing.studio.repository.UserRepository;
import com.greenhealing.studio.service.EmailVerificationService;
import com.greenhealing.studio.service.PasswordResetService;
import com.greenhealing.studio.service.UserService;
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
        return "login"; // templates/login.html
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // templates/signup.html
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(required = false) String phone,
                         Model model) {
        if (!emailVerificationService.isVerified(email)) {
            model.addAttribute("error", "이메일 인증을 먼저 완료해 주세요.");
            return "signup";
        }
        try {
            userService.signup(name, email, password, phone);
            return "redirect:/login?signup=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
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
        return "password-reset-request";
    }

    @PostMapping("/password-reset")
    public String passwordResetRequest(@RequestParam String email, Model model) {
        passwordResetService.requestReset(email);
        // 가입 여부와 무관하게 동일한 안내 (계정 존재 여부 노출 방지)
        model.addAttribute("message", "입력하신 이메일로 재설정 링크를 보냈습니다. (가입된 이메일인 경우에만 발송됩니다)");
        return "password-reset-request";
    }

    @GetMapping("/password-reset/confirm")
    public String passwordResetConfirmPage(@RequestParam String token, Model model) {
        boolean valid = passwordResetService.isValidToken(token);
        model.addAttribute("valid", valid);
        model.addAttribute("token", token);
        return "password-reset-confirm";
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
            return "password-reset-confirm";
        }
    }
}