package com.greenhealing.studio.order.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.order.service.OrderAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 공방 관리자 전용 "주문 관리" 화면.
 * /studio-admin/orders/** 는 SecurityConfig에서 이미 STUDIO_ADMIN 권한만 접근 가능하도록 막아뒀음.
 */
@Controller
@RequestMapping("/studio-admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminService orderAdminService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Authentication authentication, Model model) {
        model.addAttribute("orders", orderAdminService.getMyStudioOrders(currentUser(authentication)));
        return "studio/order/list";
    }

    @PostMapping("/{id}/ship")
    public String startShipping(@PathVariable Long id, Authentication authentication) {
        orderAdminService.startShipping(currentUser(authentication), id);
        return "redirect:/studio-admin/orders";
    }

    @PostMapping("/{id}/deliver")
    public String markDelivered(@PathVariable Long id, Authentication authentication) {
        orderAdminService.markDelivered(currentUser(authentication), id);
        return "redirect:/studio-admin/orders";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}