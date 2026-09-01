package com.greenhealing.studio.service;

import com.greenhealing.studio.domain.EmailVerification;
import com.greenhealing.studio.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int CODE_VALID_MINUTES = 5;

    private final EmailVerificationRepository emailVerificationRepository;
    private final MailService mailService;

    @Transactional
    public void sendVerificationCode(String email) {
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_VALID_MINUTES))
                .build();
        emailVerificationRepository.save(verification);

        mailService.send(
                email,
                "[그린힐링 스튜디오] 이메일 인증번호",
                "인증번호는 [" + code + "] 입니다. " + CODE_VALID_MINUTES + "분 이내에 입력해 주세요."
        );
    }

    @Transactional
    public boolean verifyCode(String email, String inputCode) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElse(null);

        if (verification == null || verification.isExpired() || !verification.matches(inputCode)) {
            return false;
        }

        verification.markVerified();
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return emailVerificationRepository.findTopByEmailOrderByIdDesc(email)
                .map(v -> v.isVerified() && !v.isExpired())
                .orElse(false);
    }
}