package com.greenhealing.studio.studio.repository;

import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudioRepository extends JpaRepository<Studio, Long> {
    Optional<Studio> findByOwner(User owner);
    List<Studio> findByApproved(boolean approved);
}