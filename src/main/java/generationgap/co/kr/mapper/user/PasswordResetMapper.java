package generationgap.co.kr.mapper.user;

import generationgap.co.kr.domain.user.PasswordReset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PasswordResetMapper {

    // 토큰 정보 저장
    void insertPasswordReset(PasswordReset passwordReset);

    // 토큰으로 정보 조회
    PasswordReset findByToken(@Param("token") String token);

    // 토큰 사용 처리 (used_at 업데이트)
    void updateUsedAt(@Param("token") String token, @Param("usedAt") LocalDateTime usedAt);

    // user_idx로 사용되지 않은 유효한 토큰이 있는지 확인 (중복 요청 방지)
    PasswordReset findValidTokenByUserIdx(@Param("userIdx") Long userIdx, @Param("now") LocalDateTime now);

    // 특정 user_idx의 모든 토큰을 무효화 (예: 비밀번호 변경 시 기존 토큰 무효화)
    void invalidateAllTokensForUser(@Param("userIdx") Long userIdx, @Param("now") LocalDateTime now);
}