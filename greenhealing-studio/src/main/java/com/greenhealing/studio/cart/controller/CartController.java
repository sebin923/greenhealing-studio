package com.greenhealing.studio.cart.controller;

import com.greenhealing.studio.cart.domain.CartItem;
import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장바구니 화면 + 담기/수량변경/삭제 요청을 처리하는 컨트롤러.
 * @RequestMapping("/cart") 를 클래스에 붙여두면, 아래 메서드들의 주소 앞에
 * 전부 자동으로 "/cart" 가 붙어. (예: @PostMapping("/add") -> 실제로는 POST /cart/add)
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;      // 실제 담기/변경 로직은 여기 다 있고, 컨트롤러는 호출만 함
    private final UserRepository userRepository; // 로그인한 사람이 누구인지 찾을 때 씀

    /** 장바구니 화면 보여주기 (GET /cart) */
    @GetMapping
    public String view(Authentication authentication, Model model) {
        // Authentication : 스프링 시큐리티가 "지금 로그인한 사람이 누구인지" 담아서 자동으로 넘겨주는 객체
        User user = currentUser(authentication);
        List<CartItem> items = cartService.getCartItems(user);

        // 장바구니 전체 금액 계산: 상품마다 (가격 × 수량)을 다 더함
        // stream().mapToInt(...).sum() 은 "리스트를 하나씩 돌면서 숫자로 바꾼 뒤 다 더하기" 라고 생각하면 됨
        int total = items.stream().mapToInt(ci -> ci.getProduct().getPrice() * ci.getQuantity()).sum();

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart/cart"; // templates/cart/cart.html 을 보여줌
    }

    /** 장바구니에 상품 담기 (POST /cart/add) - 상품 목록/상세 페이지의 "담기" 버튼이 여기로 옴 */
    @PostMapping("/add")
    public String add(Authentication authentication,
                      @RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity, // 수량을 안 보내면 기본 1개
                      @RequestParam(required = false) String redirectTo) { // 담은 뒤 돌아갈 주소 (선택)
        cartService.addToCart(currentUser(authentication), productId, quantity);
        // redirectTo 가 있으면 그 주소로, 없으면 그냥 장바구니로 돌아감
        return "redirect:" + (redirectTo != null ? redirectTo : "/cart");
    }

    /** 장바구니 상품 수량 변경 (POST /cart/update) */
    @PostMapping("/update")
    public String update(Authentication authentication,
                         @RequestParam Long cartItemId, // 장바구니 "줄"의 id (상품 id가 아님! 주의)
                         @RequestParam int quantity) {
        cartService.updateQuantity(currentUser(authentication), cartItemId, quantity);
        return "redirect:/cart";
    }

    /** 장바구니 상품 삭제 (POST /cart/remove) */
    @PostMapping("/remove")
    public String remove(Authentication authentication, @RequestParam Long cartItemId) {
        cartService.removeItem(currentUser(authentication), cartItemId);
        return "redirect:/cart";
    }

    /**
     * Authentication 안에는 로그인 아이디(username) 문자열만 들어있어서,
     * 실제 User 엔티티(이름/이메일 등 전부)가 필요하면 이렇게 DB에서 한 번 더 찾아와야 함.
     */
    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}
