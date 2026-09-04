package com.greenhealing.studio.content.repository;

import com.greenhealing.studio.content.domain.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {
    Optional<SiteContent> findByContentKey(SiteContent.Key key);
}