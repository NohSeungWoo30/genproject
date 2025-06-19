package generationgap.co.kr.dto.mypage; // mypage 패키지를 새로 만들어 관리하는 것을 추천

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class PostDto {
    // 게시물 식별을 위한 인덱스
    private Integer postIdx;

    private String title;
    private String category;
    private Date createdAt;
    private int views;
    private String authorName;

    // 매퍼에서 조회되는 좋아요 및 댓글 수
    private int likeCount;
    private int commentCount;
}
