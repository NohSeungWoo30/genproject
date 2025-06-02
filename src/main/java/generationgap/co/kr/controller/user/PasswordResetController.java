package generationgap.co.kr.controller.user;

import generationgap.co.kr.domain.user.PasswordReset;
import generationgap.co.kr.service.user.PasswordResetService;
import generationgap.co.kr.service.user.UserService; // UserService를 통해 사용자 ID로 email을 찾을 수도 있음
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private UserService userService; // 사용자 정보 (이메일) 조회를 위해 주입 (선택 사항, 필요에 따라)

    // 1. 비밀번호 재설정 요청 페이지 (ID/이메일 입력 폼)
    @GetMapping("/user/forgot-password")
    public String showForgotPasswordForm() {
        return "user/forgot_password"; // user/forgot_password.html
    }

    // 2. 비밀번호 재설정 요청 처리 (토큰 생성 및 이메일 발송 - 실제 이메일 발송은 후순위)
    @PostMapping("/user/forgot-password-request")
    public String processForgotPasswordRequest(@RequestParam("userId") String userId,
                                               RedirectAttributes redirectAttributes) {
        String token = passwordResetService.createPasswordResetToken(userId);

        if (token == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 아이디의 사용자를 찾을 수 없습니다.");
            return "redirect:/user/forgot-password";
        }

        // 실제 이메일 발송 로직은 여기에 추가됩니다. (현재는 콘솔 출력 등으로 대체)
        System.out.println("비밀번호 재설정 링크: http://localhost:8080/user/reset-password?token=" + token);
        redirectAttributes.addFlashAttribute("successMessage", "비밀번호 재설정 링크가 콘솔에 출력되었습니다. (실제로는 이메일로 발송됨)");
        // TODO: 실제 이메일 발송 기능을 구현할 때 주석 해제
        // mailService.sendPasswordResetEmail(user.getEmail(), token);

        return "redirect:/user/forgot-password";
    }

    // 3. 토큰 유효성 검사 및 새 비밀번호 입력 페이지
    @GetMapping("/user/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        PasswordReset passwordReset = passwordResetService.validatePasswordResetToken(token);

        if (passwordReset == null) {
            model.addAttribute("errorMessage", "유효하지 않거나 만료된 재설정 링크입니다.");
            return "user/reset_password_error"; // 에러 페이지
        }

        model.addAttribute("token", token); // 폼에 토큰을 숨겨서 전달
        return "user/reset_password"; // 새 비밀번호 입력 폼
    }

    // 4. 새 비밀번호 재설정 처리
    @PostMapping("/user/reset-password-process")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "redirect:/user/reset-password?token=" + token; // 토큰을 다시 넘겨주어 재시도 가능하게
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 재설정되었습니다. 로그인 해주세요.");
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 재설정에 실패했습니다. 유효하지 않은 링크이거나 이미 사용된 링크입니다.");
            return "redirect:/user/reset-password?token=" + token; // 토큰을 다시 넘겨주어 재시도 가능하게
        }
    }
}