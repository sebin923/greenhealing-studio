package com.greenhealing.studio.auth.repository;

import com.greenhealing.studio.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // 플랫폼 관리자 - 회원 검색용 (이름, 아이디, 이메일 중 하나라도 검색어를 포함하면 결과에 나옴)
    @Query("select u from User u where u.name like %:keyword% or u.username like %:keyword% or u.email like %:keyword% order by u.createdAt desc")
    List<User> search(@Param("keyword") String keyword);
}