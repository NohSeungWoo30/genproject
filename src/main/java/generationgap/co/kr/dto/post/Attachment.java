package generationgap.co.kr.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Attachment {
    private Long attachmentIdx;   // 생성된 ID
    private Integer postIdx;
    private Integer uploaderIdx;
    private String fileName;      // 저장된 이름 (UUID 포함)
    private String originalName;  // 사용자 원본 이름
    private Date uploadAt;
}
