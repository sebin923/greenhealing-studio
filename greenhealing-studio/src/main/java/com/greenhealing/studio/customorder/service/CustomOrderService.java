package com.greenhealing.studio.customorder.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.customorder.domain.CustomOrder;
import com.greenhealing.studio.customorder.repository.CustomOrderRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final StudioRepository studioRepository;

    /** 주문제작 신청 생성. 승인된(입점 완료) 공방에만 신청할 수 있게 검사함 */
    @Transactional
    public Long submit(User user, Long studioId, String size, String color, String designImageUrl, String requestNote) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공방입니다."));
        if (!studio.isApproved()) {
            throw new IllegalStateException("아직 승인되지 않은 공방에는 주문제작을 신청할 수 없습니다.");
        }

        CustomOrder customOrder = CustomOrder.builder()
                .user(user)
                .studio(studio)
                .size(size)
                .color(color)
                .designImageUrl(designImageUrl)
                .requestNote(requestNote)
                .build();

        return customOrderRepository.save(customOrder).getId();
    }

    @Transactional(readOnly = true)
    public List<CustomOrder> getMyCustomOrders(User user) {
        return customOrderRepository.findByUser(user);
    }
}