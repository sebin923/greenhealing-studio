package com.greenhealing.studio.product.service;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.cart.repository.CartItemRepository;
import com.greenhealing.studio.order.repository.OrderItemRepository;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.repository.ProductLikeRepository;
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
    private final CartItemRepository cartItemRepository;
    private final ProductLikeRepository productLikeRepository;
    private final OrderItemRepository orderItemRepository;

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

    /**
     * 상품 삭제.
     * 원래는 이 상품이 장바구니(cart_items)나 찜(product_likes)에 걸려있으면
     * DB가 "다른 데서 참조하고 있어서 못 지운다"고 막아버리는 에러가 났었음.
     *
     * 그래서:
     * 1) 이미 "주문"된 적 있는 상품이면 - 주문 기록이 깨지면 안 되니까 아예 삭제를 막고 안내 메시지를 줌
     * 2) 장바구니/찜은 "지금 담겨있다"는 임시 상태일 뿐이라, 상품 지우기 전에 먼저 같이 정리해줌
     */
    @Transactional
    public void deleteProduct(User owner, Long productId) {
        Product product = getMyProduct(owner, productId);

        if (orderItemRepository.existsByProduct(product)) {
            throw new IllegalStateException(
                    "이미 주문된 적 있는 상품은 삭제할 수 없습니다. 재고를 0으로 설정해 품절 처리해 주세요.");
        }

        cartItemRepository.deleteByProduct(product);   // 남의 장바구니에 담겨있던 것 정리
        productLikeRepository.deleteByProduct(product); // 찜해둔 기록 정리
        productRepository.delete(product);
    }

    private void checkOwnership(User owner, Product product) {
        Studio myStudio = getMyStudio(owner);
        if (!product.belongsTo(myStudio)) {
            throw new IllegalStateException("본인 공방의 상품만 관리할 수 있습니다.");
        }
    }
}