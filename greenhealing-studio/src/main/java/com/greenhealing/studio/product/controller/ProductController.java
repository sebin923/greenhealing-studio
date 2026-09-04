package com.greenhealing.studio.product.controller;

import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 목록/상세 화면을 담당하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class ProductController {

    private static final int PAGE_SIZE = 12; // 한 페이지에 보여줄 상품 개수

    // 나중에 공방마다 카테고리를 자유롭게 등록하게 되면 DB에서 조회하는 방식으로 바꿔야 함
    private static final List<String> CATEGORIES = List.of("완제품", "키트");

    private final ProductRepository productRepository;

    /**
     * 상품 목록 화면.
     * page 파라미터로 몇 번째 페이지인지 받고(0부터 시작), 한 번에 12개씩 끊어서 보여줌.
     * 예) /products?category=완제품&page=1  -> 완제품 카테고리의 두 번째 페이지(13~24번째 상품)
     */
    @GetMapping("/products")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        // id 역순(최신 등록순)으로 정렬해서, 12개 단위로 잘라 보여줌
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));

        Page<Product> result;
        if (hasKeyword && hasCategory) {
            result = productRepository.findByCategoryAndNameContaining(category, keyword, pageable);
        } else if (hasKeyword) {
            result = productRepository.findByNameContaining(keyword, pageable);
        } else if (hasCategory) {
            result = productRepository.findByCategory(category, pageable);
        } else {
            result = productRepository.findAll(pageable);
        }

        // 왼쪽 사이드바에 "완제품 (16)" 처럼 카테고리별 개수를 같이 보여주기 위해 미리 세어둠
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        for (String c : CATEGORIES) {
            categoryCounts.put(c, productRepository.countByCategory(c));
        }

        model.addAttribute("products", result.getContent());  // 이번 페이지에 보여줄 12개(혹은 이하)
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalCount", result.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("categoryCounts", categoryCounts);
        return "product/list";
    }

    /** 상품 상세 (누구나 조회 가능) */
    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다.")));
        return "product/detail";
    }
}