package generationgap.co.kr.domain.mypage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInfoDTO {
    private Long userIdx;
    private String nickname;
    private String introduction;
}