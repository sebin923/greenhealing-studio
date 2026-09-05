package com.greenhealing.studio.customorder.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.customorder.service.CustomOrderAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 공방 관리자 전용 "주문제작 요청 관리" 화면.
 * /studio-admin/custom-orders/** 는 SecurityConfig에서 이미 STUDIO_ADMIN 권한만
 * 접근 가능하도록 막아뒀음.
 */
@Controller
@RequestMapping("/studio-admin/custom-orders")
@RequiredArgsConstructor
public class CustomOrderAdminController {

    private final CustomOrderAdminService customOrderAdminService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Authentication authentication, Model model) {
        model.addAttribute("requests", customOrderAdminService.getMyStudioRequests(currentUser(authentication)));
        return "studio/customorder/list";
    }

    @PostMapping("/{id}/quote")
    public String giveQuote(@PathVariable Long id, Authentication authentication, @RequestParam int price) {
        customOrderAdminService.giveQuote(currentUser(authentication), id, price);
        return "redirect:/studio-admin/custom-orders";
    }

    @PostMapping("/{id}/start")
    public String startProduction(@PathVariable Long id, Authentication authentication) {
        customOrderAdminService.startProduction(currentUser(authentication), id);
        return "redirect:/studio-admin/custom-orders";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, Authentication authentication) {
        customOrderAdminService.complete(currentUser(authentication), id);
        return "redirect:/studio-admin/custom-orders";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, Authentication authentication) {
        customOrderAdminService.reject(currentUser(authentication), id);
        return "redirect:/studio-admin/custom-orders";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}