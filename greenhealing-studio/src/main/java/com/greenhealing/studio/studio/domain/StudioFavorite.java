package com.greenhealing.studio.studio.domain;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소비자가 "관심 공방"으로 즐겨찾기한 기록.
 * User 1명이 Studio 1곳을 즐겨찾기하면 이 테이블에 한 줄 생기고,
 * 즐겨찾기 취소하면 그 줄을 그냥 지워버리는 단순한 구조야
 * (좋아요 누르면 생기고, 다시 누르면 없어지는 것과 같은 방식).
 */
@Entity
@Table(name = "studio_favorites",
        // 같은 사람이 같은 공방을 두 번 즐겨찾기하지 못하도록 DB 차원에서도 막아둠
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "studio_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudioFavorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Builder
    public StudioFavorite(User user, Studio studio) {
        this.user = user;
        this.studio = studio;
    }
}