package com.greenhealing.studio.order.domain;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.common.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false, length = 200)
    private String shippingAddress;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 30)
    private String receiverPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    // 테스트 결제 여부/키 (실결제 PG 연동 전 단계)
    private String testPaymentKey;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(User user, int totalAmount, String shippingAddress, String receiverName, String receiverPhone) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.status = Status.PAYMENT_PENDING;
    }

    public void addItem(OrderItem item) {
        this.orderItems.add(item);
        item.assignOrder(this);
    }

    public void completeTestPayment(String testPaymentKey) {
        this.testPaymentKey = testPaymentKey;
        this.status = Status.PAID;
    }

    public enum Status {
        PAYMENT_PENDING, PAID, SHIPPING, DELIVERED, CANCELED
    }
}
