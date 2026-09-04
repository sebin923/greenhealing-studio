package com.greenhealing.studio.studio.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.domain.StudioFavorite;
import com.greenhealing.studio.studio.repository.StudioFavoriteRepository;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudioFavoriteService {

    private final StudioFavoriteRepository studioFavoriteRepository;
    private final StudioRepository studioRepository;

    /**
     * 즐겨찾기 상태를 뒤집음 (있으면 삭제, 없으면 추가).
     * 버튼 하나로 켜고 끄는 "토글" 방식이라 이렇게 구현하는 게 제일 간단해.
     * 반환값은 "토글 이후" 상태 (true = 지금 즐겨찾기 됨)
     */
    @Transactional
    public boolean toggleFavorite(User user, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공방입니다."));

        return studioFavoriteRepository.findByUserAndStudio(user, studio)
                .map(existing -> {
                    studioFavoriteRepository.delete(existing); // 이미 있으면 -> 삭제 (즐겨찾기 해제)
                    return false;
                })
                .orElseGet(() -> {
                    studioFavoriteRepository.save(StudioFavorite.builder().user(user).studio(studio).build());
                    return true; // 없었으면 -> 새로 추가 (즐겨찾기 등록)
                });
    }

    /** 내가 즐겨찾기한 공방 목록 (마이페이지에서 씀) */
    @Transactional(readOnly = true)
    public List<Studio> getMyFavoriteStudios(User user) {
        return studioFavoriteRepository.findByUser(user).stream()
                .map(StudioFavorite::getStudio)
                .collect(Collectors.toList());
    }

    /**
     * 홈 화면에서 "이 공방, 내가 즐겨찾기 해둔 곳인가?"를 빠르게 확인하기 위해
     * 즐겨찾기한 공방 id들만 Set으로 뽑아줌 (화면에서 매번 DB 조회 안 하고 이 Set에 있는지만 확인하면 됨)
     */
    @Transactional(readOnly = true)
    public Set<Long> getMyFavoriteStudioIds(User user) {
        return studioFavoriteRepository.findByUser(user).stream()
                .map(f -> f.getStudio().getId())
                .collect(Collectors.toSet());
    }
}