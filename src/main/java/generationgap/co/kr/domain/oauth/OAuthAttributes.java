package generationgap.co.kr.domain.oauth;

import generationgap.co.kr.domain.user.UserDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.time.LocalDate; // LocalDate 임포트

@Getter
@Slf4j
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String userId;           // ⭐ 추가: OAuth 제공자의 고유 식별자 (예: Google의 'sub')
    private String userName; // 기존 name -> userName
    private String email;
    private String profileName; // 기존 picture -> profileName
    private String registrationId;

    // --- Google People API로 가져올 추가 정보 필드 (UserDTO 필드명에 맞춤) ---
    private String phone;        // phoneNumber -> phone (String으로 임시 저장 후 변환)
    private String genderString; // gender (String) -> genderString (원본 문자열)
    private String birthDateString; // birthday (String) -> birthDateString (원본 문자열)
    // ------------------------------------------------------------------

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String userName, String email, String profileName, String registrationId, String userId,
                           String phone, String genderString, String birthDateString) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.userId = userId; // ⭐ 추가된 필드 초기화
        this.userName = userName;
        this.email = email;
        this.profileName = profileName;
        this.registrationId = registrationId;
        this.phone = phone;
        this.genderString = genderString;
        this.birthDateString = birthDateString;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            // 이 메서드는 `CustomOAuth2UserService`에서 `OAuth2User.getAttributes()`만으로 초기 생성되므로,
            // People API에서 가져올 정보는 여기서 직접 설정되지 않습니다.
            // CustomOAuth2UserService에서 People API 호출 후, OAuthAttributes.builder()... 에 직접 주입합니다.
            return OAuthAttributes.builder()
                    .userName((String) attributes.get("name"))
                    .email((String) attributes.get("email"))
                    .profileName((String) attributes.get("picture"))
                    .attributes(attributes)
                    .nameAttributeKey(userNameAttributeName)
                    .registrationId("google")
                    .userId((String) attributes.get("sub"))
                    .build();
        }
        log.warn("지원하지 않는 OAuth 제공자: {}", registrationId);
        throw new IllegalArgumentException("Unsupported OAuth provider: " + registrationId);
    }

    // UserDTO로 변환하는 메서드 수정
    public UserDTO toDto() {
        // 날짜 변환 로직 (String -> LocalDate)
        LocalDate parsedBirthDate = null;
        if (this.birthDateString != null && !this.birthDateString.isEmpty()) {
            try {
                // 예: "YYYY-MM-DD" 형식 또는 Google People API의 Birthday 객체에서 String으로 변환된 형식
                parsedBirthDate = LocalDate.parse(this.birthDateString);
            } catch (java.time.format.DateTimeParseException e) {
                log.warn("생년월일 형식 파싱 실패: {}", this.birthDateString);
                // 다른 형식의 텍스트 생년월일 처리 또는 null 유지
            }
        }

        // 성별 변환 로직 (String -> Character)
        Character parsedGender = null;
        if (this.genderString != null && !this.genderString.isEmpty()) {
            String lowerCaseGender = this.genderString.toLowerCase();
            if ("male".equals(lowerCaseGender)) {
                parsedGender = 'M';
            } else if ("female".equals(lowerCaseGender)) {
                parsedGender = 'F';
            } else {
                parsedGender = 'U'; // Unspecified 또는 기타
            }
        }

        return UserDTO.builder()
                .userId(this.userId) // Google의 'sub'
                .userName(userName)
                .nickname(userName) // 닉네임은 이름과 동일하게 초기 설정
                .email(email)
                .profileName(profileName) // profileName으로 변경
                .provider(registrationId.toUpperCase()) // "GOOGLE"
                .userStatus("ACTIVE") // 기본 상태 활성
                .signupDate(LocalDate.now()) // 가입일은 현재 날짜로 설정
                .lastLoginAt(LocalDate.now()) // 마지막 로그인도 현재 날짜
                .updateAt(LocalDate.now()) // 업데이트 날짜도 현재 날짜
                // People API로 가져온 추가 정보는 CustomOAuth2UserService에서 직접 설정할 예정이므로 여기서는 Builder로 직접 주입
                .phone(phone)         // phone으로 변경
                .gender(parsedGender) // Character 타입으로 변환 후 주입
                .birthDate(parsedBirthDate) // LocalDate 타입으로 변환 후 주입
                .build();
    }
}