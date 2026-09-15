package com.greenhealing.studio.product.controller;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.service.ProductAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공방 관리자 전용 "내 상품 관리" 화면.
 * 이 컨트롤러의 모든 주소(/studio-admin/products/**)는
 * SecurityConfig에서 이미 STUDIO_ADMIN 권한만 접근 가능하도록 막아뒀기 때문에,
 * 소비자나 플랫폼 관리자, 비로그인 사용자는 아예 들어올 수 없음.
 */
@Controller
@RequestMapping("/studio-admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private static final List<String> CATEGORIES = List.of("완제품", "키트");

    private final ProductAdminService productAdminService;
    private final UserRepository userRepository;

    /** 내 상품 목록 */
    @GetMapping
    public String list(Authentication authentication, Model model) {
        User me = currentUser(authentication);
        model.addAttribute("products", productAdminService.getMyProducts(me));
        model.addAttribute("studio", productAdminService.getMyStudio(me));
        return "studio/product/list";
    }

    /** 상품 등록 폼 */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("formAction", "/studio-admin/products");
        model.addAttribute("submitLabel", "등록하기");
        return "studio/product/form";
    }

    /** 상품 등록 처리 */
    @PostMapping
    public String create(Authentication authentication,
                         @RequestParam String name,
                         @RequestParam String category,
                         @RequestParam int price,
                         @RequestParam int stock,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String imageUrl) {
        productAdminService.createProduct(currentUser(authentication), name, category, price, stock, description, imageUrl);
        return "redirect:/studio-admin/products";
    }

    /** 상품 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        Product product = productAdminService.getMyProduct(currentUser(authentication), id);
        model.addAttribute("product", product);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("formAction", "/studio-admin/products/" + id);
        model.addAttribute("submitLabel", "수정하기");
        return "studio/product/form";
    }

    /** 상품 수정 처리 */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         Authentication authentication,
                         @RequestParam String name,
                         @RequestParam String category,
                         @RequestParam int price,
                         @RequestParam int stock,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String imageUrl) {
        productAdminService.updateProduct(currentUser(authentication), id, name, category, price, stock, description, imageUrl);
        return "redirect:/studio-admin/products";
    }

    /** 상품 삭제 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        try {
            productAdminService.deleteProduct(currentUser(authentication), id);
        } catch (IllegalStateException e) {
            // 이미 주문된 상품이라 삭제가 막힌 경우 등 - 에러 메시지를 화면에 보여주기 위해 쿼리파라미터로 전달
            return "redirect:/studio-admin/products?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/studio-admin/products";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }
}