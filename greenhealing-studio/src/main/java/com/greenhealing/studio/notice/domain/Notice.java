package com.greenhealing.studio.notice.domain;

import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 플랫폼 공지사항. 지금은 SUPER_ADMIN 관리자 화면(작성/수정/삭제)이 아직 없어서
 * DataInitializer로 샘플 데이터만 넣어두고, 소비자가 "조회"하는 화면만 우선 구현함.
 * 나중에 플랫폼 관리자 페이지를 채울 때 이 엔티티에 작성/수정/삭제 기능을 붙이면 됨.
 */
@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public Notice(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /** 플랫폼 관리자가 공지사항을 수정할 때 씀 */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}