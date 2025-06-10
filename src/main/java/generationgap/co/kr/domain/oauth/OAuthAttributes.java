package generationgap.co.kr.domain.oauth;

import generationgap.co.kr.domain.user.UserDTO; // UserDTO import
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate; // UserDTO의 signupDate, lastLoginAt, updateAt, ghost가 LocalDate 타입이므로 필요
import java.util.Map;

@Getter
@Builder
@Slf4j
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey; // 'sub' (구글의 경우)
    private String name;             // 구글 프로필 'name'
    private String email;            // 구글 프로필 'email'
    private String picture;          // 구글 프로필 'picture' URL (UserDTO에 직접 매핑은 안 할 것)

    private String registrationId; // 추가: 어떤 OAuth 제공자인지 (google)

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        // 다른 소셜 로그인 추가 시 여기에 else if
        else {
            throw new IllegalArgumentException("Unsupported registrationId: " + registrationId);
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        log.info("Google OAuth attributes: {}", attributes);
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName) // 'sub'
                .registrationId("google") // 등록 ID 명시
                .build();
    }

    // UserDTO로 변환하는 메서드
    public UserDTO toDto() {
        UserDTO user = new UserDTO();
        // userIdx는 DB에서 자동 생성되므로 설정하지 않음
        user.setUserId((String) this.attributes.get(this.nameAttributeKey)); // Google의 'sub' 값을 userId에 저장
        user.setProvider(this.registrationId.toUpperCase()); // 제공자 이름 (예: "GOOGLE")
        user.setUserName(this.name); // 구글 계정의 이름
        user.setNickname(this.name); // 닉네임을 구글 이름으로 설정할 수도 있음. 중복 처리 필요
        user.setEmail(this.email); // 구글 이메일
        // user.setPhone(null); // 소셜 로그인 시 전화번호는 보통 제공되지 않음
        // user.setUserCi(null); // 소셜 로그인 시 CI는 보통 제공되지 않음
        user.setPasswordHash("oauth_password"); // OAuth2 사용자는 비밀번호가 필요 없으므로 더미 값 (null 허용하지 않는 경우)
        user.setBirthDate(LocalDate.parse("1997-06-08")); // 생년월일도 제공되지 않음(임시 더미 값)
        user.setGender('G');// 성별도 제공되지 않음(더미 값)
        user.setIntroduction("안녕하세요! Google 계정으로 가입한 사용자입니다.");
        user.setSignupDate(LocalDate.now()); // 가입일 현재 날짜
        user.setUserStatus("ACTIVE"); // 초기 상태는 'ACTIVE'
        user.setLastLoginAt(LocalDate.now()); // 최초 로그인 날짜
        user.setUpdateAt(LocalDate.now()); // 업데이트 날짜
        user.setGhost(null); // 삭제된 계정이 아니므로 null

        return user;
    }
}