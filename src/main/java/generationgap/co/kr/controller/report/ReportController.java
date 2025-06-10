package generationgap.co.kr.controller.report;

import generationgap.co.kr.dto.report.ReportRequestDto;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> reportContent(@RequestBody ReportRequestDto dto,
                                           @AuthenticationPrincipal CustomUserDetails user){
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        reportService.submitReport(dto, user.getUserIdx());
        return ResponseEntity.ok(Map.of("message", "신고가 접수되었습니다."));
    }

    @GetMapping("/options")
    public ResponseEntity<Map<String, Object>> getReportOptions(){
        Map<String, Object> options = new HashMap<>();
        options.put("categories", reportService.getReportCategories());
        options.put("reasons", reportService.getReportReasons());
        return ResponseEntity.ok(options);
    }



}
