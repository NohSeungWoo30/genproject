package generationgap.co.kr.domain.user;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userIdx;
    private String userId;
    private String provider;
    private String userName;
    private String nickname;
    private LocalDate birthDate;
    private Character gender;
    private String userCi;
    private String password;
    private String passwordHash;
    private String email;
    private String phone;
    private String profileName;       // DB에 저장된 파일명 또는 URL 경로
    private String introduction;
    private LocalDate signupDate;
    private String userStatus;
    private LocalDate lastLoginAt;
    private LocalDate updateAt;
    private LocalDate ghost;
    private int isSuspended;
    private LocalDateTime suspendUntil;

    /** 추천 수 */
    private int trust;
    /** 불참(노쇼) 횟수 */
    private int noShowCount;

    /**
     * Thymeleaf에서 ${user.profileImageUrl} 로 프로필 이미지를 읽어올 수 있도록,
     * 기존 profileName 필드를 반환하는 alias getter
     */
    public String getProfileImageUrl() {
        return this.profileName;
    }
}
