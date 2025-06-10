package generationgap.co.kr.security; // 이 패키지 경로가 맞다면 그대로 사용

import generationgap.co.kr.domain.user.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User; // OAuth2User 임포트 추가

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List; // List 임포트 추가
import java.util.Map;   // Map 임포트 추가

// UserDetails와 OAuth2User 인터페이스를 모두 구현
public class CustomUserDetails implements UserDetails, OAuth2User, Serializable {

    private UserDTO userDTO; // 실제 사용자 정보를 담을 UserDTO 객체
    private Map<String, Object> oauth2Attributes; // OAuth2 제공자가 제공한 원본 속성 (OAuth2User 구현용)
    private List<GrantedAuthority> authorities; // 권한 (생성자에서 초기화)


    // 1. 일반 로그인 사용자를 위한 생성자 (UserDetailsService에서 사용)
    public CustomUserDetails(UserDTO userDTO) {
        this.userDTO = userDTO;
        // UserDTO의 userStatus 등에 따라 권한을 동적으로 부여할 수도 있습니다.
        // 현재는 모든 사용자에게 기본 "ROLE_USER" 부여
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 2. OAuth2 로그인 사용자를 위한 생성자 (CustomOAuth2UserService에서 사용)
    public CustomUserDetails(UserDTO userDTO, Map<String, Object> oauth2Attributes) {
        this.userDTO = userDTO;
        this.oauth2Attributes = oauth2Attributes;
        // OAuth2 사용자의 권한도 "ROLE_USER"로 부여 (필요 시 역할 로직 추가 가능)
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }


    // --- UserDetails 인터페이스 구현 (기존과 동일) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return userDTO.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return userDTO.getUserId(); // 사용자 ID를 Spring Security의 "username"으로 사용
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() {
        // UserDTO의 userStatus가 "ACTIVE"일 때만 계정을 활성화 (예시)
        return userDTO.getUserStatus() != null && userDTO.getUserStatus().equals("ACTIVE");
    }

    // --- OAuth2User 인터페이스 구현 (새롭게 추가) ---
    @Override
    public Map<String, Object> getAttributes() {
        // OAuth2 제공자가 제공한 원본 속성 맵을 반환
        return this.oauth2Attributes;
    }

    @Override
    public String getName() {
        // OAuth2User의 'name'은 주로 사용자의 고유 식별자 또는 이름을 반환합니다.
        // 여기서는 UserDTO의 userId를 반환하거나, attributes에서 'name'을 반환할 수 있습니다.
        // main.html에서 principal.attributes['name']을 사용하므로,
        // 여기서는 userDTO의 userId (Google의 sub 값)를 반환하는 것이 일관적입니다.
        return userDTO.getUserId();
        // 또는, Google 계정 이름에 더 가깝게
        // return (String) oauth2Attributes.get("name");
    }

    // --- UserDTO의 추가 정보를 얻기 위한 메서드들 (기존과 동일) ---
    // UserDTO에서 필요한 모든 정보들을 직접 가져올 수 있도록 Getter 추가
    public Long getUserIdx() {
        return userDTO.getUserIdx();
    }

    public String getProvider() {
        return userDTO.getProvider();
    }

    public String getUserName() { // UserDTO의 userName 필드
        return userDTO.getUserName();
    }

    public String getNickname() { // UserDTO의 nickname 필드
        return userDTO.getNickname();
    }

    public LocalDate getBirthDate() {
        return userDTO.getBirthDate();
    }

    public Character getGender() {
        return userDTO.getGender();
    }

    public String getEmail() {
        return userDTO.getEmail();
    }

    public String getPhone() {
        return userDTO.getPhone();
    }

    // UserDTO에 profileName 필드가 없다면 이 메서드는 제거하거나,
    // 필요에 따라 UserDTO에 필드를 추가해야 합니다.
    // public String getProfileName() {
    //     return userDTO.getProfileName();
    // }

    public String getIntroduction() {
        return userDTO.getIntroduction();
    }

    public String getUserStatus() {
        return userDTO.getUserStatus();
    }

    // 필요하다면 UserDTO 자체를 반환하는 메서드 (모든 정보를 한 번에 받을 때 유용)
    public UserDTO getUserDTO() {
        return this.userDTO;
    }
}