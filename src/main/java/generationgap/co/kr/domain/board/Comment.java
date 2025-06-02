package generationgap.co.kr.domain.board;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;
import java.util.Date;

@Alias("Comment")
@Getter
@Setter
public class Comment {
    private Long commentIdx;
    private int postIdx;
    private int commenterIdx;
    private Long parentCommentId;
    private String content;
    private LocalDateTime createdAt;
    private String isDeleted;
    private Integer deletedBy;
    private Date deletedAt;
    private String authorNickname; //조인할 때 쓰기

}
