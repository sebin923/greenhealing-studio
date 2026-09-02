package com.greenhealing.studio.common.config;

import com.greenhealing.studio.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 로그인 상태라면 nav의 프로필 표시 등에서 바로 쓸 수 있도록
 * loginUserName(이름) / loginUserInitial(아바타 이니셜) 을
 * 모든 화면 Model에 자동으로 채워준다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute
    public void addLoginUser(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }

        userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
            model.addAttribute("loginUserName", user.getName());
            model.addAttribute("loginUserInitial", initialOf(user.getName()));
        });
    }

    private String initialOf(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.substring(0, 1);
    }
}