package generationgap.co.kr.domain.mypage;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class HistoryDto {
    private String title;
    private Date date;
    private Integer participants;
}