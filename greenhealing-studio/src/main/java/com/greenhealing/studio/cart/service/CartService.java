package com.greenhealing.studio.cart.service;

import com.greenhealing.studio.cart.domain.CartItem;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.cart.repository.CartItemRepository;
import com.greenhealing.studio.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 장바구니 담기/조회/수량변경/삭제의 실제 로직을 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * 장바구니에 상품 담기.
     * 이미 같은 상품이 장바구니에 있으면 → 수량만 더해줌 (중복으로 새 줄이 안 생기게)
     * 없으면 → 새로운 CartItem을 하나 만들어서 저장
     */
    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // findByUserAndProduct : "이 사람 장바구니에 이 상품이 이미 있는지" 찾아봄
        // ifPresentOrElse(있을때, 없을때) 는 자바 8+ 문법. Optional을 if/else 처럼 쓰는 방법
        cartItemRepository.findByUserAndProduct(user, product)
                .ifPresentOrElse(
                        existing -> existing.changeQuantity(existing.getQuantity() + quantity), // 있으면: 수량 추가
                        () -> cartItemRepository.save(                                          // 없으면: 새로 생성
                                CartItem.builder().user(user).product(product).quantity(quantity).build())
                );
        // 참고: existing.changeQuantity(...) 만 호출하고 save()를 안 부르는데도 DB에 반영되는 이유는
        // @Transactional 안에서 조회해온 엔티티를 수정하면, 트랜잭션이 끝날 때 JPA가 자동으로
        // "변경된 걸 감지해서" UPDATE 쿼리를 날려주기 때문이야 (이걸 "더티 체킹"이라고 불러).
    }

    /** 특정 사용자의 장바구니 전체 조회 */
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    /** 장바구니 항목 수량 변경. 0개 이하로 바꾸면 아예 삭제 처리함 */
    @Transactional
    public void updateQuantity(User user, Long cartItemId, int quantity) {
        CartItem item = getOwnedItem(user, cartItemId);
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.changeQuantity(quantity);
        }
    }

    /** 장바구니 항목 삭제 */
    @Transactional
    public void removeItem(User user, Long cartItemId) {
        cartItemRepository.delete(getOwnedItem(user, cartItemId));
    }

    /**
     * cartItemId로 장바구니 항목을 찾아오되, "이게 진짜 이 사람 것이 맞는지"도 함께 검사해주는 헬퍼.
     * 이게 없으면, 다른 사람이 자기 cartItemId를 몰래 남의 id로 바꿔서 요청 보냈을 때
     * 남의 장바구니를 수정/삭제할 수 있게 되는 보안 구멍이 생김.
     */
    private CartItem getOwnedItem(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 장바구니만 수정할 수 있습니다.");
        }
        return item;
    }
}
