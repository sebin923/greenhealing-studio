package com.greenhealing.studio.order.repository;

import com.greenhealing.studio.order.domain.OrderItem;
import com.greenhealing.studio.studio.domain.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * "내 공방 상품이 포함된 주문"을 찾기 위한 쿼리.
     * product.studio 를 기준으로 찾음.
     *
     * 참고: 한 주문(Order)에 여러 공방 상품이 섞여 담길 수도 있는 구조라서
     * (장바구니에 여러 공방 상품을 같이 담을 수 있으니까), 엄밀히는
     * "주문 하나 = 공방 하나"가 아닐 수 있음. 지금은 프로젝트 규모상
     * 이 부분까지 세분화하지 않고, "내 상품이 하나라도 포함된 주문 전체"를
     * 공방 관리자가 볼 수 있게 단순화해서 구현함.
     */
    @Query("select oi from OrderItem oi where oi.product.studio = :studio order by oi.order.createdAt desc")
    List<OrderItem> findByProductStudio(@Param("studio") Studio studio);

    // 이 상품이 한 번이라도 주문된 적 있는지 확인 (있으면 삭제 막아야 함 - 주문 기록이 깨지니까)
    boolean existsByProduct(com.greenhealing.studio.product.domain.Product product);
}