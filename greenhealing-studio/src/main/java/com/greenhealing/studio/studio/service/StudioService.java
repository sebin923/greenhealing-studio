package com.greenhealing.studio.studio.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "공방 사장님" 회원가입을 처리하는 서비스.
 * 일반 소비자 회원가입(UserService.signup)이랑 거의 비슷하지만,
 * User를 STUDIO_ADMIN 권한으로 만들고, 동시에 Studio(공방) 하나를
 * "미승인" 상태로 같이 만들어준다는 점이 다르다.
 *
 * 공방은 가입한다고 바로 활동 가능한 게 아니라,
 * 플랫폼 관리자가 "승인" 버튼을 눌러줘야 실제로 상품/클래스를 등록할 수 있게 될 예정
 * (이 승인 화면은 나중에 "플랫폼 관리자 페이지" 만들 때 같이 구현함).
 */
@Service
@RequiredArgsConstructor
public class StudioService {

    private final UserRepository userRepository;
    private final StudioRepository studioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signupStudio(String name, String username, String email, String rawPassword, String phone,
                             String studioName, String studioDescription) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 1) 공방 관리자 권한(STUDIO_ADMIN)으로 회원 계정 생성
        User owner = userRepository.save(User.builder()
                .name(name)
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword)) // 비밀번호는 반드시 해시 처리 후 저장
                .phone(phone)
                .role(User.Role.STUDIO_ADMIN)
                .build());

        // 2) 방금 만든 계정을 운영자로 하는 공방을 "미승인" 상태로 생성
        //    Studio.builder() 안에서 approved 는 자동으로 false 로 시작함 (Studio 엔티티 참고)
        Studio studio = Studio.builder()
                .owner(owner)
                .name(studioName)
                .description(studioDescription)
                .build();
        studioRepository.save(studio);

        return owner.getId();
    }

    /** 내 공방 정보 조회 (공방 관리자 전용) */
    @Transactional(readOnly = true)
    public Studio getMyStudio(User owner) {
        return studioRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("운영 중인 공방 정보를 찾을 수 없습니다."));
    }

    /** 내 공방 정보(이름/소개) 수정 */
    @Transactional
    public void updateMyStudio(User owner, String name, String description) {
        getMyStudio(owner).updateInfo(name, description);
    }
}