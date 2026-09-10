package com.greenhealing.studio.product.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.product.service.ProductLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 상품 찜 등록/해제 처리.
 * /products/{id}/like 는 SecurityConfig의 permitAll 목록에 없어서(POST는 /products/**에 없음)
 * 로그인 안 한 사람이 접근하면 자동으로 로그인 화면으로 이동함.
 */
@Controller
@RequiredArgsConstructor
public class ProductLikeController {

    private final ProductLikeService productLikeService;
    private final UserRepository userRepository;

    @PostMapping("/products/{id}/like")
    public String toggleLike(@PathVariable Long id,
                             Authentication authentication,
                             @RequestParam(required = false) String redirectTo) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
        productLikeService.toggleLike(user, id);
        return "redirect:" + (redirectTo != null ? redirectTo : "/products/" + id);
    }
}