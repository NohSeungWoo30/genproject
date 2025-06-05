package generationgap.co.kr.service.group;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
        /*Geocoding API (X-Naver-Client-Id, X-Naver-Client-Secret 사용)*/
        headers.set("X-Naver-Client-Id", NAVER_CLIENT_ID.trim());
        headers.set("X-Naver-Client-Secret", NAVER_CLIENT_SECRET.trim());
        headers.set("Content-Type", "application/json");
        return headers;
    }
    /**
     * 특정 좌표 주변의 네이버 플레이스 정보를 검색합니다.
     * @param lat 위도
     * @param lng 경도
     * @param query 검색 키워드 (예: "음식점", "카페", "병원", "장소" 등)
     * @param radius 검색 반경 (미터 단위, 기본값 1000m)
     * @param display 검색 결과 수 (기본값 5)
     * @return 네이버 검색 API 응답 (JSON 문자열)
     */
    // Geocoding (주소 -> 좌표) API 호출
    public String searchPlacesByCoordinates(double lat, double lng, String query, int radius, int display) {
        try {
            // 쿼리 인코딩
            String encodedQuery = null;
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            // 네이버 지역 검색 API URL
            // x는 경도, y는 위도
            // sort=comment : 사용자 리뷰 수가 많은 순, sim : 정확도순
            String apiUrl = String.format("https://openapi.naver.com/v1/search/local.json" +
                            "?query=%s" +
                            "&display=%d" +
                            "&start=1" +
                            "&sort=sim" +
                            "&x=%.6f" +
                            "&y=%.6f" +
                            "&radius=%d",
                    encodedQuery, display, lng, lat, radius);
            HttpHeaders headers = createNaverApiHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            return response.getBody();

        } catch (HttpClientErrorException e) {
            System.err.println("네이버 Geocoding API 호출 중 클라이언트 에러: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Geocoding API 호출 오류: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            System.err.println("네이버 Geocoding API 호출 중 RestClient 에러: " + e.getMessage());
            throw new RuntimeException("네트워크 또는 Geocoding API 호출 중 알 수 없는 오류 발생", e);
        }
    }



}
