package com.greenhealing.studio.studio.domain;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "studios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Studio extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공방을 운영하는 공방 관리자(STUDIO_ADMIN) 계정. 공방 1개 : 운영자 1명(1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false, length = 50)
    private String name;

    @Lob
    private String description;

    // 플랫폼 관리자가 입점 신청을 승인하기 전까지는 상품/클래스가 노출되지 않음
    @Column(nullable = false)
    private boolean approved;

    @Builder
    public Studio(User owner, String name, String description) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.approved = false; // 신청 시점에는 미승인 상태로 시작
    }

    public void approve() {
        this.approved = true;
    }

    public void reject() {
        this.approved = false;
    }
}