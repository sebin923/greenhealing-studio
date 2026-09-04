package com.greenhealing.studio.lesson.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * "이번 달 달력을 몇 주치 그려야 하는지"를 계산해주는 도우미 클래스.
 * /classes 페이지(클래스 개수)와 /reservations 페이지(예약 개수) 둘 다
 * 이 클래스를 그대로 재사용함 - 세는 대상만 다를 뿐 달력을 그리는 방식은 똑같으니까.
 */
public class CalendarUtil {

    private CalendarUtil() {
        // 유틸리티 클래스라 인스턴스를 만들 필요가 없음 (전부 static 메서드)
    }

    /**
     * anchorMonth가 속한 달의 달력을, 일요일 시작 기준으로 "주 단위" 리스트로 만들어줌.
     * countsByDate : 날짜별로 몇 개가 있는지 미리 세어둔 것 (예: 2026-09-09 -> 3개)
     */
    public static List<List<CalendarDay>> buildMonthCalendar(LocalDate anchorMonth, Map<LocalDate, Integer> countsByDate) {
        LocalDate firstOfMonth = anchorMonth.withDayOfMonth(1);
        LocalDate lastOfMonth = firstOfMonth.plusMonths(1).minusDays(1);

        // DayOfWeek는 월=1 ~ 일=7 이라서, "일요일 시작" 달력을 만들려면
        // 이번 달 1일 기준으로 며칠을 앞으로 당겨야 하는지 이렇게 계산함 (일요일이면 0칸, 월요일이면 1칸 ...)
        int offset = firstOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate cursor = firstOfMonth.minusDays(offset);

        List<List<CalendarDay>> weeks = new ArrayList<>();
        while (true) {
            List<CalendarDay> week = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                boolean inMonth = !cursor.isBefore(firstOfMonth) && !cursor.isAfter(lastOfMonth);
                int count = countsByDate.getOrDefault(cursor, 0);
                week.add(new CalendarDay(cursor, cursor.getDayOfMonth(), inMonth, count));
                cursor = cursor.plusDays(1);
            }
            weeks.add(week);
            // 이번 달 마지막 날을 이미 지났으면 (한 주를 다 채운 뒤) 종료
            if (cursor.isAfter(lastOfMonth)) {
                break;
            }
        }
        return weeks;
    }
}