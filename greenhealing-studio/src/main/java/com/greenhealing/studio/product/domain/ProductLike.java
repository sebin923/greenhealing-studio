package com.greenhealing.studio.product.domain;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소비자가 상품을 "찜"한 기록. StudioFavorite(공방 즐겨찾기)랑 구조가 똑같음
 * (좋아요 누르면 생기고, 다시 누르면 사라지는 단순한 방식).
 */
@Entity
@Table(name = "product_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public ProductLike(User user, Product product) {
        this.user = user;
        this.product = product;
    }
}