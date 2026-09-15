package com.greenhealing.studio.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스페이먼츠 "결제 승인 API"를 호출하는 서비스.
 *
 * 결제 흐름을 그림으로 그리면:
 * 1) 우리 서버에서 주문을 "결제대기" 상태로 미리 만들어둠
 * 2) 화면에서 토스 결제창을 띄워서 사용자가 카드 정보 등을 입력하고 인증까지 마침
 *    (이 단계까지는 아직 "진짜 결제"가 안 된 상태 - 돈이 안 빠져나감)
 * 3) 토스가 우리 successUrl로 paymentKey를 담아서 돌려보내줌
 * 4) *여기가 이 클래스가 하는 일* - 우리 서버가 그 paymentKey를 들고 토스한테
 *    "진짜 승인해도 돼?" 라고 다시 한번 물어봄 (이 요청에 돈이 실제로 움직임)
 * 5) 토스가 승인해주면 그제서야 진짜 결제 완료
 *
 * 이렇게 서버가 한 번 더 확인하는 이유: 화면(자바스크립트)에서 오는 정보는
 * 사용자가 마음대로 조작할 수 있어서 못 믿음. 시크릿키를 가진 서버끼리의 통신만 믿을 수 있음.
 */
@Service
public class TossPaymentService {

    @Value("${toss.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    /**
     * 결제 승인 요청.
     * 실패하면(카드 한도초과, 이미 처리된 결제 등) IllegalStateException을 던짐.
     */
    public void confirm(String paymentKey, String tossOrderId, int amount) {
        // 시크릿키를 "시크릿키:" 형태로 만든 다음 Base64로 인코딩해서 인증 헤더를 만듦
        // (토스페이먼츠가 정해준 인증 방식 - Basic 인증)
        String encodedAuth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", tossOrderId);
        body.put("amount", amount);

        try {
            restTemplate.postForEntity(CONFIRM_URL, new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException e) {
            // 토스가 승인을 거절한 경우 (카드한도초과 등) - 에러 내용을 그대로 화면에 보여줄 수 있게 전달
            throw new IllegalStateException("결제 승인에 실패했습니다: " + e.getResponseBodyAsString());
        }
    }
}