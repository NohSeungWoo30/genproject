package generationgap.co.kr.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * 프론트엔드에서 결제 승인 요청 시 보내는 데이터를 담는 DTO
 */
@Getter
@Setter
public class ConfirmRequest {

    // 토스페이먼츠가 발급한 결제 건의 고유 키
    private String paymentKey;

    // 우리 상점에서 주문을 구분하기 위해 발급한 고유 ID
    private String orderId;

    // 결제된 금액
    private BigDecimal amount;

    // [수정] productName 필드를 추가하여 값을 받을 수 있도록 함
    private String productName;
}