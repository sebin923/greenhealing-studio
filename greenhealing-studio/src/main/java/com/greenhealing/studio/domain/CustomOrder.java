package com.greenhealing.studio.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String size; // 예: 60x90cm

    @Column(length = 30)
    private String color;

    private String designImageUrl;

    @Lob
    private String requestNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    private Integer quotedPrice; // 운영자가 확정한 견적 (검토 전 null)

    @Builder
    public CustomOrder(User user, String size, String color, String designImageUrl, String requestNote) {
        this.user = user;
        this.size = size;
        this.color = color;
        this.designImageUrl = designImageUrl;
        this.requestNote = requestNote;
        this.status = Status.PENDING;
    }

    public void giveQuote(int price) {
        this.quotedPrice = price;
        this.status = Status.QUOTED;
    }

    public void startProduction() {
        this.status = Status.IN_PROGRESS;
    }

    public void complete() {
        this.status = Status.COMPLETED;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }

    public enum Status {
        PENDING,      // 견적 검토 중
        QUOTED,       // 견적 완료
        IN_PROGRESS,  // 제작 중
        COMPLETED,    // 완성/배송
        REJECTED      // 제작 불가
    }
}
