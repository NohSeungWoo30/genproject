package generationgap.co.kr.service.user;

import generationgap.co.kr.domain.user.PasswordReset;
import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.user.PasswordResetMapper;
import generationgap.co.kr.mapper.user.UserMapper;
import generationgap.co.kr.service.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private PasswordResetMapper passwordResetMapper;
    @Autowired
    private UserMapper userMapper; // UserMapper는 user_idx로 조회하는 메서드가 필요합니다.
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    // createPasswordResetToken 메서드 수정: userId 대신 user_idx를 인자로 받습니다.
    @Transactional
    public boolean createPasswordResetToken(Long userIdx) { // Long userIdx로 변경
        // UserMapper에 findByUserIdx 메서드가 필요합니다.
        UserDTO user = userMapper.findByUserIdx(userIdx); // userIdx로 조회
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            log.warn("비밀번호 재설정 요청 실패: 사용자 (idx: {})를 찾을 수 없거나 이메일이 없습니다.", userIdx);
            return false;
        }

        // 1. 기존 토큰 무효화 (논리적 삭제: used_at 업데이트)
        passwordResetMapper.invalidateAllTokensForUserIdx(userIdx, LocalDateTime.now()); // userIdx로 변경
        log.info("사용자 (idx: {})의 모든 기존 재설정 토큰을 무효화했습니다.", userIdx);

        // 중복 요청 방지 로직 추가 (findValidUnusedTokenByUserIdx 활용)
        PasswordReset existingToken = passwordResetMapper.findValidUnusedTokenByUserIdx(userIdx, LocalDateTime.now());
        if (existingToken != null) {
            log.warn("사용자 (idx: {})에게 이미 유효한 재설정 토큰이 존재합니다. 새 토큰 생성을 중단합니다.", userIdx);
            return false; // 또는 다른 처리 (예: 기존 토큰 정보 반환)
        }

        // 2. 새 토큰 생성 및 DB 저장
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // 24시간 유효

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserIdx(userIdx); // userIdx로 변경
        passwordReset.setToken(token);
        passwordReset.setExpiresAt(expiresAt);

        passwordResetMapper.insertToken(passwordReset); // 토큰 저장
        log.info("비밀번호 재설정 토큰 생성 완료: userIdx={}, token={}", userIdx, token);

        // 3. 이메일 발송 (HTML 로 하려면 수정 필요함)
        String resetLink = appBaseUrl + "/user/reset-password?token=" + token;
        String subject = "[GenerationGap] 비밀번호 재설정 안내";
        String content = String.format(
                "안녕하세요, %s님.\n\n" +
                        "귀하의 비밀번호 재설정 요청이 접수되었습니다. 아래 링크를 클릭하여 비밀번호를 재설정해주세요.\n\n" +
                        "링크: %s\n\n" +
                        "이 링크는 24시간 동안 유효합니다. 만약 본인이 요청한 것이 아니라면, 이 이메일을 무시해주세요.\n\n" +
                        "감사합니다.\n" +
                        "GenerationGap 팀 드림",
                user.getNickname() != null ? user.getNickname() : user.getUserName(),
                resetLink
        );

        try {
            emailService.sendEmail(user.getEmail(), subject, content);
            log.info("비밀번호 재설정 이메일 전송 완료: To={}, Link={}", user.getEmail(), resetLink);
            return true;
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 전송 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    // resetPassword 메서드 수정: PasswordReset 객체에서 userIdx를 가져옵니다.
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        PasswordReset passwordReset = passwordResetMapper.findByToken(token);

        // 1. 토큰 유효성 검사
        if (passwordReset == null || !passwordReset.isValid()) {
            log.warn("비밀번호 재설정 실패: 유효하지 않거나 만료되었거나 이미 사용된 토큰입니다. Token: {}", token);
            return false;
        }

        // 2. 토큰에 연결된 사용자 정보 조회
        // user_idx로 사용자를 조회
        UserDTO user = userMapper.findByUserIdx(passwordReset.getUserIdx()); // userIdx로 변경
        if (user == null) {
            log.warn("비밀번호 재설정 실패: 토큰에 연결된 사용자를 찾을 수 없습니다. userIdx: {}", passwordReset.getUserIdx());
            return false;
        }

        // 3. 새 비밀번호 해싱 및 사용자 비밀번호 업데이트
        String newHashedPassword = passwordEncoder.encode(newPassword);
        userMapper.updateUserPassword(user.getUserIdx(), newHashedPassword); // UserMapper의 updateUserPassword 시그니처에 맞춰 호출

        // 4. 토큰 사용 시간 기록 (used_at 업데이트)
        passwordResetMapper.updateTokenUsedAt(token, LocalDateTime.now());
        log.info("비밀번호 재설정 성공: userIdx={}, token={} 사용 완료", user.getUserIdx(), token); // userIdx로 변경
        return true;
    }
}