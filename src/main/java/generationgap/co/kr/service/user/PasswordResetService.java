package generationgap.co.kr.service.user;

import generationgap.co.kr.domain.user.PasswordReset;
import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.user.PasswordResetMapper;
import generationgap.co.kr.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리를 위해 추가

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserMapper userMapper; // 사용자 정보 조회 및 업데이트용
    @Autowired
    private PasswordResetMapper passwordResetMapper; // 토큰 정보 관리용
    @Autowired
    private PasswordEncoder passwordEncoder; // 비밀번호 해싱용

    // 1. 비밀번호 재설정 요청 처리 (이메일/ID로 사용자 찾기 및 토큰 생성)
    @Transactional // 하나의 트랜잭션으로 처리 (토큰 생성/저장, 기존 토큰 무효화)
    public String createPasswordResetToken(String userId) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) {
            // 사용자 ID를 찾을 수 없을 경우
            // 실제 구현에서는 사용자에게 '해당 이메일로 가입된 계정을 찾을 수 없습니다' 등으로 응답
            return null;
        }

        // 기존에 유효한 재설정 토큰이 있다면 모두 무효화
        passwordResetMapper.invalidateAllTokensForUser(user.getUserIdx(), LocalDateTime.now());

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        // 토큰 만료 시간 설정 (예: 24시간 후)
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserIdx(user.getUserIdx());
        passwordReset.setToken(token);
        passwordReset.setExpiresAt(expiresAt);

        passwordResetMapper.insertPasswordReset(passwordReset);

        // 이메일 발송 로직은 여기에 추가 (예: mailService.sendResetEmail(user.getEmail(), token))
        // 지금은 토큰 자체를 반환하여 테스트에 용이하게 합니다.
        return token;
    }

    // 2. 토큰 유효성 검사
    public PasswordReset validatePasswordResetToken(String token) {
        PasswordReset passwordReset = passwordResetMapper.findByToken(token);

        if (passwordReset == null) {
            // 토큰이 존재하지 않음
            return null;
        }

        // 토큰이 이미 사용되었거나 만료된 경우
        if (passwordReset.getUsedAt() != null || passwordReset.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null; // 무효한 토큰
        }

        return passwordReset; // 유효한 토큰
    }

    // 3. 비밀번호 재설정 (실제 비밀번호 업데이트)
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        PasswordReset passwordReset = validatePasswordResetToken(token);

        if (passwordReset == null) {
            return false; // 유효하지 않은 토큰
        }

        // 사용자 비밀번호 업데이트
        String encodedPassword = passwordEncoder.encode(newPassword);
        UserDTO userToUpdate = new UserDTO();
        userToUpdate.setUserIdx(passwordReset.getUserIdx()); // user_idx로 업데이트
        userToUpdate.setPasswordHash(encodedPassword);

        userMapper.updateUserPassword(userToUpdate); // UserMapper에 이 메서드 추가 필요

        // 토큰 사용 처리 (used_at 업데이트)
        passwordResetMapper.updateUsedAt(token, LocalDateTime.now());

        return true;
    }
}