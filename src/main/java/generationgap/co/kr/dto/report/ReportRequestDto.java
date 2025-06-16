package generationgap.co.kr.dto.report;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReportRequestDto {
    private String entityType; // "USER", "POST", "COMMENT"
    private Long entityId; // 대상 Id
    private Long reportedUserId; // 피신고자ID
    private Long reportCategoryId;
    private Long reportReasonId;
    private String reportComment;

}
