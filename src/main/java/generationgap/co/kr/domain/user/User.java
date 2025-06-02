package generationgap.co.kr.domain.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
public class User {
    private Long userIdx; // PRIMARY KEY
    private String userId; // NOT NULL
    private String provider; // NOT NULL
    private String userName; // NOT NULL
    private String nickname;
    private LocalDate birthDate; // NOT NULL (예: 2000-01-01)
    private String gender; // NOT NULL CHECK (gender IN ('M', 'F'))
    private String userCi; // NOT NULL
    private String passwordHash;
    private String email; // NOT NULL
    private String phone; // NOT NULL
    private String profileName;
    private String introduction;
    private LocalDate signupDate; // NOT NULL
    private String userStatus; // NOT NULL
    private LocalDateTime lastLoginAt; // LocalDateTime.of(2023, 10, 26, 9, 0, 0)
    private LocalDateTime updateAt; //
    private LocalDate ghost; //
}