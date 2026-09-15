package com.greenhealing.studio.admin.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
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
    // 지금 로그인해있는 사람들의 세션 명부. 정지/삭제할 때 이 사람이 로그인 중이면 강제로 끊어내는 데 씀
    private final SessionRegistry sessionRegistry;

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
        forceLogout(user.getUsername()); // 지금 로그인 중이었다면 즉시 세션 끊기
    }

    @Transactional
    public void activate(Long userId) {
        getMember(userId).activate();
    }

    @Transactional
    public void delete(Long userId) {
        User user = getMember(userId);
        guardAgainstSuperAdmin(user);
        String username = user.getUsername();
        userRepository.delete(user);
        forceLogout(username); // 지금 로그인 중이었다면 즉시 세션 끊기
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

    /**
     * 이 아이디로 지금 로그인해있는 세션이 있으면 강제로 만료시킴.
     * SecurityConfig에 설정해둔 expiredUrl("/") 덕분에, 그 사람이 다음에 아무 페이지나
     * 클릭하는 순간 자동으로 로그인 전 홈 화면으로 튕겨나가게 됨.
     */
    private void forceLogout(String username) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof UserDetails userDetails && userDetails.getUsername().equals(username)) {
                for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                    session.expireNow();
                }
            }
        }
    }
}