package com.greenhealing.studio.order.controller;

import com.greenhealing.studio.cart.domain.CartItem;
import com.greenhealing.studio.order.domain.Order;
import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.cart.service.CartService;
import com.greenhealing.studio.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제(주문서 작성 → 테스트 결제 → 완료 화면) 흐름을 담당하는 컨트롤러.
 * 실제 "결제해서 재고 깎고 주문을 만드는" 로직은 전부 OrderService 에 있고,
 * 여긴 화면 왔다갔다하는 것만 처리해.
 */
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;

    /**
     * 결제 페이지 보여주기 (GET /checkout).
     * 장바구니 페이지에서 "주문/결제 하기" 버튼을 누르면 여기로 옴.
     */
    @GetMapping("/checkout")
    public String checkoutForm(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        List<CartItem> items = cartService.getCartItems(user);

        // 장바구니가 비어있는데 주소를 직접 쳐서 /checkout 으로 들어온 경우 방어
        if (items.isEmpty()) {
            return "redirect:/cart";
        }
        int total = items.stream().mapToInt(ci -> ci.getProduct().getPrice() * ci.getQuantity()).sum();

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        // 배송지 폼에 이름/연락처를 미리 채워주기 위해 로그인한 사람 정보를 같이 넘김
        model.addAttribute("loginUserNameForForm", user.getName());
        model.addAttribute("loginUserPhoneForForm", user.getPhone());
        return "order/checkout";
    }

    /**
     * 실제 결제(테스트) 처리 (POST /checkout).
     * "테스트 결제하기" 버튼을 누르면 배송지 정보와 함께 여기로 옴.
     */
    @PostMapping("/checkout")
    public String checkout(Authentication authentication,
                           @RequestParam String receiverName,
                           @RequestParam String receiverPhone,
                           @RequestParam String shippingAddress,
                           Model model) {
        User user = currentUser(authentication);
        try {
            // 실제 "주문 만들기 + 재고 차감 + 장바구니 비우기" 는 OrderService 가 다 함
            Order order = orderService.checkout(user, receiverName, receiverPhone, shippingAddress);
            // 성공하면 주문 완료 화면으로 이동 (주문 id를 주소에 포함해서, 어떤 주문인지 알 수 있게)
            return "redirect:/orders/" + order.getId() + "/complete";
        } catch (IllegalStateException e) {
            // 재고 부족 등으로 실패 시 장바구니로 돌려보내고 사유 표시
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /** 주문 완료 화면 (GET /orders/3/complete 같은 식) */
    @GetMapping("/orders/{id}/complete")
    public String complete(@PathVariable Long id, Authentication authentication, Model model) {
        // getOrder 안에서 "이 주문이 진짜 내 주문 맞는지"도 같이 검사함
        // (남의 주문번호를 주소창에 쳐서 들어오는 걸 막기 위함)
        Order order = orderService.getOrder(currentUser(authentication), id);
        model.addAttribute("order", order);
        return "order/order-complete";
    }

    /** 내 주문 내역 (마이페이지에서 연결됨) */
    @GetMapping("/orders")
    public String myOrders(Authentication authentication, Model model) {
        model.addAttribute("orders", orderService.getMyOrders(currentUser(authentication)));
        return "order/my-orders";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}