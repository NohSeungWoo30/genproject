package generationgap.co.kr.domain.group;


import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class SearchFilterRequest{

    @DateTimeFormat(pattern = "yyyy/MM/dd") // "2025/07/20" 같은 문자열을 LocalDate로 파싱
    private LocalDate groupDate;

    private String region;

    private int minAge; // 최소나이
    private int maxAge; // 최대나이

    private int minParticipants; // 최소 참여수
    private int maxParticipants; // 최대 참여수

    private int categoryMainIdx;

}
