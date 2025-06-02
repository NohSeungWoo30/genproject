package generationgap.co.kr.service.user;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.UserMapper; // UserMapper 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections; // Collection.emptyList()를 위해 임포트
import org.slf4j.Logger; // Logger 추가
import org.slf4j.LoggerFactory; // LoggerFactory 추가

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class); // 로거 인스턴스

    @Autowired
    private UserMapper userMapper; // 사용자 정보를 DB에서 가져오기 위해 UserMapper 주입

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 1. userId를 이용하여 DB에서 사용자 정보를 조회합니다.
        UserDTO userDTO = userMapper.findByUserId(userId); // UserMapper에 이 메서드가 필요합니다.

        if (userDTO == null) {
            // 사용자 ID가 DB에 없는 경우 예외를 발생시킵니다.
            throw new UsernameNotFoundException("사용자 ID를 찾을 수 없습니다: " + userId);
        }

        // 2. 조회된 UserDTO 정보를 Spring Security의 UserDetails 객체로 변환합니다.
        // UserDetails는 Spring Security가 사용자 인증 및 권한 부여에 사용하는 핵심 인터페이스입니다.
        // 여기서는 간단하게 UserDTO의 userId와 passwordHash를 사용하여 User 객체를 생성합니다.
        // 권한(Authorities)은 Collections.emptyList()로 비워둡니다. (나중에 권한 관리 시 추가)
        return new User(userDTO.getUserId(), userDTO.getPasswordHash(), Collections.emptyList());
    }
}