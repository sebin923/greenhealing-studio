package com.greenhealing.studio.admin.service;

import com.greenhealing.studio.notice.domain.Notice;
import com.greenhealing.studio.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 플랫폼 관리자(SUPER_ADMIN)의 공지사항 작성·수정·삭제 서비스. */
@Service
@RequiredArgsConstructor
public class NoticeAdminService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public List<Notice> getAll() {
        return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public Notice getOne(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
    }

    @Transactional
    public Long create(String title, String content) {
        return noticeRepository.save(Notice.builder().title(title).content(content).build()).getId();
    }

    @Transactional
    public void update(Long id, String title, String content) {
        getOne(id).update(title, content);
    }

    @Transactional
    public void delete(Long id) {
        noticeRepository.delete(getOne(id));
    }
}