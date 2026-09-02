package com.greenhealing.studio.product.controller;

import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 상품 목록/상세 화면을 담당하는 컨트롤러.
 * "컨트롤러"는 브라우저의 요청(GET /products 같은 것)을 받아서,
 * 필요한 데이터를 조회한 뒤, 어떤 화면(html)을 보여줄지 정해주는 역할이야.
 */
@Controller
@RequiredArgsConstructor // final 필드(productRepository)를 자동으로 생성자 주입해줌 (롬복 기능)
public class ProductController {

    // 상품 카테고리는 아직 관리자 페이지가 없어서 일단 코드에 고정값으로 넣어둠.
    // 나중에 공방마다 카테고리를 자유롭게 등록하게 되면 DB에서 조회하는 방식으로 바꿔야 함.
    private static final List<String> CATEGORIES = List.of("완제품", "재료", "키트");

    private final ProductRepository productRepository;

    /**
     * 상품 목록 화면.
     * 예) /products?keyword=러그&category=완제품 처럼 검색어/카테고리를
     * 쿼리 파라미터로 받아서 필터링된 결과를 보여줌.
     * 둘 다 없으면 그냥 전체 목록을 보여줌.
     */
    @GetMapping("/products")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       Model model) {
        // isBlank()는 null이거나 공백만 있는 문자열("", "   ")도 true로 처리해줌
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        List<Product> products;
        // 검색어+카테고리 둘 다 있으면 -> 둘 다 만족하는 것만
        if (hasKeyword && hasCategory) {
            products = productRepository.findByCategoryAndNameContaining(category, keyword);
        // 검색어만 있으면 -> 이름에 검색어가 포함된 것만
        } else if (hasKeyword) {
            products = productRepository.findByNameContaining(keyword);
        // 카테고리만 있으면 -> 그 카테고리인 것만
        } else if (hasCategory) {
            products = productRepository.findByCategory(category);
        // 아무 조건도 없으면 -> 전체 상품
        } else {
            products = productRepository.findAll();
        }

        // model.addAttribute(이름, 값) : 화면(html)에서 ${이름} 으로 꺼내 쓸 수 있게 데이터를 실어보내는 것
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);               // 검색창에 입력했던 값을 그대로 유지하기 위해
        model.addAttribute("selectedCategory", category);     // 드롭다운에서 선택된 카테고리를 유지하기 위해
        model.addAttribute("categories", CATEGORIES);         // 드롭다운 옵션 목록

        // "product/list" 는 templates/product/list.html 파일을 보여주라는 뜻
        return "product/list";
    }

    /**
     * 상품 상세 화면.
     * 예) /products/3 으로 들어오면 id=3 인 상품 정보를 조회해서 보여줌.
     */
    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productRepository.findById(id)
                // findById는 Optional을 반환하니까, 없으면 예외를 던지도록 처리
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다.")));
        return "product/detail";
    }
}
