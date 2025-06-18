// src/main/java/generationgap/co/kr/domain/payment/UserMemberships.java
package generationgap.co.kr.domain.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_memberships")
@Getter
@Setter
public class UserMemberships {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_idx")
    private Long membershipIdx;

    @Column(name = "user_idx", nullable = false, unique = true) // 한 유저당 하나의 레코드만 갖도록 unique = true 추가
    private Long userIdx;

    @Column(name = "payment_idx") // 가장 마지막 결제의 idx를 저장하거나, null로 유지할 수 있습니다.
    private Long paymentIdx;

    // [수정] description 필드 제거
    // @Column(name = "description", length = 100)
    // private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "remaining_uses")
    private Integer remainingUses = 0; // null을 방지하기 위해 기본값 0으로 초기화

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";
}