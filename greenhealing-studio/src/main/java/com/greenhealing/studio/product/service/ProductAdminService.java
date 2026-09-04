package com.greenhealing.studio.product.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.repository.ProductRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공방 관리자(STUDIO_ADMIN)가 "자기 공방" 상품만 등록/수정/삭제할 수 있도록 하는 서비스.
 * 매 메서드마다 "이 상품이 진짜 내 공방 것이 맞는지" 검사하는 게 핵심이야
 * (안 그러면 URL의 상품 id만 바꿔서 남의 공방 상품을 수정/삭제할 수 있게 되는 보안 구멍이 생김).
 */
@Service
@RequiredArgsConstructor
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final StudioRepository studioRepository;

    /** 로그인한 공방 관리자가 운영하는 공방을 찾아줌 (없으면 오류 - 정상적으로는 항상 있어야 함) */
    @Transactional(readOnly = true)
    public Studio getMyStudio(User owner) {
        return studioRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalStateException("운영 중인 공방 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Product> getMyProducts(User owner) {
        return productRepository.findByStudio(getMyStudio(owner));
    }

    @Transactional(readOnly = true)
    public Product getMyProduct(User owner, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        checkOwnership(owner, product);
        return product;
    }

    @Transactional
    public Long createProduct(User owner, String name, String category, int price, int stock,
                              String description, String imageUrl) {
        Studio myStudio = getMyStudio(owner);
        Product product = Product.builder()
                .studio(myStudio)
                .name(name)
                .category(category)
                .price(price)
                .stock(stock)
                .description(description)
                .imageUrl(imageUrl)
                .build();
        return productRepository.save(product).getId();
    }

    @Transactional
    public void updateProduct(User owner, Long productId, String name, String category, int price, int stock,
                              String description, String imageUrl) {
        Product product = getMyProduct(owner, productId); // 여기서 이미 소유권 검사가 됨
        product.update(name, category, price, stock, description, imageUrl);
    }

    @Transactional
    public void deleteProduct(User owner, Long productId) {
        Product product = getMyProduct(owner, productId);
        productRepository.delete(product);
    }

    private void checkOwnership(User owner, Product product) {
        Studio myStudio = getMyStudio(owner);
        if (!product.belongsTo(myStudio)) {
            throw new IllegalStateException("본인 공방의 상품만 관리할 수 있습니다.");
        }
    }
}