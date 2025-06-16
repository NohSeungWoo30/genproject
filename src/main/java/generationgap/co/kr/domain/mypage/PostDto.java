package generationgap.co.kr.domain.mypage; // mypage 패키지를 새로 만들어 관리하는 것을 추천

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class PostDto {
    private String title;
    private String category;
    private Date createdAt;
    private int views;
}