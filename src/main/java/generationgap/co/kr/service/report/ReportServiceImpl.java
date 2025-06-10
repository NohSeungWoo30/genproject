package generationgap.co.kr.service.report;

import generationgap.co.kr.dto.report.ReportCategoryDTO;
import generationgap.co.kr.dto.report.ReportReasonDTO;
import generationgap.co.kr.dto.report.ReportRequestDto;
import generationgap.co.kr.mapper.report.ReportMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private final ReportMapper reportMapper;

    @Override
    @Transactional
    public void submitReport(ReportRequestDto dto, Long reportingUserId){
        
        // 피신고자 ID null방지
        if (dto.getReportedUserId() == null) {
            throw new IllegalArgumentException("피신고자 ID가 누락되었습니다.");
        }


        //1. 본인 신고 방지
        if(reportingUserId.equals(dto.getReportedUserId())){
            throw new IllegalArgumentException("자기 자신은 신고할 수 없습니다.");
        }

        //2. 중복 신고 방지
        boolean alreadyReported = reportMapper.checkDuplicateReport(
                reportingUserId, dto.getEntityType(), dto.getEntityId()
        );

        if(alreadyReported){
            throw  new IllegalArgumentException("이미 신고한 대상입니다.");
        }

        //3. 대상 존재 여부 확인 및 스냅샷 추출
        String contentSnapshot = getEntityContent(dto.getEntityType(), dto.getEntityId());
        if(contentSnapshot == null){
            throw new IllegalArgumentException("신고 대상이 존재하지 않습니다.");
        }

        //4. report_details 테이블에 저장

        Map<String, Object> param = new HashMap<>();
        param.put("entityType", dto.getEntityType());
        param.put("entityId", dto.getEntityId());
        param.put("content", contentSnapshot);

        reportMapper.insertReportDetail(param);
        Long detailId = reportMapper.findReportDetailId(dto.getEntityType(), dto.getEntityId());


        //5. reports 테이블에 저장
        reportMapper.insertReport(
                dto.getReportCategoryId(),
                dto.getReportReasonId(),
                reportingUserId,
                dto.getReportedUserId(),
                detailId,
                dto.getReportComment()
        );
    }

    /**
     * 신고 대상의 현재 상태를 가져오는 내부 메서드
     */
    private String getEntityContent(String entityType, Long entityId) {
        return switch (entityType) {
            case "POST" -> reportMapper.getPostContent(entityId);
            case "COMMENT" -> reportMapper.getCommentContent(entityId);
            case "USER" -> reportMapper.getUserIntro(entityId);
            case "CHAT" -> reportMapper.getChatContent(entityId);
            default -> null;
        };
    }

    @Override
    public List<ReportCategoryDTO> getReportCategories() {
        return reportMapper.selectReportCategories();
    }

    @Override
    public List<ReportReasonDTO> getReportReasons() {
        return reportMapper.selectReportReasons();
    }
}
