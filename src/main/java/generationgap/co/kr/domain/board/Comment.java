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
    private LocalDateTime updateAt;
    private String isDeleted;
    private Integer deletedBy;
    private Date deletedAt;
    private String authorNickname; //조인할 때 쓰기
    private String formattedDisplayTime; //LocalDateTime → String 포맷된 값 컨트롤러에서 넘겨주기위해 추가
    private boolean isEdited;            // 수정 여부


}
