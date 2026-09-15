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

    // 토스페이먼츠가 결제 승인 후 돌려주는 고유 결제번호 (paymentKey)
    private String paymentKey;

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

    /** 토스페이먼츠 결제 승인이 완료됐을 때 호출. paymentKey를 저장하고 결제완료 상태로 바꿈 */
    public void completePayment(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = Status.PAID;
    }

    /** 공방 관리자가 "배송중"으로 변경할 때 씀. 결제완료 상태에서만 가능 */
    public void startShipping() {
        if (this.status != Status.PAID) {
            throw new IllegalStateException("결제 완료 상태의 주문만 배송을 시작할 수 있습니다.");
        }
        this.status = Status.SHIPPING;
    }

    /** 배송완료로 변경. 배송중 상태에서만 가능 */
    public void markDelivered() {
        if (this.status != Status.SHIPPING) {
            throw new IllegalStateException("배송중 상태의 주문만 배송완료로 변경할 수 있습니다.");
        }
        this.status = Status.DELIVERED;
    }

    public enum Status {
        PAYMENT_PENDING, PAID, SHIPPING, DELIVERED, CANCELED
    }
}