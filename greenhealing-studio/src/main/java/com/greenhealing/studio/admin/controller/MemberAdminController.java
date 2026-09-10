package com.greenhealing.studio.admin.controller;

import com.greenhealing.studio.admin.service.MemberAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 플랫폼 관리자 전용 "회원 관리" 화면.
 * /platform-admin/members/** 는 SecurityConfig에서 이미 SUPER_ADMIN 권한만 접근 가능.
 */
@Controller
@RequestMapping("/platform-admin/members")
@RequiredArgsConstructor
public class MemberAdminController {

    private final MemberAdminService memberAdminService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("members", memberAdminService.getMembers(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/member/list";
    }

    @PostMapping("/{id}/suspend")
    public String suspend(@PathVariable Long id) {
        memberAdminService.suspend(id);
        return "redirect:/platform-admin/members";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id) {
        memberAdminService.activate(id);
        return "redirect:/platform-admin/members";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        memberAdminService.delete(id);
        return "redirect:/platform-admin/members";
    }
}