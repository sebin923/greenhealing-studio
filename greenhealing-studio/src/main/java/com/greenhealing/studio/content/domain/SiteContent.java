package com.greenhealing.studio.content.domain;

import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이용약관, 개인정보처리방침처럼 "페이지 하나에 긴 글 하나"만 있는
 * 사이트 고정 콘텐츠를 담는 엔티티. key로 어떤 문서인지 구분함.
 * 나중에 플랫폼 관리자 화면에서 이 content를 수정하는 기능을 붙이면 됨
 * (지금은 DataInitializer로 초기 내용만 넣어둠).
 */
@Entity
@Table(name = "site_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiteContent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private Key contentKey;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Builder
    public SiteContent(Key contentKey, String title, String body) {
        this.contentKey = contentKey;
        this.title = title;
        this.body = body;
    }

    public void update(String body) {
        this.body = body;
    }

    public enum Key {
        TERMS, PRIVACY
    }
}