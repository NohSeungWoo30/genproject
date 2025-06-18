package generationgap.co.kr.domain.group;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GroupSearchResponseDTO {

    private Long groupIdx;
    private String title;
    private String categoryMainName;
    private LocalDate groupDate;
    private String placeAddress;
    private String groupImgUrl;

    private String district; // 장소 주소에서 지역구만 빼서 저장
}
