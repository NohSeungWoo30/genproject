package generationgap.co.kr.security;

import generationgap.co.kr.domain.user.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable; // 이 임포트는 현재 코드에서 직접 사용되지 않으므로 제거 가능 (UserDTO에 이미 Serializable 구현)
import java.time.LocalDate; // 이 임포트도 현재 클래스에서 직접 사용되지 않으므로 제거 가능
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private UserDTO userDTO;
    private Map<String, Object> oauth2Attributes;
    private List<GrantedAuthority> authorities;

    // 1. 일반 로그인 사용자를 위한 생성자 (UserDetailsService에서 사용)
    public CustomUserDetails(UserDTO userDTO) {
        // ⭐ 중요: userDTO가 null이거나 핵심 식별자(userId)가 null/empty인지 여기서 검증
        if (userDTO == null || userDTO.getUserId() == null || userDTO.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("UserDTO or userDTO.userId cannot be null or empty for CustomUserDetails.");
        }
        this.userDTO = userDTO;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 2. OAuth2 로그인 사용자를 위한 생성자 (CustomOAuth2UserService에서 사용)
    public CustomUserDetails(UserDTO userDTO, Map<String, Object> oauth2Attributes) {
        // ⭐ 중요: userDTO가 null이거나 핵심 식별자(userId)가 null/empty인지 여기서 검증
        if (userDTO == null || userDTO.getUserId() == null || userDTO.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("UserDTO or userDTO.userId cannot be null or empty for CustomUserDetails (OAuth2).");
        }
        this.userDTO = userDTO;
        this.oauth2Attributes = oauth2Attributes;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }


    // --- UserDetails 인터페이스 구현 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return userDTO.getPasswordHash(); // 일반 로그인 시 필요
    }

    @Override
    public String getUsername() {
        // ⭐ 핵심: 이 값이 Spring Security의 Principal Name이 됩니다.
        // userDTO.getUserId()가 null이거나 비어있으면 "principalName cannot be empty" 오류 발생.
        // 생성자에서 이미 검증했으니 여기서는 그대로 반환.
        return userDTO.getUserId();
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

    // --- OAuth2User 인터페이스 구현 ---
    @Override
    public Map<String, Object> getAttributes() {
        return this.oauth2Attributes;
    }

    @Override
    public String getName() {
        // ⭐ 핵심: 이 값이 OAuth2User 인터페이스의 'name' 속성으로 사용됩니다.
        // Spring Security는 보통 이 값을 Authentication.getName()으로 사용합니다.
        // getUsername()과 동일한 값을 반환하는 것이 일관적이고 일반적입니다.
        return getUsername(); // userDTO.getUserId()와 동일
        // 만약 principal.attributes['name']을 사용하고 싶다면,
        // return (String) oauth2Attributes.get("name");
        // 이 경우 oauth2Attributes에 'name' 키가 없을 때 null 반환 가능성이 있습니다.
        // 안전하게 getUsername()을 따르는 것이 좋습니다.
    }

    // --- UserDTO의 추가 정보를 얻기 위한 메서드들 ---
    public Long getUserIdx() { return userDTO.getUserIdx(); }
    public String getProvider() { return userDTO.getProvider(); }
    public String getUserName() { return userDTO.getUserName(); }
    public String getNickname() { return userDTO.getNickname(); }
    public LocalDate getBirthDate() { return userDTO.getBirthDate(); }
    public Character getGender() { return userDTO.getGender(); }
    public String getEmail() { return userDTO.getEmail(); }
    public String getPhone() { return userDTO.getPhone(); }
    public String getProfileName() { return userDTO.getProfileName(); } // UserDTO에 이 필드가 있다면 유지
    public String getIntroduction() { return userDTO.getIntroduction(); }
    public String getUserStatus() { return userDTO.getUserStatus(); }
    public UserDTO getUserDTO() { return this.userDTO; }
}