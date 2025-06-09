package generationgap.co.kr.mapper.group;

import generationgap.co.kr.service.group.NaverPlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/naver") // 새로운 엔드포인트 경로
public class NaverMapApiController {

    private final NaverPlaceService naverPlaceService;

    public NaverMapApiController(NaverPlaceService naverPlaceService) {
        this.naverPlaceService = naverPlaceService;
    }

    @GetMapping("/places/by-coords")
    //@ResponseBody // JSON 응답을 반환
    public ResponseEntity<String> getPlacesByCoordinates(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "장소") String query, // 기본 검색 키워드 "장소"
            @RequestParam(defaultValue = "1000") int radius, // 기본 반경 1000m (1km)
            @RequestParam(defaultValue = "5") int display // 기본 5개 결과
    ) {
        // [백엔드 로그] 컨트롤러가 받은 파라미터 확인
        System.out.println("--- [백엔드 컨트롤러 로그] ---");
        System.out.println("프론트엔드로부터 받은 검색 요청:");
        System.out.println("  query: " + query);
        System.out.println("  lat: " + lat);
        System.out.println("  lng: " + lng);
        System.out.println("  radius: " + radius);
        System.out.println("  display: " + display);
        System.out.println("-------------------------");
        try {
            System.out.println("lat: " + lat + ", lng: " + lng + ", query: " + query);
            String placesJson = naverPlaceService.searchPlacesByCoordinates(lat, lng, query, radius, display);
            return ResponseEntity.ok(placesJson);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("네이버 플레이스 정보 가져오기 오류: " + e.getMessage());
        }
    }
}
