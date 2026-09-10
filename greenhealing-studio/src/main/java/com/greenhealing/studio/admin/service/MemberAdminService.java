package com.greenhealing.studio.admin.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 플랫폼 관리자(SUPER_ADMIN)가 전체 회원을 조회·검색·정지·삭제하는 서비스.
 * SUPER_ADMIN 계정 자기 자신은 정지/삭제 대상에서 제외함 (실수로 관리자 계정을 없애버리는 사고 방지).
 */
@Service
@RequiredArgsConstructor
public class MemberAdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getMembers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return userRepository.search(keyword);
    }

    @Transactional
    public void suspend(Long userId) {
        User user = getMember(userId);
        guardAgainstSuperAdmin(user);
        user.suspend();
    }

    @Transactional
    public void activate(Long userId) {
        getMember(userId).activate();
    }

    @Transactional
    public void delete(Long userId) {
        User user = getMember(userId);
        guardAgainstSuperAdmin(user);
        userRepository.delete(user);
    }

    private User getMember(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    /** SUPER_ADMIN 계정은 정지/삭제 대상에서 제외 */
    private void guardAgainstSuperAdmin(User user) {
        if (user.getRole() == User.Role.SUPER_ADMIN) {
            throw new IllegalStateException("플랫폼 관리자 계정은 정지하거나 삭제할 수 없습니다.");
        }
    }
}