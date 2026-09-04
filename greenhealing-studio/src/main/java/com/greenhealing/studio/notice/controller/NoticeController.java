package com.greenhealing.studio.notice.controller;

import com.greenhealing.studio.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepository;

    @GetMapping("/notice")
    public String list(Model model) {
        // 최신 공지가 위로 오도록 등록일 역순 정렬
        model.addAttribute("notices", noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "notice/list";
    }

    @GetMapping("/notice/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다.")));
        return "notice/detail";
    }
}