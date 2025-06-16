package generationgap.co.kr.controller.user;

import generationgap.co.kr.domain.user.UserDTO; // UserDTO 임포트
import generationgap.co.kr.mapper.user.UserMapper; // UserMapper 임포트
import generationgap.co.kr.service.user.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private UserMapper userMapper; // UserMapper 주입

    // 비밀번호 찾기 페이지 보여주기
    @GetMapping("/user/forgot-password")
    public String showForgotPasswordForm() {
        return "user/forgot-password";
    }

    // 비밀번호 재설정 링크 요청 처리
    @PostMapping("/user/forgot-password-request")
    public String requestPasswordReset(@RequestParam("userId") String userId, Model model) {
        log.info("비밀번호 재설정 요청 받음: userId={}", userId);

        // 1. userId로 UserDTO 조회하여 user_idx 얻기
        UserDTO user = userMapper.findByUserId(userId); // 기존 userId로 조회하는 메서드 사용
        if (user == null) {
            model.addAttribute("message", "사용자 ID를 찾을 수 없습니다. ID를 확인해주세요.");
            model.addAttribute("success", false);
            log.warn("비밀번호 재설정 요청 처리 실패: 사용자 {}를 찾을 수 없습니다.", userId);
            return "user/forgot-password";
        }

        // 이메일이 없는 경우도 처리
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            model.addAttribute("message", "해당 사용자에게 등록된 이메일이 없습니다.");
            model.addAttribute("success", false);
            log.warn("비밀번호 재설정 요청 처리 실패: 사용자 {}의 이메일이 없습니다.", userId);
            return "user/forgot-password";
        }


        // 2. user_idx로 비밀번호 재설정 토큰 생성 및 이메일 발송 서비스 호출
        boolean success = passwordResetService.createPasswordResetToken(user.getUserIdx()); // userIdx 전달

        if (success) {
            model.addAttribute("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다. 이메일을 확인해주세요.");
            model.addAttribute("success", true);
            log.info("비밀번호 재설정 요청 처리 성공: userId={}", userId);
        } else {
            model.addAttribute("message", "이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("success", false);
            log.error("비밀번호 재설정 이메일 전송 실패: userId={}", userId);
        }
        return "user/forgot-password";
    }

    // ... (resetPassword, processPasswordReset, showResetPasswordError 메서드는 변경 없음) ...
    @GetMapping("/user/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (token == null || token.isEmpty()) {
            return "redirect:/user/reset_password_error";
        }
        model.addAttribute("token", token);
        return "user/reset-password";
    }

    @PostMapping("/user/reset-password-process")
    public String processPasswordReset(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("token", token);
            return "user/reset-password";
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);

        if (success) {
            model.addAttribute("message", "비밀번호가 성공적으로 재설정되었습니다.");
            log.info("비밀번호 재설정 프로세스 성공: token={}", token);
            return "user/login";
        } else {
            model.addAttribute("message", "비밀번호 재설정에 실패했습니다. 토큰이 유효하지 않거나 만료되었거나 이미 사용되었을 수 있습니다.");
            log.warn("비밀번호 재설정 프로세스 실패: token={}", token);
            return "user/reset_password_error";
        }
    }

    @GetMapping("/user/reset_password_error")
    public String showResetPasswordError() {
        return "user/reset_password_error";
    }
}