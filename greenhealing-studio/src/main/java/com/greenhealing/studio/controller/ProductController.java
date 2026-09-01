package com.greenhealing.studio.controller;

import com.greenhealing.studio.domain.Product;
import com.greenhealing.studio.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping("/products")
    public String list(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다.")));
        return "products/detail";
    }
}
