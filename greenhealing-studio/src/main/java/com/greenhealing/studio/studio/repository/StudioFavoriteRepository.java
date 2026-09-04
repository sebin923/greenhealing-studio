package com.greenhealing.studio.studio.repository;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.domain.StudioFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudioFavoriteRepository extends JpaRepository<StudioFavorite, Long> {
    List<StudioFavorite> findByUser(User user);
    Optional<StudioFavorite> findByUserAndStudio(User user, Studio studio);
    boolean existsByUserAndStudio(User user, Studio studio);
}