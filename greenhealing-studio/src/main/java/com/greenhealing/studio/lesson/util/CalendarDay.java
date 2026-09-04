package com.greenhealing.studio.lesson.util;

import java.time.LocalDate;

/**
 * 달력 한 칸(하루)을 나타내는 작은 그릇(record).
 * date       : 이 칸이 가리키는 실제 날짜
 * dayOfMonth : 화면에 숫자로 보여줄 "일" (예: 15)
 * inMonth    : 이번 달 날짜가 맞는지 (달력 앞뒤로 삐져나온 지난달/다음달 날짜는 false)
 * count      : 그 날짜에 해당하는 것의 개수 (클래스 개수 or 예약 개수) - 0이면 점 표시를 안 함
 */
public record CalendarDay(LocalDate date, int dayOfMonth, boolean inMonth, int count) {
}