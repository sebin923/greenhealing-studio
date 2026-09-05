package com.greenhealing.studio.admin.service;

import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 플랫폼 관리자(SUPER_ADMIN)가 공방 입점 신청을 승인/거절하는 서비스.
 * 여기서 승인해줘야만 그 공방의 상품·클래스가 홈 화면 등 소비자 화면에 노출됨
 * (StudioFavoriteService, HomeController 등에서 findByApproved(true)로 걸러서 조회하기 때문).
 */
@Service
@RequiredArgsConstructor
public class StudioAdminService {

    private final StudioRepository studioRepository;

    /** 승인 대기중인 공방 목록 */
    @Transactional(readOnly = true)
    public List<Studio> getPendingStudios() {
        return studioRepository.findByApproved(false);
    }

    /** 승인된 공방 목록 (관리 화면에서 "현재 입점중" 확인용) */
    @Transactional(readOnly = true)
    public List<Studio> getApprovedStudios() {
        return studioRepository.findByApproved(true);
    }

    @Transactional
    public void approve(Long studioId) {
        Studio studio = getStudio(studioId);
        studio.approve();
    }

    @Transactional
    public void reject(Long studioId) {
        Studio studio = getStudio(studioId);
        studio.reject();
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공방입니다."));
    }
}