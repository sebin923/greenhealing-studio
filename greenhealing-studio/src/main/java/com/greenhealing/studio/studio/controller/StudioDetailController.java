package com.greenhealing.studio.studio.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.lesson.repository.ClassScheduleRepository;
import com.greenhealing.studio.product.repository.ProductRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import com.greenhealing.studio.studio.service.StudioFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

/**
 * 소비자가 "공방 하나"를 클릭했을 때 보는 상세 화면.
 * 그 공방의 소개, 판매중인 상품, 예정된 클래스를 한 화면에 모아서 보여줌.
 * (기존에는 홈 화면 공방 카드가 클릭이 안 됐는데, 이제 클릭하면 여기로 옴)
 */
@Controller
@RequiredArgsConstructor
public class StudioDetailController {

    private final StudioRepository studioRepository;
    private final ProductRepository productRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final StudioFavoriteService studioFavoriteService;
    private final UserRepository userRepository;

    @GetMapping("/studios/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        Studio studio = studioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공방입니다."));

        model.addAttribute("studio", studio);
        model.addAttribute("products", productRepository.findByStudio(studio));

        // 지난 클래스는 굳이 안 보여주고, 오늘 이후 예정된 것만 날짜순으로 보여줌
        model.addAttribute("classes", classScheduleRepository.findByStudio(studio).stream()
                .filter(c -> !c.getClassDate().isBefore(LocalDate.now()))
                .sorted((a, b) -> a.getClassDate().compareTo(b.getClassDate()))
                .toList());

        // 로그인한 사람이면, 이 공방을 즐겨찾기 했는지도 같이 확인
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("isFavorite", studioFavoriteService.getMyFavoriteStudioIds(user).contains(id));
            }
        }

        return "studiodetail/view";
    }
}