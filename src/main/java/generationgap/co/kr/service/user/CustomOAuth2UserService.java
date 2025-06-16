package generationgap.co.kr.service.user; // 패키지 경로 일치 확인

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.security.CustomUserDetails; // CustomUserDetails 임포트 확인
import generationgap.co.kr.domain.oauth.OAuthAttributes;
import generationgap.co.kr.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- 비활성화
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User; // OAuth2User 임포트 유지
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections; // <-- 비활성화
import java.util.Map; // <-- 비활성화

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2UserService delegate = new DefaultOAuth2UserService(); // 제네릭 타입 명시 권장
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService(); // <--컴파일 오류
        OAuth2User oauth2User = delegate.loadUser(userRequest); // 여기서 oauth2User는 DefaultOAuth2User 타입

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName(); // "sub"

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oauth2User.getAttributes());

        UserDTO user = saveOrUpdate(attributes); // DB에 저장/업데이트된 UserDTO 객체 반환

        // *** 이 부분이 핵심적인 수정입니다. ***
        // DefaultOAuth2User 대신 우리가 정의한 CustomUserDetails 객체를 반환합니다.
        // CustomUserDetails 생성자에 UserDTO와 OAuth2 속성을 함께 넘겨줍니다.
        // 권한은 CustomUserDetails 생성자 내부에서 처리되므로 SimpleGrantedAuthority는 직접 넘기지 않습니다.
        return new CustomUserDetails(user, attributes.getAttributes()); // UserDTO와 OAuth2Attributes 전달
    }

    // 사용자 정보를 DB에 저장하거나 업데이트하는 메서드
    @Transactional
    private UserDTO saveOrUpdate(OAuthAttributes attributes) {
        String providedUserId = (String) attributes.getAttributes().get(attributes.getNameAttributeKey());

        // findByProviderAndUserId 메서드 확인 (UserMapper.xml에 쿼리 있어야 함)
        UserDTO existingUser = userMapper.findByProviderAndUserId(attributes.getRegistrationId().toUpperCase(), providedUserId);

        if (existingUser == null) {
            // 새로운 사용자
            UserDTO newUser = attributes.toDto(); // OAuthAttributes에서 UserDTO로 변환
            userMapper.insertUser(newUser); // Mybatis insertUser 메서드 호출
            log.info("새로운 Google 사용자 등록: userId={}, email={}", newUser.getUserId(), newUser.getEmail());
            return newUser;
        } else {
            // 기존 사용자: 정보 업데이트
            existingUser.setUserName(attributes.getName());
            existingUser.setEmail(attributes.getEmail());
            existingUser.setNickname(attributes.getName()); // 닉네임 업데이트
            // existingUser.setProfileName(attributes.getName()); // profileName이 있다면
            existingUser.setLastLoginAt(LocalDate.now());
            existingUser.setUpdateAt(LocalDate.now());

            userMapper.updateUserInfo(existingUser); // userMapper.updateUserUserInfo(existingUser)가 아니라 userMapper.updateUser(existingUser)가 맞는지 확인
            log.info("기존 Google 사용자 정보 업데이트: userId={}, email={}", existingUser.getUserId(), existingUser.getEmail());
            return existingUser;
        }
    }
}