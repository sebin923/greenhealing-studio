package com.greenhealing.studio.home.controller;

import com.greenhealing.studio.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newProducts",
                productRepository.findAll(PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        return "home/home"; // src/main/resources/templates/home/home.html
    }

    // 아직 구현 전인 메뉴는 임시 안내 페이지로 연결 (죽은 링크 방지)
    @GetMapping("/classes")
    public String classesComingSoon(Model model) {
        model.addAttribute("title", "클래스 예약");
        return "common/coming-soon";
    }

    @GetMapping("/custom-order")
    public String customOrderComingSoon(Model model) {
        model.addAttribute("title", "주문제작");
        return "common/coming-soon";
    }

    @GetMapping("/community")
    public String communityComingSoon(Model model) {
        model.addAttribute("title", "커뮤니티");
        return "common/coming-soon";
    }

    @GetMapping("/about")
    public String aboutComingSoon(Model model) {
        model.addAttribute("title", "소개");
        return "common/coming-soon";
    }

    // 로그인해야만 접근 가능 (SecurityConfig의 anyRequest().authenticated() 규칙 적용됨)
    @GetMapping("/mypage")
    public String mypageComingSoon(Model model) {
        model.addAttribute("title", "마이페이지");
        return "common/coming-soon";
    }

    // ---------- 푸터 링크 ----------
    @GetMapping("/notice")
    public String noticeComingSoon(Model model) {
        model.addAttribute("title", "공지사항");
        return "common/coming-soon";
    }

    @GetMapping("/faq")
    public String faqComingSoon(Model model) {
        model.addAttribute("title", "자주 묻는 질문");
        return "common/coming-soon";
    }

    @GetMapping("/terms")
    public String termsComingSoon(Model model) {
        model.addAttribute("title", "이용약관");
        return "common/coming-soon";
    }

    @GetMapping("/privacy")
    public String privacyComingSoon(Model model) {
        model.addAttribute("title", "개인정보처리방침");
        return "common/coming-soon";
    }

    // 장바구니는 CartController에서 실제 구현됨 (이전의 준비중 라우트 제거)
}