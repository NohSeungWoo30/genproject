package generationgap.co.kr.domain.board;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Post {

    private Integer authorIdx; // 사용자 번호 (지금은 1로 고정)


    private Long postIdx;
    private String title;
    private String category; //공지, 자유, 질문, 정보
    private String content;
    private String authorName;
    private int viewCount;
    private int likeCount;
    private Date createdAt;
    private int commentCount; //댓글 갯수 표시 추가
    private String isDeleted;     // 'Y' or 'N'
    private Integer deletedBy;    // 삭제한 사용자 번호 (관리자 등)
    private Date deletedAt;       // 삭제 시각
    private Date updateAt;        // 수정 시각
}
