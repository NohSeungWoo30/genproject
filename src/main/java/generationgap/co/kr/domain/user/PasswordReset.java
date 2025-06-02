package generationgap.co.kr.domain.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PasswordReset {
    private Long resetIdx;        // reset_idx (NUMBER)
    private Long userIdx;         // user_idx (NUMBER)
    private String token;         // token (VARCHAR2)
    private LocalDateTime expiresAt; // expires_at (DATE -> LocalDateTime)
    private LocalDateTime usedAt;    // used_at (DATE -> LocalDateTime)

    // 편의 메서드: 토큰이 유효한지 확인
    public boolean isValid() {
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now()) && usedAt == null;
    }
}