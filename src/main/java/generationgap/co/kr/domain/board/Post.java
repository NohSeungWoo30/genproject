package generationgap.co.kr.domain.board;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Post {
    private Long postIdx;
    private String title;
    private String content;
    private String authorName;
    private int viewCount;
    private int likeCount;
    private Date createdAt;
}
