package com.greenhealing.studio.common.config;

import com.greenhealing.studio.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 로그인 실패했을 때, "왜" 실패했는지에 따라 다른 메시지를 보여주기 위한 핸들러.
 *
 * 기본 Spring Security는 "아이디/비밀번호가 틀렸다"는 것 외엔 구분을 잘 안 해주는데
 * (보안상 일부러 뭉뚱그리는 것 - 아이디가 존재하는지 안 하는지 알려주면 공격에 악용될 수 있어서),
 * 이 프로젝트는 사용자 편의를 위해 아래 3가지를 구분해서 보여주기로 함:
 * - 정지된 계정으로 로그인 시도 -> "계정이 정지되었습니다"
 * - 존재하지 않는 아이디(탈퇴/삭제됨)로 로그인 시도 -> "존재하지 않는 계정입니다"
 * - 그 외(아이디는 있는데 비번이 틀림) -> "아이디 또는 비밀번호가 올바르지 않습니다"
 */
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        String reason;

        if (exception instanceof DisabledException) {
            // AuthService에서 .disabled(!user.isEnabled()) 로 넘겨준 값 때문에 여기로 옴 (정지된 계정)
            reason = "suspended";
        } else if (username != null && !userRepository.existsByUsername(username)) {
            // 시도한 아이디 자체가 DB에 없음 (탈퇴/삭제된 계정이거나 애초에 없는 아이디)
            reason = "notfound";
        } else {
            reason = "badcredentials";
        }

        response.sendRedirect(request.getContextPath() + "/login?error=" + reason);
    }
}