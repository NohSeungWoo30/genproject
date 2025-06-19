package generationgap.co.kr.dto.mypage;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class HistoryDto {
    private String groupName;
    private Date participatedAt;
    private Integer participants;
}