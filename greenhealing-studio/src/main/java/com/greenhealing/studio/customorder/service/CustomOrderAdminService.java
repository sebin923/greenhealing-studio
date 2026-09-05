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

/**
 * 공방 관리자(STUDIO_ADMIN)가 "자기 공방으로 들어온" 주문제작 요청을 확인하고
 * 견적을 주거나(승인) 제작이 어려우면 거절하는 서비스.
 * 소비자 쪽 CustomOrderService(신청)와는 역할이 달라서 별도 클래스로 뒀어.
 */
@Service
@RequiredArgsConstructor
public class CustomOrderAdminService {

    private final CustomOrderRepository customOrderRepository;
    private final StudioRepository studioRepository;

    private Studio getMyStudio(User owner) {
        return studioRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("운영 중인 공방 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<CustomOrder> getMyStudioRequests(User owner) {
        return customOrderRepository.findByStudio(getMyStudio(owner));
    }

    /** 이 요청이 진짜 내 공방으로 온 게 맞는지 확인하면서 조회 (다른 공방 요청 못 건드리게) */
    private CustomOrder getMyRequest(User owner, Long requestId) {
        CustomOrder customOrder = customOrderRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));
        if (!customOrder.getStudio().getId().equals(getMyStudio(owner).getId())) {
            throw new IllegalStateException("본인 공방으로 들어온 요청만 처리할 수 있습니다.");
        }
        return customOrder;
    }

    /** 견적 제시 (PENDING -> QUOTED) */
    @Transactional
    public void giveQuote(User owner, Long requestId, int price) {
        getMyRequest(owner, requestId).giveQuote(price);
    }

    /** 제작 시작 (QUOTED -> IN_PROGRESS) */
    @Transactional
    public void startProduction(User owner, Long requestId) {
        getMyRequest(owner, requestId).startProduction();
    }

    /** 제작 완료 (IN_PROGRESS -> COMPLETED) */
    @Transactional
    public void complete(User owner, Long requestId) {
        getMyRequest(owner, requestId).complete();
    }

    /** 제작 불가 처리 (어느 단계에서든 -> REJECTED) */
    @Transactional
    public void reject(User owner, Long requestId) {
        getMyRequest(owner, requestId).reject();
    }
}