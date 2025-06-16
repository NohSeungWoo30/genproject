package generationgap.co.kr.domain.mypage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    private Long userIdx;
    private String currentPassword;
    private String newPassword;
}