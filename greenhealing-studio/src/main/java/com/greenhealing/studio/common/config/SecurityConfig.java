package com.greenhealing.studio.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // 로그인 성공 시 역할별로 다른 화면으로 보내주는 핸들러 (별도 클래스로 분리해둠)
    private final RoleBasedLoginSuccessHandler roleBasedLoginSuccessHandler;
    // 로그인 실패 이유별로 다른 메시지를 보여주는 핸들러
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호는 절대 평문 저장하지 않고 BCrypt로 해시하여 저장
        return new BCryptPasswordEncoder();
    }

    /**
     * 지금 로그인해있는 사람들의 세션(접속 상태)을 기억해두는 registry(명부).
     * 플랫폼 관리자가 회원을 정지/삭제했을 때, 그 사람이 지금 로그인 중이면
     * 이 명부에서 찾아서 강제로 세션을 만료시키는 용도로 씀 (MemberAdminService에서 사용).
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 세션이 생성/종료될 때마다 SessionRegistry에게 알려주는 연결고리.
     * 이게 없으면 SessionRegistry가 "누가 지금 로그인해있는지"를 제대로 못 따라감.
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**",
                                "/signup", "/login", "/classes", "/classes/**", "/community", "/about",
                                "/notice", "/notice/**", "/faq", "/terms", "/privacy", "/studio-signup",
                                "/password-reset", "/password-reset/**", "/api/auth/**").permitAll()
                        // 상품/공방 상세페이지는 GET(조회)만 공개. POST(찜/즐겨찾기 토글 등)는 로그인 필요하므로
                        // 아래 anyRequest().authenticated()에 걸리도록 일부러 permitAll에서 빠뜨림
                        .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/studios/**").permitAll()
                        .requestMatchers("/studio-admin/**").hasRole("STUDIO_ADMIN")
                        .requestMatchers("/platform-admin/**").hasRole("SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // defaultSuccessUrl("/", true) 대신 커스텀 핸들러를 붙여서
                        // 로그인한 사람의 권한에 따라 다른 화면으로 보내도록 함
                        .successHandler(roleBasedLoginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                // 정지/삭제된 회원이 로그인 중이었다면, 다음 요청 때 세션이 만료된 걸 감지해서
                // 자동으로 로그인 전 홈 화면(expiredUrl)으로 보내도록 설정
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/")
                )
                // 개발 초기 단계 임시 설정. 실제 폼 제출(POST) 붙이면 CSRF 토큰을 폼에 반드시 포함시키세요.
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}