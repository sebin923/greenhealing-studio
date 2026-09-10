package com.greenhealing.studio.order.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.order.domain.Order;
import com.greenhealing.studio.order.repository.OrderItemRepository;
import com.greenhealing.studio.order.repository.OrderRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공방 관리자(STUDIO_ADMIN)가 "내 공방 상품이 포함된 주문"을 확인하고
 * 배송 상태를 변경(결제완료 -> 배송중 -> 배송완료)하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StudioRepository studioRepository;

    private Studio getMyStudio(User owner) {
        return studioRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("운영 중인 공방 정보를 찾을 수 없습니다."));
    }

    /** 내 공방 상품이 하나라도 포함된 주문 목록 (최신순, 중복 제거) */
    @Transactional(readOnly = true)
    public List<Order> getMyStudioOrders(User owner) {
        Studio myStudio = getMyStudio(owner);
        // OrderItem 단위로 조회한 뒤, 같은 Order에 내 상품이 여러 개 담겨있어도
        // 화면에는 주문 하나로만 보이도록 LinkedHashSet으로 중복 제거함
        return orderItemRepository.findByProductStudio(myStudio).stream()
                .map(item -> item.getOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .collect(Collectors.toList());
    }

    /** 이 주문에 내 공방 상품이 진짜 포함돼있는지 확인하면서 조회 (다른 공방 주문 못 건드리게) */
    private Order getMyStudioOrder(User owner, Long orderId) {
        Studio myStudio = getMyStudio(owner);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        boolean hasMyProduct = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().belongsTo(myStudio));
        if (!hasMyProduct) {
            throw new IllegalStateException("본인 공방 상품이 포함된 주문만 관리할 수 있습니다.");
        }
        return order;
    }

    @Transactional
    public void startShipping(User owner, Long orderId) {
        getMyStudioOrder(owner, orderId).startShipping();
    }

    @Transactional
    public void markDelivered(User owner, Long orderId) {
        getMyStudioOrder(owner, orderId).markDelivered();
    }
}