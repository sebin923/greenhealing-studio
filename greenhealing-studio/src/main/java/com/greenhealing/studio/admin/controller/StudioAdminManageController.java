package com.greenhealing.studio.admin.controller;

import com.greenhealing.studio.admin.service.StudioAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 플랫폼 관리자 전용 "공방 관리" 화면.
 * 이 컨트롤러의 모든 주소(/platform-admin/studios/**)는
 * SecurityConfig에서 이미 SUPER_ADMIN 권한만 접근 가능하도록 막아뒀음.
 */
@Controller
@RequiredArgsConstructor
public class StudioAdminManageController {

    private final StudioAdminService studioAdminService;

    @GetMapping("/platform-admin/studios")
    public String list(Model model) {
        model.addAttribute("pendingStudios", studioAdminService.getPendingStudios());
        model.addAttribute("approvedStudios", studioAdminService.getApprovedStudios());
        return "admin/studio/list";
    }

    @PostMapping("/platform-admin/studios/{id}/approve")
    public String approve(@PathVariable Long id) {
        studioAdminService.approve(id);
        return "redirect:/platform-admin/studios";
    }

    @PostMapping("/platform-admin/studios/{id}/reject")
    public String reject(@PathVariable Long id) {
        studioAdminService.reject(id);
        return "redirect:/platform-admin/studios";
    }
}