package generationgap.co.kr.dto.mypage;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class PaymentDto {
    private String productName;
    private Date approvedAt;
    private Long paidAmount;
    private String status;
}