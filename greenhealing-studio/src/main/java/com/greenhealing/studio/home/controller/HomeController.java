package com.greenhealing.studio.home.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.product.repository.ProductRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import com.greenhealing.studio.studio.service.StudioFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;
    private final StudioRepository studioRepository;
    private final StudioFavoriteService studioFavoriteService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        // "신상품" = 가장 최근에 등록된(createdAt 기준) 상품 4개
        model.addAttribute("newProducts",
                productRepository.findAll(PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());

        // 승인된(입점 완료된) 공방만 홈 화면에 소개함 - 승인 대기중인 공방은 아직 노출 안 됨
        List<Studio> studios = studioRepository.findByApproved(true);
        model.addAttribute("studios", studios);

        // 로그인한 사람이면, 이 공방들 중 내가 즐겨찾기한 곳이 어디인지도 같이 넘겨줌
        // (화면에서 "즐겨찾기 됨" 별 표시를 하기 위해)
        User user = currentUserOrNull(authentication);
        if (user != null) {
            model.addAttribute("favoriteStudioIds", studioFavoriteService.getMyFavoriteStudioIds(user));
        } else {
            model.addAttribute("favoriteStudioIds", Set.of());
        }

        return "home/home"; // src/main/resources/templates/home/home.html
    }

    // 클래스 예약은 LessonController에서 실제 구현됨 (이전의 준비중 라우트 제거)
    // 주문제작은 CustomOrderController에서 실제 구현됨 (이전의 준비중 라우트 제거)

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

    /**
     * 마이페이지 (실제 구현).
     * 지금은 "관심 공방" 목록만 보여주고, 주문/예약/주문제작 내역은 다음 단계에서 채울 예정
     * (당장은 /reservations 페이지로 바로가기 링크만 걸어둠).
     */
    @GetMapping("/mypage")
    public String mypage(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));

        model.addAttribute("favoriteStudios", studioFavoriteService.getMyFavoriteStudios(user));
        return "home/mypage";
    }

    // ---------- 푸터 링크 (공지사항/이용약관/개인정보처리방침은 각각 NoticeController, SiteContentController로 이동함) ----------
    @GetMapping("/faq")
    public String faqComingSoon(Model model) {
        model.addAttribute("title", "자주 묻는 질문");
        return "common/coming-soon";
    }

    // 장바구니는 CartController에서 실제 구현됨 (이전의 준비중 라우트 제거)

    /** 로그인 안 했으면 null을 돌려주는 안전한 헬퍼 (홈 화면은 비로그인도 볼 수 있어야 하니까) */
    private User currentUserOrNull(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}