package com.greenhealing.studio.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 상품 리뷰 또는 클래스 리뷰 중 하나만 채워짐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_schedule_id")
    private ClassSchedule classSchedule;

    @Column(nullable = false)
    private int rating; // 1~5

    @Lob
    private String content;

    @Builder
    public Review(User user, Product product, ClassSchedule classSchedule, int rating, String content) {
        this.user = user;
        this.product = product;
        this.classSchedule = classSchedule;
        this.rating = rating;
        this.content = content;
    }
}
