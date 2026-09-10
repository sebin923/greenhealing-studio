package com.greenhealing.studio.product.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.domain.ProductLike;
import com.greenhealing.studio.product.repository.ProductLikeRepository;
import com.greenhealing.studio.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;

    /** 찜 토글 (있으면 취소, 없으면 추가). Product.likeCount도 같이 갱신함 */
    @Transactional
    public boolean toggleLike(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        return productLikeRepository.findByUserAndProduct(user, product)
                .map(existing -> {
                    productLikeRepository.delete(existing);
                    product.decreaseLike();
                    return false;
                })
                .orElseGet(() -> {
                    productLikeRepository.save(ProductLike.builder().user(user).product(product).build());
                    product.increaseLike();
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public List<Product> getMyLikedProducts(User user) {
        return productLikeRepository.findByUser(user).stream()
                .map(ProductLike::getProduct)
                .collect(Collectors.toList());
    }

    /** 상품 목록/상세 화면에서 "내가 찜한 상품인지" 버튼 표시용 */
    @Transactional(readOnly = true)
    public Set<Long> getMyLikedProductIds(User user) {
        return productLikeRepository.findByUser(user).stream()
                .map(l -> l.getProduct().getId())
                .collect(Collectors.toSet());
    }
}