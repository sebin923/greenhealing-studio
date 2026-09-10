package com.greenhealing.studio.admin.controller;

import com.greenhealing.studio.admin.service.NoticeAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 플랫폼 관리자 전용 "공지사항 관리" 화면.
 * /platform-admin/notices/** 는 SecurityConfig에서 이미 SUPER_ADMIN 권한만 접근 가능.
 */
@Controller
@RequestMapping("/platform-admin/notices")
@RequiredArgsConstructor
public class NoticeAdminController {

    private final NoticeAdminService noticeAdminService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notices", noticeAdminService.getAll());
        return "admin/notice/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("formAction", "/platform-admin/notices");
        model.addAttribute("submitLabel", "등록하기");
        return "admin/notice/form";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam String content) {
        noticeAdminService.create(title, content);
        return "redirect:/platform-admin/notices";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeAdminService.getOne(id));
        model.addAttribute("formAction", "/platform-admin/notices/" + id);
        model.addAttribute("submitLabel", "수정하기");
        return "admin/notice/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String title, @RequestParam String content) {
        noticeAdminService.update(id, title, content);
        return "redirect:/platform-admin/notices";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        noticeAdminService.delete(id);
        return "redirect:/platform-admin/notices";
    }
}