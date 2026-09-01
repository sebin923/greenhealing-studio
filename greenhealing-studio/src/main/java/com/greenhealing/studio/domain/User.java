package com.greenhealing.studio.domain;

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
    public User(String name, String email, String password, String phone, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role == null ? Role.USER : role;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public enum Role {
        USER, ADMIN
    }
}
