package generationgap.co.kr.service.oauth;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.domain.oauth.OAuthAttributes;
import generationgap.co.kr.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

// Google People API 관련 임포트 (JacksonFactory 오류 해결을 위해)
// ⭐ 중요: com.google.api.client.json.jackson2.JacksonFactory;
//       이 임포트가 오류가 난다면, build.gradle에 Jackson 라이브러리 추가가 필요할 수 있습니다.
//       일반적으로 google-api-client 의존성에 포함되어 있어야 하지만,
//       만약 문제가 지속된다면 아래를 추가해 보세요:
//       implementation 'com.fasterxml.jackson.core:jackson-databind'
//       implementation 'com.fasterxml.jackson.core:jackson-core'
//       implementation 'com.fasterxml.jackson.core:jackson-annotations'
//       그리고 Gradle 새로고침을 다시 해주세요.
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory; // 이 임포트가 오류나면 위 주석 참고
import com.google.api.services.people.v1.PeopleService;       // 이 임포트가 오류나면 build.gradle 확인
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Birthday;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate; // LocalDate 임포트
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("✅ CustomOAuth2UserService.loadUser() 진입 확인");
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName(); // 보통 'sub'

        // 1. 기본 OAuthAttributes 생성
        // ⭐ 중요: userId는 Google의 'sub' 값을 사용하도록 명확히 지정
        OAuthAttributes.OAuthAttributesBuilder attributesBuilder = OAuthAttributes.builder()
                .registrationId(registrationId) // registrationId도 빌더에 추가
                .nameAttributeKey(userNameAttributeName) // 'sub'를 가리키는 키
                .attributes(oauth2User.getAttributes()) // 원본 속성 맵 전체
                // ⭐ 핵심 수정: userId 필드를 oauth2User.getAttribute("sub")로 명시적으로 설정
                .userId((String) oauth2User.getAttribute("sub")) // Google의 고유 식별자 'sub' 사용
                .userName((String) oauth2User.getAttribute("name")) // Google의 사용자 이름 (본명)
                .email((String) oauth2User.getAttribute("email"))   // Google 이메일
                .profileName((String) oauth2User.getAttribute("picture")); // 프로필 이미지 URL



        // 2. Google People API를 통해 추가 정보 가져오기
        String phoneFromApi = null;        // People API에서 가져온 전화번호 (String)
        String genderStringFromApi = null; // People API에서 가져온 성별 (String)
        String birthdayStringFromApi = null; // People API에서 가져온 생년월일 (String)

        if ("google".equals(registrationId)) {
            try {
                GoogleCredential credential = new GoogleCredential()
                        .setAccessToken(userRequest.getAccessToken().getTokenValue());

                PeopleService peopleService = new PeopleService.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential)
                        .setApplicationName("generationgap-web-app") // 애플리케이션 이름 (GCP 프로젝트 ID 등)
                        .build();

                Person person = peopleService.people().get("people/me")
                        .setPersonFields("names,emailAddresses,photos,phoneNumbers,genders,birthdays")
                        .execute();

                // 전화번호 추출 로직
                if (person.getPhoneNumbers() != null && !person.getPhoneNumbers().isEmpty()) {
                    Optional<PhoneNumber> primaryPhoneNumber = person.getPhoneNumbers().stream()
                            .filter(pn -> pn.getMetadata() != null && pn.getMetadata().getPrimary() != null && pn.getMetadata().getPrimary())
                            .findFirst();
                    phoneFromApi = primaryPhoneNumber.map(PhoneNumber::getValue)
                            .orElse(person.getPhoneNumbers().get(0).getValue());
                }

                // 성별 추출 로직
                if (person.getGenders() != null && !person.getGenders().isEmpty()) {
                    genderStringFromApi = person.getGenders().get(0).getValue(); // "male", "female", "unspecified" 등
                }

                // 생년월일 추출 로직
                if (person.getBirthdays() != null && !person.getBirthdays().isEmpty()) {
                    Birthday birth = person.getBirthdays().get(0);
                    if (birth.getDate() != null) {
                        // 년, 월, 일 정보가 있다면 "YYYY-MM-DD" 형식으로 조합하여 String으로 저장
                        birthdayStringFromApi = String.format("%04d-%02d-%02d",
                                birth.getDate().getYear() != null ? birth.getDate().getYear() : 0,
                                birth.getDate().getMonth() != null ? birth.getDate().getMonth() : 0,
                                birth.getDate().getDay() != null ? birth.getDate().getDay() : 0);
                    } else if (birth.getText() != null) {
                        birthdayStringFromApi = birth.getText(); // 텍스트 형태의 생년월일 (예: "1월 1일")
                    }
                }

                // People API에서 가져온 추가 정보를 attributesBuilder에 추가
                attributesBuilder
                        .phone(phoneFromApi)
                        .genderString(genderStringFromApi)
                        .birthDateString(birthdayStringFromApi);

            } catch (GeneralSecurityException | IOException e) {
                log.warn("⚠️ Google People API에서 추가 사용자 정보를 가져오는데 실패했습니다: {}", e.getMessage());
                // 해당 정보는 필수가 아니므로 오류를 던지기보다 로그만 남기고 진행.
            }
        }

        // 최종 OAuthAttributes 객체 생성
        OAuthAttributes finalAttributes = attributesBuilder.build();
        log.info("ℹ️ OAuthAttributes 생성 완료: {}", finalAttributes);

        // 3. UserDTO 생성 및 추가 정보 설정
        // OAuthAttributes의 toDto() 메서드에서 최종적으로 UserDTO 타입에 맞게 변환됩니다.
        UserDTO user = finalAttributes.toDto();
        log.info("ℹ️ 최종 UserDTO 준비 완료: {}", user.getEmail());

        try {
            UserDTO savedUser = userService.saveOrUpdateOAuthUser(user); // DB에 저장/업데이트 시도
            log.info("🎉 DB 저장/업데이트 성공: userIdx={}, email={}", savedUser.getUserIdx(), savedUser.getEmail());
            return new CustomUserDetails(savedUser, oauth2User.getAttributes());
        } catch (Exception e) {
            log.error("❌ DB 저장/업데이트 실패: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException(new OAuth2Error("db_save_error", "Failed to save or update OAuth2 user in DB.", null), e);
        }
    }
}