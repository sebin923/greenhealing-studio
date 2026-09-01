package com.greenhealing.studio.service;

import com.greenhealing.studio.domain.User;
import com.greenhealing.studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(String name, String email, String rawPassword, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword)) // 해시 처리 후 저장
                .phone(phone)
                .role(User.Role.USER)
                .build();

        return userRepository.save(user).getId();
    }
}
