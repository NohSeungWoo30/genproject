package generationgap.co.kr.security;

import generationgap.co.kr.domain.user.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate; // LocalDate 임포트
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private UserDTO userDTO; // 실제 사용자 정보를 담을 UserDTO 객체

    public CustomUserDetails(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 "ROLE_USER"만 부여하지만, 필요에 따라 UserDTO에 role 컬럼 추가 후 변경 가능
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return userDTO.getPasswordHash(); // 비밀번호 해시값 반환
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
    public boolean isEnabled() { return true; }

    // --- UserDTO의 추가 정보를 얻기 위한 메서드들 ---
    // UserDTO에서 필요한 모든 정보들을 직접 가져올 수 있도록 Getter 추가
    public Long getUserIdx() {
        return userDTO.getUserIdx();
    }

    public String getProvider() {
        return userDTO.getProvider();
    }

    public String getUserName() {
        return userDTO.getUserName();
    }

    public String getNickname() {
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

    public String getProfileName() {
        return userDTO.getProfileName();
    }

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