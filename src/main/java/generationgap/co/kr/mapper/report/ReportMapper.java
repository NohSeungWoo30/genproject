package generationgap.co.kr.mapper.report;

import generationgap.co.kr.dto.report.ReportCategoryDTO;
import generationgap.co.kr.dto.report.ReportReasonDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    boolean checkDuplicateReport(Long reporterId, String entityType, Long entityId);

    String getPostContent(Long entityId);
    String getCommentContent(Long entityId);
    String getUserIntro(Long entityId);
    String getChatContent(Long entityId);

    void insertReportDetail(Map<String, Object> param);


    void insertReport(@Param("categoryId") Long categoryId,
                      @Param("reasonId") Long reasonId,
                      @Param("reporterId") Long reporterId,
                      @Param("reportedUserId") Long reportedUserId,
                      @Param("detailId") Long detailId,
                      @Param("comment") String comment);

    Long findReportDetailId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    List<ReportCategoryDTO> selectReportCategories();
    List<ReportReasonDTO> selectReportReasons();

    int countRecentReports(@Param("userId") Long userId);

}


