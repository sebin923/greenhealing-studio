package com.greenhealing.studio.auth.service;

import com.greenhealing.studio.common.service.MailService;
import com.greenhealing.studio.auth.domain.PasswordResetToken;
import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.PasswordResetTokenRepository;
import com.greenhealing.studio.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_VALID_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public void requestReset(String email) {
        // 가입된 이메일이 아니어도 존재 여부를 노출하지 않기 위해 조용히 리턴
        if (!userRepository.existsByEmail(email)) {
            return;
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES))
                .build();
        tokenRepository.save(resetToken);

        String resetLink = baseUrl + "/password-reset/confirm?token=" + token;
        mailService.send(
                email,
                "[그린힐링 스튜디오] 비밀번호 재설정 안내",
                "아래 링크를 눌러 비밀번호를 재설정해 주세요 (" + TOKEN_VALID_MINUTES + "분간 유효).\n" + resetLink
        );
    }

    @Transactional(readOnly = true)
    public boolean isValidToken(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 링크입니다."));

        if (!resetToken.isValid()) {
            throw new IllegalStateException("만료되었거나 이미 사용된 링크입니다.");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        user.changePassword(passwordEncoder.encode(newPassword));
        resetToken.markUsed();
    }
}