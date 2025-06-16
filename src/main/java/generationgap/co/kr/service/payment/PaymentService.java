// src/main/java/generationgap/co/kr/service/payment/PaymentService.java
package generationgap.co.kr.service.payment;

import generationgap.co.kr.dto.ConfirmRequest;
import generationgap.co.kr.domain.payment.Payment;
import generationgap.co.kr.repository.payment.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserMembershipsService userMembershipsService; // 주입
    private final RestTemplate restTemplate;
    private final String secretKey;

    public PaymentService(PaymentRepository paymentRepository,
                          UserMembershipsService userMembershipsService, // 주입
                          RestTemplateBuilder restTemplateBuilder,
                          @Value("${toss.payments.secretKey}") String secretKey) {
        this.paymentRepository = paymentRepository;
        this.userMembershipsService = userMembershipsService; // 주입
        this.restTemplate = restTemplateBuilder.rootUri("https://api.tosspayments.com").build();
        this.secretKey = secretKey;
    }

    @Transactional
    public Payment confirmPayment(ConfirmRequest dto, Long userIdx) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", dto.getPaymentKey());
        body.put("orderId", dto.getOrderId());
        body.put("amount", dto.getAmount().longValue());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/payments/confirm", requestEntity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("결제 승인 실패: " + response.getStatusCode() + " " + response.getBody());
        }

        Map<String, Object> respBody = response.getBody();
        LocalDateTime approvedAt = LocalDateTime.parse(
                Objects.toString(respBody.get("approvedAt")),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );

        Payment payment = new Payment();
        payment.setUserIdx(userIdx);
        payment.setProductName(dto.getProductName());
        payment.setPaymentKey(dto.getPaymentKey());
        payment.setOrderId(dto.getOrderId());
        payment.setPaidAmount(dto.getAmount());
        payment.setApprovedAt(approvedAt);
        payment.setStatus(Objects.toString(respBody.get("status")));

        Payment savedPayment = paymentRepository.save(payment);

        // 결제 성공 후 '추가 구매' 로직 호출
        userMembershipsService.grantMembershipFromPayment(savedPayment);

        return savedPayment;
    }
}