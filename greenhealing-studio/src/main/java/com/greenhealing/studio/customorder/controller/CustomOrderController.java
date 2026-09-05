package com.greenhealing.studio.customorder.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.customorder.domain.CustomOrder;
import com.greenhealing.studio.customorder.service.CustomOrderService;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 소비자가 원하는 공방에 주문제작(커스텀)을 신청하는 화면.
 * 이 컨트롤러의 주소들은 SecurityConfig의 permitAll 목록에서 뺐기 때문에
 * 로그인 안 한 사람이 접근하면 자동으로 로그인 화면으로 이동함
 * (장바구니, 클래스예약과 같은 방식).
 */
@Controller
@RequiredArgsConstructor
public class CustomOrderController {

    private final CustomOrderService customOrderService;
    private final StudioRepository studioRepository;
    private final UserRepository userRepository;

    /** 신청 폼. 승인된 공방 목록을 드롭다운으로 보여줘서 그중 하나를 고르게 함 */
    @GetMapping("/custom-order")
    public String form(Model model) {
        model.addAttribute("studios", studioRepository.findByApproved(true));
        return "customorder/form";
    }

    /** 신청 처리 */
    @PostMapping("/custom-order")
    public String submit(Authentication authentication,
                         @RequestParam Long studioId,
                         @RequestParam String size,
                         @RequestParam(required = false) String color,
                         @RequestParam(required = false) String designImageUrl,
                         @RequestParam(required = false) String requestNote,
                         Model model) {
        try {
            customOrderService.submit(currentUser(authentication), studioId, size, color, designImageUrl, requestNote);
            return "redirect:/custom-orders?submitted=success";
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("studios", studioRepository.findByApproved(true));
            return "customorder/form";
        }
    }

    /** 내 주문제작 신청 내역 (마이페이지에서 연결됨) */
    @GetMapping("/custom-orders")
    public String myList(Authentication authentication, Model model) {
        List<CustomOrder> customOrders = customOrderService.getMyCustomOrders(currentUser(authentication));
        model.addAttribute("customOrders", customOrders);
        return "customorder/my-list";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}