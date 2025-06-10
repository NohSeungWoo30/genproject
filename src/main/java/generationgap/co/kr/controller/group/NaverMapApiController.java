package generationgap.co.kr.controller.group;

import generationgap.co.kr.service.group.NaverPlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/naver") // 새로운 엔드포인트 경로
public class NaverMapApiController {

    private final NaverPlaceService naverPlaceService;

    public NaverMapApiController(NaverPlaceService naverPlaceService) {
        this.naverPlaceService = naverPlaceService;
    }

    @GetMapping("/places/by-coords")
    @ResponseBody // JSON 응답을 반환
    public ResponseEntity<String> getPlacesByCoordinates(
            //@RequestParam double lat,
            //@RequestParam double lng,
            @RequestParam double southwest_lat,
            @RequestParam double southwest_lng,
            @RequestParam double northeast_lat,
            @RequestParam double northeast_lng,
            @RequestParam(defaultValue = "장소") String query,
            @RequestParam int display
    ) {
        // [백엔드 로그] 컨트롤러가 받은 파라미터 확인
        System.out.println("--- [백엔드 컨트롤러 로그] ---");
        System.out.println("프론트엔드로부터 받은 검색 요청:");
        System.out.println("  query: " + query);
        System.out.println("  southwest_lat: " + southwest_lat);
        System.out.println("  southwest_lng: " + southwest_lng);
        System.out.println("  northeast_lat: " + northeast_lat);
        System.out.println("  northeast_lng: " + northeast_lng);
        System.out.println("-------------------------");
        try {
            //System.out.println("lat: " + lat + ", lng: " + lng + ", query: " + query);
            String placesJson = naverPlaceService.searchPlacesByCoordinates(southwest_lat, southwest_lng, northeast_lat, northeast_lng, query, display);
            return ResponseEntity.ok(placesJson);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("네이버 플레이스 정보 가져오기 오류: " + e.getMessage());
        }
    }
}
