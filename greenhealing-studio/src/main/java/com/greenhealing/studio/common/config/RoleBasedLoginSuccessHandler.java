package com.greenhealing.studio.common.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * 로그인에 성공했을 때 "어디로 보낼지"를 회원 권한(Role)에 따라 다르게 정해주는 핸들러.
 *
 * 원래는 SecurityConfig에 defaultSuccessUrl("/") 하나만 있어서
 * 소비자든 공방 관리자든 플랫폼 관리자든 무조건 일반 홈 화면으로 갔었는데,
 * 이제는 로그인한 사람의 권한(ROLE_XXX)을 확인해서
 * - 공방 관리자(STUDIO_ADMIN)면       -> /studio-admin
 * - 플랫폼 관리자(SUPER_ADMIN)면      -> /platform-admin
 * - 그 외(일반 소비자, CUSTOMER)면    -> / (일반 홈)
 * 으로 각각 다르게 이동시켜준다.
 */
@Component
public class RoleBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectUrl = "/"; // 기본값: 일반 소비자
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority(); // 예: "ROLE_STUDIO_ADMIN"
            if ("ROLE_SUPER_ADMIN".equals(role)) {
                redirectUrl = "/platform-admin";
                break; // 플랫폼 관리자가 최우선이므로 찾으면 바로 종료
            } else if ("ROLE_STUDIO_ADMIN".equals(role)) {
                redirectUrl = "/studio-admin";
                // STUDIO_ADMIN을 찾아도 혹시 모를 SUPER_ADMIN 권한이 더 있을 수 있으니 break 없이 계속 확인
            }
        }

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}