package generationgap.co.kr.domain.user;

import lombok.*;

import java.time.LocalDate; // 추가

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userIdx;
    private String userId;
    private String provider;
    private String userName;
    private String nickname;
    private LocalDate birthDate;
    private Character gender;
    private String userCi;
    // --- 이 부분 추가 ---
    private String password; // 사용자가 폼에 입력하는 원본 비밀번호를 받을 필드
    private String passwordHash; // 데이터베이스에 저장될 해싱된 비밀번호
    private String email;
    private String phone;
    private String profileName;
    private String introduction;
    private LocalDate signupDate;
    private String userStatus;
    private LocalDate lastLoginAt;
    private LocalDate updateAt;
    private LocalDate ghost;
}