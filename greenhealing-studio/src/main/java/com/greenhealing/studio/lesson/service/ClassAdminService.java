package com.greenhealing.studio.lesson.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.lesson.domain.ClassSchedule;
import com.greenhealing.studio.lesson.repository.ClassScheduleRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 공방 관리자(STUDIO_ADMIN)가 "자기 공방" 클래스만 등록/수정/삭제할 수 있도록 하는 서비스.
 * ProductAdminService랑 구조가 거의 똑같음 (소유권 검사 패턴 재사용).
 */
@Service
@RequiredArgsConstructor
public class ClassAdminService {

    private final ClassScheduleRepository classScheduleRepository;
    private final StudioRepository studioRepository;

    @Transactional(readOnly = true)
    public Studio getMyStudio(User owner) {
        return studioRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("운영 중인 공방 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<ClassSchedule> getMyClasses(User owner) {
        return classScheduleRepository.findByStudio(getMyStudio(owner));
    }

    @Transactional(readOnly = true)
    public ClassSchedule getMyClass(User owner, Long classId) {
        ClassSchedule classSchedule = classScheduleRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클래스입니다."));
        checkOwnership(owner, classSchedule);
        return classSchedule;
    }

    @Transactional
    public Long createClass(User owner, String title, LocalDate classDate, LocalTime startTime, LocalTime endTime,
                            int capacity, int price) {
        Studio myStudio = getMyStudio(owner);
        ClassSchedule classSchedule = ClassSchedule.builder()
                .studio(myStudio)
                .title(title)
                .classDate(classDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(capacity)
                .price(price)
                .build();
        return classScheduleRepository.save(classSchedule).getId();
    }

    @Transactional
    public void updateClass(User owner, Long classId, String title, LocalDate classDate, LocalTime startTime,
                            LocalTime endTime, int capacity, int price) {
        ClassSchedule classSchedule = getMyClass(owner, classId); // 여기서 이미 소유권 검사됨
        classSchedule.update(title, classDate, startTime, endTime, capacity, price);
    }

    @Transactional
    public void deleteClass(User owner, Long classId) {
        ClassSchedule classSchedule = getMyClass(owner, classId);
        classScheduleRepository.delete(classSchedule);
    }

    private void checkOwnership(User owner, ClassSchedule classSchedule) {
        Studio myStudio = getMyStudio(owner);
        if (!classSchedule.belongsTo(myStudio)) {
            throw new IllegalStateException("본인 공방의 클래스만 관리할 수 있습니다.");
        }
    }
}