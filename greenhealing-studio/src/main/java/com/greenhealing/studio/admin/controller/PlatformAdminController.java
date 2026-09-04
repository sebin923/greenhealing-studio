package com.greenhealing.studio.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 플랫폼 관리자(SUPER_ADMIN) 전용 화면의 진입점.
 * SecurityConfig에 "/platform-admin/**" 는 SUPER_ADMIN 권한만 접근 가능하도록
 * 이미 막아뒀음. DataInitializer가 만들어주는 platform 계정으로 로그인해야 들어와짐.
 */
@Controller
public class PlatformAdminController {

    @GetMapping("/platform-admin")
    public String home() {
        return "admin/platform-home";
    }
}