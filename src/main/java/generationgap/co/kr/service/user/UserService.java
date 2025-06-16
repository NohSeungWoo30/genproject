package generationgap.co.kr.service.user;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder; // *** 이 부분 추가 ***

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired // *** 이 부분 추가 (PasswordEncoder 주입) ***
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(UserDTO user) {

        // --- 비밀번호 해싱 로직 추가 ---
        // 사용자가 폼에 입력한 비밀번호(user.getPassword())를 해싱하여
        // 데이터베이스에 저장할 passwordHash 필드에 설정합니다.
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPasswordHash(hashedPassword);

        // --- provider 필드에 기본값 설정 추가 ---
        // 폼에서 넘어온 값이 null이거나 비어있을 경우 "LOCAL"로 설정
        // (readonly로 설정했더라도, 빈 DTO가 넘어가면 null일 수 있음)
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            user.setProvider("LOCAL");
        }
        // ---

        // profileName 기본값 설정 (이전 오류 해결을 위해 추가했던 부분)
        if (user.getProfileName() == null || user.getProfileName().isEmpty()) {
            user.setProfileName("default_profile.jpg"); // 또는 "" (DB NOT NULL 허용 시)
        }

        // signupDate는 DB DEFAULT SYSDATE가 있으므로 여기서 설정 안 해도 되지만,
        // 필요하다면 DTO에도 채워넣을 수 있습니다.
        // if (user.getSignupDate() == null) {
        //     user.setSignupDate(LocalDateTime.now());
        // }

        userMapper.insertUser(user);
    }

}
