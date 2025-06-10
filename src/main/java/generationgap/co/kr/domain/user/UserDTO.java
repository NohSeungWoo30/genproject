package generationgap.co.kr.domain.user;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate; // 추가

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L; // ⭐ serialVersionUID 추가 (권장)

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
    private String userStatus; // 소프트 삭제 상태 관리: 'ACTIVE', 'DELETED'
    private LocalDate lastLoginAt;
    private LocalDate updateAt;
    private LocalDate ghost; // 삭제 일시 기록용 (deletedAt의 의미로 사용)
}