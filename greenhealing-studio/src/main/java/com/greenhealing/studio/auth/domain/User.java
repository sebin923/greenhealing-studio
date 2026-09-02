package com.greenhealing.studio.auth.domain;

import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String username; // 로그인용 아이디 (이메일과 별개)

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // BCrypt로 해시된 값만 저장 (평문 저장 금지)
    @Column(nullable = false)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    public User(String name, String username, String email, String password, String phone, Role role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role == null ? Role.CUSTOMER : role;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public enum Role {
        CUSTOMER,      // 소비자
        STUDIO_ADMIN,  // 공방 관리자
        SUPER_ADMIN    // 플랫폼 관리자
    }
}