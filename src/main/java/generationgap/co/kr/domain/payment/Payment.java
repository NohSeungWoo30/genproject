package generationgap.co.kr.domain.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_idx")
    private Long paymentIdx;

    @Column(name = "user_idx", nullable = false)
    private Long userIdx;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "payment_key", nullable = false, unique = true, length = 100)
    private String paymentKey;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "payments_status") // DB 컬럼명 매핑
    private String status;
}