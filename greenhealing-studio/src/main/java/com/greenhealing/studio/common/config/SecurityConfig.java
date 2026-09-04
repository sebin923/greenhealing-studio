package com.greenhealing.studio.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // 로그인 성공 시 역할별로 다른 화면으로 보내주는 핸들러 (별도 클래스로 분리해둠)
    private final RoleBasedLoginSuccessHandler roleBasedLoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호는 절대 평문 저장하지 않고 BCrypt로 해시하여 저장
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/products", "/products/**",
                                "/signup", "/login", "/classes", "/classes/**", "/custom-order", "/community", "/about",
                                "/notice", "/notice/**", "/faq", "/terms", "/privacy", "/studio-signup",
                                "/password-reset", "/password-reset/**", "/api/auth/**").permitAll()
                        .requestMatchers("/studio-admin/**").hasRole("STUDIO_ADMIN")
                        .requestMatchers("/platform-admin/**").hasRole("SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // defaultSuccessUrl("/", true) 대신 커스텀 핸들러를 붙여서
                        // 로그인한 사람의 권한에 따라 다른 화면으로 보내도록 함
                        .successHandler(roleBasedLoginSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                // 개발 초기 단계 임시 설정. 실제 폼 제출(POST) 붙이면 CSRF 토큰을 폼에 반드시 포함시키세요.
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}