package com.greenhealing.studio.studio.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.studio.service.StudioFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 공방 즐겨찾기 등록/해제 처리.
 * 이 주소(/studios/**)는 SecurityConfig의 permitAll 목록에 없어서
 * 로그인 안 한 사람이 접근하면 자동으로 로그인 페이지로 튕겨나감.
 */
@Controller
@RequiredArgsConstructor
public class StudioFavoriteController {

    private final StudioFavoriteService studioFavoriteService;
    private final UserRepository userRepository;

    @PostMapping("/studios/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id,
                                 Authentication authentication,
                                 @RequestParam(required = false) String redirectTo) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));

        studioFavoriteService.toggleFavorite(user, id);

        // 버튼을 누른 그 화면(홈 화면이든 마이페이지든)으로 그대로 돌아가게 함
        return "redirect:" + (redirectTo != null ? redirectTo : "/");
    }
}