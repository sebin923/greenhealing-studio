package com.greenhealing.studio.studio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 공방 관리자(STUDIO_ADMIN) 전용 화면의 진입점.
 * SecurityConfig에 "/studio-admin/**" 는 STUDIO_ADMIN 권한만 접근 가능하도록
 * 이미 막아뒀기 때문에, 다른 권한(CUSTOMER, SUPER_ADMIN)으로 로그인한 사람이나
 * 비로그인 사용자가 이 주소로 들어오면 자동으로 403(권한없음) 또는 로그인 화면으로 이동함.
 *
 * 지금은 화면만 준비해두고, 실제 상품/클래스/주문 관리 기능은
 * 다음 단계에서 이 컨트롤러에 하나씩 채워 넣을 예정이야.
 */
@Controller
public class StudioAdminController {

    @GetMapping("/studio-admin")
    public String home() {
        return "studio/admin-home";
    }
}