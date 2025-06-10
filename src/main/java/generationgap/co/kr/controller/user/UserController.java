package generationgap.co.kr.controller.user;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.service.user.UserService;
import generationgap.co.kr.security.CustomUserDetails; // CustomUserDetails 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // @AuthenticationPrincipal 임포트
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // RedirectAttributes 임포트
import org.slf4j.Logger; // Logger 임포트
import org.slf4j.LoggerFactory; // LoggerFactory 임포트
import org.springframework.http.HttpStatus; // ResponseEntity 사용을 위해 추가
import org.springframework.http.ResponseEntity; // JSON 응답을 위해 추가
import java.util.Map; // Map.of를 위해 추가
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class); // Slf4j Logger 사용

    @Autowired
    private UserService userService;

    // 회원가입 폼 표시
    @GetMapping("/signup")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "user/signup";
    }

    // 회원가입 폼 제출 처리
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute("user") UserDTO user, RedirectAttributes redirectAttributes) { // ✅ RedirectAttributes 추가
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다. 로그인해주세요!"); // ✅ 성공 메시지 추가
            return "redirect:/user/login";
        } catch (Exception e) {
            log.error("회원가입 실패: userId={}, Error: {}", user.getUserId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 오류가 발생했습니다: " + e.getMessage()); // ✅ 실패 메시지 추가
            // 회원가입 실패 시 다시 폼으로 돌아가되, 입력값 유지를 위해 UserDTO도 함께 넘길 수 있음
            // model.addAttribute("user", user); // 이 방법은 redirectAttributes와 함께 사용하기 어려움
            // return "user/signup";
            return "redirect:/user/signup"; // 간단하게 다시 폼으로 리다이렉트
        }
    }

    // --- 중복 확인 API 추가 시작 ---
    //아이디 중복 확인 API, URL: /user/check/userId?userId=testuser
    @GetMapping("/check/userId") // 세부 경로 매핑
    @ResponseBody // 이 메서드의 반환 값을 HTTP 응답 본문에 직접 작성 (JSON 등으로 변환)
    public ResponseEntity<Map<String, Boolean>> checkUserIdDuplication(@RequestParam String userId) {
        log.info("아이디 중복 확인 요청: {}", userId);
        boolean duplicated = userService.isUserIdDuplicated(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("duplicated", duplicated);
        return ResponseEntity.ok(response); // 200 OK와 JSON 응답 반환
    }

    //닉네임 중복 확인 API, URL: /user/check/nickname?nickname=testnick
    @GetMapping("/check/nickname")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplication(@RequestParam String nickname) {
        log.info("닉네임 중복 확인 요청: {}", nickname);
        boolean duplicated = userService.isNicknameDuplicated(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("duplicated", duplicated);
        return ResponseEntity.ok(response);
    }

    //이메일 중복 확인 API, URL: /user/check/email?email=test@example.com
    @GetMapping("/check/email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplication(@RequestParam String email) {
        log.info("이메일 중복 확인 요청: {}", email);
        boolean duplicated = userService.isEmailDuplicated(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("duplicated", duplicated);
        return ResponseEntity.ok(response);
    }

    //전화번호 중복 확인 API, URL: /user/check/phone?phone=01012345678
    @GetMapping("/check/phone")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplication(@RequestParam String phone) {
        log.info("전화번호 중복 확인 요청: {}", phone);
        boolean duplicated = userService.isPhoneDuplicated(phone);
        Map<String, Boolean> response = new HashMap<>();
        response.put("duplicated", duplicated);
        return ResponseEntity.ok(response);
    }

    // 로그인 페이지 표시
    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    // 추가: 아이디 찾기 페이지를 표시하는 GET 요청 핸들러
    @GetMapping("/find-id")
    public String showFindIdForm(@RequestParam(value = "success", required = false) String success,
                                 @RequestParam(value = "error", required = false) String error,
                                 Model model) {
        if (success != null) {
            model.addAttribute("foundUserId", success); // 성공적으로 찾은 아이디를 모델에 추가
        }
        if (error != null) {
            model.addAttribute("errorMessage", error); // 오류 메시지를 모델에 추가
        }
        log.info("아이디 찾기 폼 로드."); // 로깅 추가
        return "user/find-id"; // find-id.html 템플릿 반환
    }

    // 추가: 이름과 전화번호로 아이디를 찾기 요청을 처리하는 POST 요청 핸들러
    @PostMapping("/find-id")
    public String findIdProcess(@RequestParam("userName") String userName,
                                @RequestParam("phone") String phone,
                                RedirectAttributes rttr) {
        log.info("아이디 찾기 POST 요청 수신: 이름={}, 전화번호={}", userName, phone);

        // UserService의 findUserIdByUserNameAndPhone 메서드를 호출
        Optional<String> maskedUserIdOptional = userService.findUserIdByUserNameAndPhone(userName, phone);

        if (maskedUserIdOptional.isPresent()) {
            // 아이디를 찾았으면 성공 메시지와 마스킹된 아이디를 리다이렉트 파라미터로 전달
            rttr.addAttribute("success", maskedUserIdOptional.get());
            log.info("아이디 찾기 성공, 리다이렉트.");
        } else {
            // 아이디를 찾지 못했으면 오류 메시지를 리다이렉트 파라미터로 전달
            rttr.addAttribute("error", "입력하신 정보와 일치하는 아이디를 찾을 수 없습니다.");
            log.warn("아이디 찾기 실패, 리다이렉트.");
        }
        // GET 요청으로 리다이렉트하여 페이지를 다시 로드하고 메시지를 표시
        return "redirect:/user/find-id";
    }

    // 회원 정보 수정 폼 표시
    // @AuthenticationPrincipal을 사용하여 현재 로그인된 사용자 정보를 가져옴
    @GetMapping("/profile-edit")
    public String showProfileEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            log.warn("인증되지 않은 사용자가 프로필 수정 폼에 접근 시도.");
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
        }

        // CustomUserDetails에서 userIdx를 직접 가져와서 사용자 정보 조회
        UserDTO loggedInUser = userService.getUserProfile(userDetails.getUserIdx());
        if (loggedInUser == null) {
            log.error("로그인된 사용자(userIdx: {})의 정보를 DB에서 찾을 수 없습니다.", userDetails.getUserIdx());
            // 이 경우는 심각한 상황이므로, 로그아웃 처리 또는 오류 페이지로 리다이렉트
            return "redirect:/logout";
        }

        model.addAttribute("user", loggedInUser);
        log.info("프로필 수정 폼 로드: userIdx={}", userDetails.getUserIdx());
        return "user/profile-edit"; // src/main/resources/templates/user/profile-edit.html
    }

    // 일반 회원 정보 수정 처리 (이름, 닉네임, 이메일 등)
    @PostMapping("/profile-edit")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @ModelAttribute("user") UserDTO user, // 폼에서 넘어온 UserDTO
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            log.warn("인증되지 않은 사용자가 프로필 수정 요청 시도.");
            return "redirect:/user/login";
        }

        // 보안: 클라이언트에서 넘어온 userIdx를 사용하지 않고, 로그인된 사용자의 userIdx를 강제 설정
        user.setUserIdx(userDetails.getUserIdx());

        try {
            userService.updateUserInfo(user);
            redirectAttributes.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
            log.info("회원 정보 업데이트 성공: userIdx={}", userDetails.getUserIdx());
        } catch (IllegalArgumentException e) { // UserService에서 이메일 중복 시 던지는 예외
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            log.warn("회원 정보 업데이트 실패: userIdx={}, Error: {}", userDetails.getUserIdx(), e.getMessage());
        } catch (Exception e) {
            log.error("회원 정보 업데이트 중 알 수 없는 오류 발생: userIdx={}, Error: {}", userDetails.getUserIdx(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "회원 정보 수정 중 오류가 발생했습니다.");
        }
        return "redirect:/user/profile-edit"; // 수정 후 다시 프로필 수정 폼으로 (메시지 표시)
    }

    // 비밀번호 변경 처리
    @PostMapping("/profile-update-password")
    public String updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam("currentPassword") String currentPassword, // 폼 필드 이름 일치
                                 @RequestParam("newPassword") String newPassword,         // 폼 필드 이름 일치
                                 @RequestParam("confirmNewPassword") String confirmNewPassword, // 폼 필드 이름 일치
                                 RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            log.warn("인증되지 않은 사용자가 비밀번호 변경 요청 시도.");
            return "redirect:/user/login";
        }

        // 새 비밀번호와 확인 비밀번호 일치 여부 1차 검증
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            log.warn("비밀번호 변경 실패: userIdx={} - 새 비밀번호 불일치", userDetails.getUserIdx());
            return "redirect:/user/profile-edit";
        }

        try {
            // UserService의 비밀번호 변경 메서드 호출
            boolean isUpdated = userService.updatePassword(userDetails.getUserIdx(), currentPassword, newPassword);
            if (isUpdated) {
                redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
                log.info("비밀번호 변경 성공: userIdx={}", userDetails.getUserIdx());
            } else {
                // UserService에서 false를 반환하는 경우는 주로 현재 비밀번호 불일치
                redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않거나 변경에 실패했습니다.");
                log.warn("비밀번호 변경 실패: userIdx={} - 현재 비밀번호 불일치 또는 기타 실패", userDetails.getUserIdx());
            }
        } catch (Exception e) {
            log.error("비밀번호 변경 중 알 수 없는 오류 발생: userIdx={}, Error: {}", userDetails.getUserIdx(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 변경 중 오류가 발생했습니다.");
        }
        return "redirect:/user/profile-edit"; // 변경 후 다시 프로필 수정 폼으로
    }

    // 회원 정보 보기 페이지 표시
    @GetMapping("/profile") // ✅ 이 부분이 정확히 "/profile" 인지 확인
    public String showProfileView(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            log.warn("인증되지 않은 사용자가 프로필 보기 페이지에 접근 시도.");
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
        }

        UserDTO loggedInUser = userService.getUserProfile(userDetails.getUserIdx());
        if (loggedInUser == null) {
            log.error("로그인된 사용자(userIdx: {})의 정보를 DB에서 찾을 수 없습니다.", userDetails.getUserIdx());
            // 이 경우는 심각한 상황이므로, 로그아웃 처리 또는 오류 페이지로 리다이렉트
            return "redirect:/logout";
        }

        model.addAttribute("user", loggedInUser);
        log.info("프로필 보기 페이지 로드: userIdx={}", userDetails.getUserIdx());
        return "user/profile"; // ✅ 이 부분이 "user/profile" 인지 확인 (확장자 .html은 자동으로 붙음)
    }

    // --- 소프트 삭제를 위한 메서드 수정 필요 ---
    @PostMapping("/delete")
    @ResponseBody // JSON 응답을 위해 @ResponseBody 추가
    public ResponseEntity<Map<String, Object>> softDeleteUser(
            @RequestParam("passwordConfirm") String passwordConfirm, // 비밀번호 확인 파라미터 추가
            @AuthenticationPrincipal CustomUserDetails currentUser) { // CustomUserDetails 주입

        if (currentUser == null) {
            log.warn("인증되지 않은 사용자가 계정 삭제 요청 시도.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인 후 이용해 주세요."));
        }

        // CustomUserDetails에서 사용자 ID를 가져옴 (userIdx를 사용해도 됨)
        String userId = currentUser.getUsername();
        Long userIdx = currentUser.getUserIdx();

        try {
            // UserService의 softDeleteUser 메서드에 비밀번호 확인을 위한 파라미터 전달
            userService.softDeleteUser(userId, passwordConfirm); // userIdx를 사용할 수도 있음

            log.info("계정 소프트 삭제 성공: userId={}", userId);
            // 성공 시 JSON 응답 반환
            return ResponseEntity.ok(Map.of("success", true, "message", "계정이 성공적으로 삭제되었습니다."));

        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 등의 비즈니스 로직 오류
            log.warn("계정 삭제 실패: userId={}, Error: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 Unauthorized (비밀번호 불일치)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            // 그 외 예상치 못한 서버 오류
            log.error("계정 삭제 중 예상치 못한 오류 발생: userId={}, Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .body(Map.of("success", false, "message", "계정 삭제 중 오류가 발생했습니다. 다시 시도해 주세요."));
        }
    }
}