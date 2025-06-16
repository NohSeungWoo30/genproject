package generationgap.co.kr.service.report;

import generationgap.co.kr.dto.report.ReportCategoryDTO;
import generationgap.co.kr.dto.report.ReportReasonDTO;
import generationgap.co.kr.dto.report.ReportRequestDto;

import java.util.List;

public interface ReportService {

    void submitReport(ReportRequestDto dto, Long reportingUserId);

    List<ReportCategoryDTO> getReportCategories();
    List<ReportReasonDTO> getReportReasons();
}
