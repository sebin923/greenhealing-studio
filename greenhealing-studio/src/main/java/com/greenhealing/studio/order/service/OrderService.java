package com.greenhealing.studio.order.service;

import com.greenhealing.studio.cart.domain.CartItem;
import com.greenhealing.studio.order.domain.Order;
import com.greenhealing.studio.order.domain.OrderItem;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.cart.repository.CartItemRepository;
import com.greenhealing.studio.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * "서비스"는 컨트롤러와 DB 사이에서 실제 비즈니스 로직(핵심 처리)을 담당하는 계층이야.
 *
 * 토스페이먼츠 실결제 연동 후 흐름이 2단계로 나뉨:
 * 1) createPendingOrder : 배송지 입력 단계에서 "결제대기" 상태의 주문만 미리 만들어둠
 *    (아직 재고를 깎지 않음 - 결제가 진짜 성공했을 때만 깎아야, 결제 안 하고 창을 닫아버려도
 *     재고가 영원히 묶여버리는 문제가 안 생김)
 * 2) confirmPayment : 토스 결제창에서 결제가 끝나고 돌아왔을 때, 진짜 승인됐는지 확인하고
 *    그제서야 재고를 깎고 장바구니를 비움
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final TossPaymentService tossPaymentService;

    /** 배송지 입력 후 "결제대기" 상태의 주문을 미리 만들어둠 (아직 재고 차감 X, 장바구니 비우기 X) */
    @Transactional
    public Order createPendingOrder(User user, String receiverName, String receiverPhone, String shippingAddress) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        if (items.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        int total = items.stream()
                .mapToInt(ci -> ci.getProduct().getPrice() * ci.getQuantity())
                .sum();

        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .shippingAddress(shippingAddress)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .build();

        for (CartItem ci : items) {
            Product product = ci.getProduct();
            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(ci.getQuantity())
                    // 지금 상품 가격을 "주문 시점 가격"으로 따로 저장해둠.
                    // 나중에 상품 가격이 바뀌어도 이 주문의 결제 금액은 안 바뀌어야 하니까.
                    .priceAtOrder(product.getPrice())
                    .build());
        }

        return orderRepository.save(order);
    }

    /**
     * 토스 결제창에서 결제가 끝나고 successUrl로 돌아왔을 때 호출.
     * 1) 이 주문이 진짜 이 사람 것이고, 아직 결제 안 된 상태인지 확인
     * 2) 화면에서 넘어온 금액이 우리 DB에 저장된 금액이랑 정확히 같은지 확인
     *    (다르면 - 누군가 화면 요청을 조작해서 실제보다 싼 금액으로 결제하려는 시도일 수 있음)
     * 3) 토스한테 "진짜 승인해도 돼?" 라고 재확인 (TossPaymentService)
     * 4) 승인되면 그제서야 재고 차감 + 장바구니 비우기 + 주문 상태를 결제완료로 변경
     */
    @Transactional
    public void confirmPayment(User user, Long orderId, String paymentKey, int amount) {
        Order order = getOrder(user, orderId);

        if (order.getStatus() != Order.Status.PAYMENT_PENDING) {
            throw new IllegalStateException("이미 처리된 주문입니다.");
        }
        if (order.getTotalAmount() != amount) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        tossPaymentService.confirm(paymentKey, tossOrderId(order), amount);

        // 결제가 진짜 승인됐을 때만 재고를 깎음 (재고 부족하면 여기서 예외 -> 결제는 됐는데 물건을 못 주는
        // 상황이라 실제 서비스라면 이 경우 자동 환불 처리가 필요하지만, 캡스톤 프로젝트 규모상
        // 여기까지는 구현하지 않고 "그런 경우는 없다"고 가정함 - 결제대기 단계에서 이미 장바구니에
        // 담겨있던 수량이라 재고 부족 가능성은 낮음)
        for (OrderItem item : order.getOrderItems()) {
            item.getProduct().decreaseStock(item.getQuantity());
        }

        order.completePayment(paymentKey);
        cartItemRepository.deleteByUser(user);
    }

    /** 우리 내부 주문 id를 토스에 넘길 고유 주문번호 문자열로 변환 */
    public String tossOrderId(Order order) {
        return "order-" + order.getId();
    }

    /**
     * 주문 1건 조회. readOnly=true 는 "이 메서드는 조회만 하고 데이터를 안 바꾼다"는 표시라서
     * DB 성능에 살짝 도움이 됨 (변경 감지를 안 해도 되니까).
     */
    @Transactional(readOnly = true)
    public Order getOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        // 남의 주문 id를 주소창에 쳐서 들어오는 것을 막기 위한 소유권 검사
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 주문만 조회할 수 있습니다.");
        }
        return order;
    }

    /** 내 주문 내역 전체 조회 (마이페이지 만들 때 쓸 예정) */
    @Transactional(readOnly = true)
    public List<Order> getMyOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
}