package com.greenhealing.studio.order.controller;

import com.greenhealing.studio.cart.domain.CartItem;
import com.greenhealing.studio.order.domain.Order;
import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.cart.service.CartService;
import com.greenhealing.studio.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제(주문서 작성 → 토스 결제창 → 승인확인 → 완료 화면) 흐름을 담당하는 컨트롤러.
 * 실제 "주문 만들고, 재고 깎고, 결제 승인하는" 로직은 전부 OrderService 에 있고,
 * 여긴 화면 왔다갔다하는 것만 처리해.
 */
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;

    @Value("${toss.client-key}")
    private String tossClientKey;

    @Value("${app.base-url}")
    private String appBaseUrl;

    /** 결제 페이지 보여주기 (GET /checkout). 장바구니에서 "주문/결제 하기" 누르면 여기로 옴 */
    @GetMapping("/checkout")
    public String checkoutForm(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        List<CartItem> items = cartService.getCartItems(user);

        if (items.isEmpty()) {
            return "redirect:/cart";
        }
        int total = items.stream().mapToInt(ci -> ci.getProduct().getPrice() * ci.getQuantity()).sum();

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("loginUserNameForForm", user.getName());
        model.addAttribute("loginUserPhoneForForm", user.getPhone());
        return "order/checkout";
    }

    /**
     * 배송지 입력 후 "결제하기" 누르면 여기로 옴 (POST /checkout).
     * 이 단계에서는 아직 실제 결제가 안 됨 - "결제대기" 상태의 주문만 만들고,
     * 토스 결제창을 띄우는 화면(/orders/{id}/pay)으로 넘어감.
     */
    @PostMapping("/checkout")
    public String checkout(Authentication authentication,
                           @RequestParam String receiverName,
                           @RequestParam String receiverPhone,
                           @RequestParam String shippingAddress,
                           Model model) {
        User user = currentUser(authentication);
        try {
            Order order = orderService.createPendingOrder(user, receiverName, receiverPhone, shippingAddress);
            return "redirect:/orders/" + order.getId() + "/pay";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /** 토스 결제창을 띄우는 화면. 여기서 "결제하기" 버튼을 누르면 진짜 토스 결제창이 뜸 */
    @GetMapping("/orders/{id}/pay")
    public String pay(@PathVariable Long id, Authentication authentication, Model model) {
        Order order = orderService.getOrder(currentUser(authentication), id);
        if (order.getStatus() != Order.Status.PAYMENT_PENDING) {
            // 이미 결제됐거나 취소된 주문이면 결제창을 또 띄울 필요 없음
            return "redirect:/orders/" + id + "/complete";
        }
        model.addAttribute("order", order);
        model.addAttribute("tossOrderId", orderService.tossOrderId(order));
        model.addAttribute("tossClientKey", tossClientKey);
        model.addAttribute("successUrl", appBaseUrl + "/orders/" + id + "/toss-success");
        model.addAttribute("failUrl", appBaseUrl + "/orders/" + id + "/toss-fail");
        return "order/pay";
    }

    /** 토스 결제창에서 결제 성공하면 토스가 이 주소로 돌려보내줌 (paymentKey, orderId, amount 포함) */
    @GetMapping("/orders/{id}/toss-success")
    public String tossSuccess(@PathVariable Long id,
                              @RequestParam String paymentKey,
                              @RequestParam int amount,
                              Authentication authentication,
                              Model model) {
        User user = currentUser(authentication);
        try {
            orderService.confirmPayment(user, id, paymentKey, amount);
            return "redirect:/orders/" + id + "/complete";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("orderId", id);
            return "order/pay-fail";
        }
    }

    /** 토스 결제창에서 결제 실패/취소하면 토스가 이 주소로 돌려보내줌 */
    @GetMapping("/orders/{id}/toss-fail")
    public String tossFail(@PathVariable Long id,
                           @RequestParam(required = false) String message,
                           Model model) {
        model.addAttribute("error", message != null ? message : "결제가 취소되었습니다.");
        model.addAttribute("orderId", id);
        return "order/pay-fail";
    }

    /** 주문 완료 화면 (GET /orders/3/complete 같은 식) */
    @GetMapping("/orders/{id}/complete")
    public String complete(@PathVariable Long id, Authentication authentication, Model model) {
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