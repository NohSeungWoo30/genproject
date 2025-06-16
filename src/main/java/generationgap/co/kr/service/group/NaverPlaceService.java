package generationgap.co.kr.service.group;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NaverPlaceService {
    @Value("${naver.api.client.id}")
    private String NAVER_CLIENT_ID; // 발급받은 Client ID
    @Value("${naver.api.client.secret}")
    private String NAVER_CLIENT_SECRET; // 발급받은 Client Secret

    private final RestTemplate restTemplate;

    public NaverPlaceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 모든 @Value 주입이 완료된 후, 초기화 메서드에서 헤더 설정
    public HttpHeaders createNaverApiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        /*local API (X-Naver-Client-Id, X-Naver-Client-Secret 사용)*/
        headers.set("X-Naver-Client-Id", NAVER_CLIENT_ID.trim());
        headers.set("X-Naver-Client-Secret", NAVER_CLIENT_SECRET.trim());
        headers.set("Content-Type", "application/json");
        return headers;
    }
    /**
     * 특정 좌표 주변의 네이버 플레이스 정보를 검색합니다.
     * @param lat 위도 southwest_lat southwest_lng
     * @param lng 경도 northeast_lat northeast_lng
     * @param query 검색 키워드 (예: "음식점", "카페", "병원", "장소" 등)
     * @param display 검색 결과 수 프론트 스크립트에서 조절 displayCount
     * @return 네이버 검색 API 응답 (JSON 문자열)
     */
    // LOCAL
    // 장소 검색 (지정된 좌표와 반경 내에서 검색)
    public String searchPlacesByCoordinates(double southwest_lat, double southwest_lng, double northeast_lat, double northeast_lng, String query, int display) {
        try {
            // 1. 지도 영역의 중심점 계산
            // 위도 (latitude)는 y, 경도 (longitude)는 x
            double centerLat = (southwest_lat + northeast_lat) / 2.0;
            double centerLng = (southwest_lng + northeast_lng) / 2.0;

            int radius = 20000;

            String apiUrl = String.format("https://openapi.naver.com/v1/search/local.json" +
                            "?query=%s" +
                            "&display=%d" +
                            "&start=1" +
                            "&sort=sim" +
                            "&x=%.6f" +
                            "&y=%.6f" +
                            "&radius=%d",
                    query, display, centerLng, centerLat, radius); // 파라미터 순서도 맞게 수정

            System.out.println("네이버 API 요청 URL: " + apiUrl);
            HttpHeaders headers = createNaverApiHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // [백엔드 로그] 네이버 API로부터 받은 원본 응답 JSON 확인
                System.out.println("--- [백엔드 서비스 로그] ---");
                System.out.println("네이버 API 원본 응답 JSON:");
                System.out.println(response.getBody());
                System.out.println("-------------------------");
            return response.getBody();
        } else {
                System.err.println("네이버 지역 검색 API 호출 실패: " + response.getStatusCode() + " " + response.getBody());
                throw new RuntimeException("네이버 지역 검색 API 호출 실패: " + response.getStatusCode() + " " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("네이버 지역 검색 API 호출 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("네이버 지역 검색 API 처리 중 오류 발생", e);
        }
    }



}
