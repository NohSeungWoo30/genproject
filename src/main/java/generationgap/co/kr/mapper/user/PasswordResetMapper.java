package generationgap.co.kr.mapper.user;

import generationgap.co.kr.domain.user.PasswordReset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PasswordResetMapper {

    // 새 토큰 정보를 저장 (PasswordReset 객체에는 expiresAt 포함)
    void insertToken(PasswordReset passwordReset);

    // 토큰 문자열로 재설정 정보 조회
    PasswordReset findByToken(@Param("token") String token);

    // 토큰 사용 처리 (used_at 업데이트)
    void updateTokenUsedAt(@Param("token") String token, @Param("usedAt") LocalDateTime usedAt);

    // 특정 userIdx의 모든 토큰을 무효화 (used_at 업데이트)
    void invalidateAllTokensForUserIdx(@Param("userIdx") Long userIdx, @Param("now") LocalDateTime now); // userIdx로 변경

    // 선택 사항: 특정 유저에게 유효하고 사용되지 않은 토큰이 있는지 확인
    PasswordReset findValidUnusedTokenByUserIdx(@Param("userIdx") Long userIdx, @Param("now") LocalDateTime now); // userIdx로 변경
}