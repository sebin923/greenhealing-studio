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
import java.util.UUID;

/**
 * "서비스"는 컨트롤러와 DB 사이에서 실제 비즈니스 로직(핵심 처리)을 담당하는 계층이야.
 * 여기선 "결제하면 실제로 무슨 일이 일어나야 하는지"를 다 처리해:
 * 주문 생성 → 재고 차감 → 테스트 결제 처리 → 장바구니 비우기.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * 결제 처리의 핵심 메서드.
     * @Transactional : 이 메서드 안의 모든 DB 작업(주문 저장, 재고 차감, 장바구니 삭제)을
     * "하나의 묶음"으로 처리해줘. 중간에 하나라도 실패하면(예외 발생) 전부 취소(롤백)됨.
     * → 예를 들어 재고 부족으로 에러가 나면, 이미 만들어지던 주문도 통째로 없었던 일이 됨.
     */
    @Transactional
    public Order checkout(User user, String receiverName, String receiverPhone, String shippingAddress) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        if (items.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        // 총 결제금액 = 각 상품의 (가격 × 수량)을 다 더한 값
        int total = items.stream()
                .mapToInt(ci -> ci.getProduct().getPrice() * ci.getQuantity())
                .sum();

        // 1) 주문(Order) 뼈대를 먼저 만듦 (아직 상품 목록은 안 들어있음)
        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .shippingAddress(shippingAddress)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .build();

        // 2) 장바구니에 담겨있던 상품 하나하나를 주문상품(OrderItem)으로 옮겨 담으면서
        //    동시에 재고도 그만큼 깎음
        for (CartItem ci : items) {
            Product product = ci.getProduct();
            product.decreaseStock(ci.getQuantity()); // 결제 시 즉시 재고 차감 (재고 부족하면 예외 발생 -> 전체 롤백)
            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(ci.getQuantity())
                    // 지금 상품 가격을 "주문 시점 가격"으로 따로 저장해둠.
                    // 나중에 상품 가격이 바뀌어도 이 주문의 결제 금액은 안 바뀌어야 하니까.
                    .priceAtOrder(product.getPrice())
                    .build());
        }

        // 3) 주문을 DB에 저장
        Order saved = orderRepository.save(order);

        // 4) "테스트 결제" 처리. 실제 PG사(토스페이먼츠 등) 연동 전이라
        //    UUID로 가짜 결제번호만 만들어서 결제완료 상태로 바꿔줌
        saved.completeTestPayment("TEST-" + UUID.randomUUID());

        // 5) 결제가 끝났으니 장바구니는 비워줌
        cartItemRepository.deleteByUser(user);

        return saved;
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
