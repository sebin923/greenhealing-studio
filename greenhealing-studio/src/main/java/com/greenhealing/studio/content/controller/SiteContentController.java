package com.greenhealing.studio.content.controller;

import com.greenhealing.studio.content.domain.SiteContent;
import com.greenhealing.studio.content.repository.SiteContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SiteContentController {

    private final SiteContentRepository siteContentRepository;

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("content", getOrPlaceholder(SiteContent.Key.TERMS, "이용약관"));
        return "content/view";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("content", getOrPlaceholder(SiteContent.Key.PRIVACY, "개인정보처리방침"));
        return "content/view";
    }

    // 혹시 DataInitializer가 아직 안 돌았거나 데이터가 없는 경우를 대비한 안전장치
    private SiteContent getOrPlaceholder(SiteContent.Key key, String title) {
        return siteContentRepository.findByContentKey(key)
                .orElse(SiteContent.builder().contentKey(key).title(title).body("준비 중입니다.").build());
    }
}