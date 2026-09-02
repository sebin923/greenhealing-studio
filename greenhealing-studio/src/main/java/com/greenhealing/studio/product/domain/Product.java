package com.greenhealing.studio.product.domain;

import com.greenhealing.studio.common.config.BaseTimeEntity;
import com.greenhealing.studio.studio.domain.Studio;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 상품을 판매하는 공방. 모든 상품은 반드시 하나의 공방에 소속된다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category; // 완제품 / 재료 / 키트 등

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Lob
    private String description;

    private String imageUrl;

    @Builder
    public Product(Studio studio, String name, String category, int price, int stock, String description, String imageUrl) {
        this.studio = studio;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다: " + this.name);
        }
        this.stock -= quantity;
    }

    public void updateStock(int stock) {
        this.stock = stock;
    }
}