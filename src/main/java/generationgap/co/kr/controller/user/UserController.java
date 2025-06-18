package generationgap.co.kr.controller.user;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.user.UserService;
// ─── 추가된 Mapper import ───

import generationgap.co.kr.mapper.payment.PaymentMapper;
import generationgap.co.kr.mapper.post.BoardPostMapper;

import java.util.*;

import generationgap.co.kr.dto.mypage.PaymentDto;
import generationgap.co.kr.dto.mypage.PostDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/user",""})
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // ─── 추가된 Mapper 주입 ───


    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private BoardPostMapper boardpostMapper;


    // 회원가입 폼 표시
    @GetMapping("/signup")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "user/signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute UserDTO user, // @ModelAttribute 사용 시 UserDTO가 폼 데이터를 받음
                               @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile, // ⭐ 추가
                               RedirectAttributes redirectAttributes) {

        try {
            userService.registerUser(user, profileImageFile); // ⭐ MultipartFile도 함께 전달
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 성공적으로 완료되었습니다. 로그인해주세요.");
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // 에러 발생 시 회원가입 폼으로 다시 리다이렉트하고 기존 입력값 유지 (필요 시 Model에 user 객체 추가)
            // redirectAttributes.addFlashAttribute("user", user); // 이 경우 폼에서 th:object="${user}"로 받아야 함
            return "redirect:/user/signup"; // 회원가입 폼 URL로 리다이렉트
        } catch (RuntimeException e) {
            log.error("회원가입 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 시스템 오류가 발생했습니다.");
            return "redirect:/user/signup";
        }
    }

    // --- 중복 확인 API ---
    @GetMapping("/check/userId")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUserIdDuplication(@RequestParam String userId) {
        return createDuplicationResponse("아이디", userId, userService.isUserIdDuplicated(userId));
    }

    @GetMapping("/check/nickname")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplication(@RequestParam String nickname) {
        return createDuplicationResponse("닉네임", nickname, userService.isNicknameDuplicated(nickname));
    }

    @GetMapping("/check/email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplication(@RequestParam String email) {
        return createDuplicationResponse("이메일", email, userService.isEmailDuplicated(email));
    }

    @GetMapping("/check/phone")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplication(@RequestParam String phone) {
        return createDuplicationResponse("전화번호", phone, userService.isPhoneDuplicated(phone));
    }

    @GetMapping("/check/userCi")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUserCiDuplication(@RequestParam String userCi) {
        return createDuplicationResponse("CI", userCi, userService.isUserCiDuplicated(userCi));
    }

    private ResponseEntity<Map<String, Boolean>> createDuplicationResponse(String type, String value, boolean duplicated) {
        log.info("{} 중복 확인 요청: {}", type, value);
        Map<String, Boolean> response = new HashMap<>();
        response.put("duplicated", duplicated);
        return ResponseEntity.ok(response);
    }

    // 로그인 폼 표시
    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    // 아이디 찾기 폼 (GET 요청)
    @GetMapping("/find-id")
    public String showFindIdForm(@RequestParam(value = "foundUserId", required = false) String foundUserId, // 'success' 대신 'foundUserId'로 변경
                                 @RequestParam(value = "errorMessage", required = false) String errorMessage,   // 'error' 대신 'errorMessage'로 변경
                                 Model model) {
        if (foundUserId != null) {
            model.addAttribute("foundUserId", foundUserId);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        log.info("아이디 찾기 폼 로드.");
        return "user/find-id"; // user/find-id.html 템플릿 반환
    }

    // 아이디 찾기 처리 (POST 요청)
    @PostMapping("/find-id")
    public String findIdProcess(@RequestParam("userName") String userName,
                                @RequestParam("phone") String phone,
                                RedirectAttributes rttr) {
        log.info("아이디 찾기 요청: 이름={}, 전화번호={}", userName, phone);

        Optional<String> result = userService.findUserIdByUserNameAndPhone(userName, phone);

        if (result.isPresent()) {
            rttr.addAttribute("foundUserId", result.get()); // 'success' 대신 'foundUserId'로 변경
            log.info("아이디 찾기 성공: {}", result.get()); // 성공 시 아이디를 로그에 기록
        } else {
            rttr.addAttribute("errorMessage", "입력한 정보로 아이디를 찾을 수 없습니다."); // 'error' 대신 'errorMessage'로 변경
            log.warn("아이디 찾기 실패: 이름={}, 전화번호={}", userName, phone); // 실패 시 입력 정보를 로그에 기록
        }
        return "redirect:/user/find-id"; // GET 요청으로 리다이렉트
    }

    // ─── 마이페이지 뷰 매핑 추가 ───
    @GetMapping("/mypage")
    public String mypage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/user/login";
        }

        UserDTO user = userService.getUserProfile(userDetails.getUserIdx());
        if (user == null) {
            return "redirect:/logout";
        }

        model.addAttribute("user", user);

        // ─── 추가된 부분: 참여 이력, 결제 내역, 내가 쓴 글 조회 ───

        List<PaymentDto> payments = paymentMapper.findPaymentsByUserIdx(user.getUserIdx());
        if (payments == null) payments = new ArrayList<>();
        List<PostDto> posts = boardpostMapper.findPostsByAuthorIdx(user.getUserIdx());
        if (posts == null) posts = new ArrayList<>();

        Map<String, Object> pageData = new HashMap<>();

        pageData.put("payments", payments);
        pageData.put("posts",    posts);
        model.addAttribute("pageData", pageData);
        return "mypage";    // resources/templates/mypage.html 렌더링
    }

    // 프로필 수정 폼
    @GetMapping("/profile-edit")
    public String showProfileEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            log.warn("비로그인 사용자의 프로필 수정 폼 접근");
            return "redirect:/user/login";
        }

        UserDTO user = userService.getUserProfile(userDetails.getUserIdx());
        if (user == null) {
            log.error("로그인 사용자의 정보 없음: userIdx={}", userDetails.getUserIdx());
            return "redirect:/logout";
        }

        model.addAttribute("user", user);
        log.info("프로필 수정 폼 로드: userIdx={}", userDetails.getUserIdx());
        return "user/profile-edit";
    }

    // 프로필 수정 처리
    @PostMapping("/profile-edit")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @ModelAttribute("user") UserDTO user,
                                @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile, // ⭐ 추가: MultipartFile 파라미터
                                @RequestParam(value = "deleteProfileImage", defaultValue = "false") boolean deleteProfileImage, // ⭐ 추가: 이미지 삭제 요청 파라미터
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            log.warn("비로그인 사용자의 프로필 수정 시도");
            return "redirect:/user/login";
        }

        user.setUserIdx(userDetails.getUserIdx());

        try {
            // ⭐ 프로필 이미지 처리 로직 호출
            userService.updateUserProfile(user, profileImageFile, deleteProfileImage);
            redirectAttributes.addFlashAttribute("messageInfo", "회원 정보가 수정되었습니다.");
            log.info("프로필 수정 성공: userIdx={}", userDetails.getUserIdx());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessageInfo01", e.getMessage());
            log.warn("프로필 수정 실패: {}", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessageInfo02", "회원 정보 수정 중 오류 발생");
            log.error("프로필 수정 오류: {}", e.getMessage(), e);
        }
        return "redirect:/user/profile";
    }

    // 비밀번호 변경 처리
    @PostMapping("/profile-update-password")
    public String updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            log.warn("비로그인 사용자의 비밀번호 변경 시도");
            return "redirect:/user/login";
        }

        // 프론트엔드 유효성 검사를 했지만, 서버에서도 한 번 더 확인 (이중 체크)
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessagePass01", "새 비밀번호가 일치하지 않습니다.");
            log.warn("비밀번호 불일치: userIdx={}", userDetails.getUserIdx());
            return "redirect:/user/profile";
        }
        if (newPassword.length() < 6) { // 최소 길이 검증 추가
            redirectAttributes.addFlashAttribute("errorMessagePass02", "새 비밀번호는 최소 6자 이상이어야 합니다.");
            log.warn("비밀번호 변경 실패: 새 비밀번호 길이 부족");
            return "redirect:/user/profile";
        }


        try {
            boolean updated = userService.updatePassword(userDetails.getUserIdx(), currentPassword, newPassword);
            if (updated) {
                redirectAttributes.addFlashAttribute("messagePass", "비밀번호가 변경되었습니다.");
                log.info("비밀번호 변경 성공: userIdx={}", userDetails.getUserIdx());
            } else {
                redirectAttributes.addFlashAttribute("errorMessagePass03", "현재 비밀번호가 틀립니다.");
                log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessagePass04", "비밀번호 변경 중 오류 발생");
            log.error("비밀번호 변경 오류: {}", e.getMessage(), e);
        }
        return "redirect:/user/profile";
    }


    // 프로필 보기 및 설정 기능 통합
    @GetMapping("/profile")
    public String showProfileAndSettingsView(Authentication authentication, Model model) {

        // 1. 비로그인 사용자 리디렉션 (기존과 동일)
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("비로그인 사용자의 프로필 보기 시도");
            return "redirect:/user/login";
        }

        // 2. 인증 정보를 통해 UserDTO 객체 가져오기 (기존과 동일)
        Object principal = authentication.getPrincipal();
        UserDTO user = null;

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            user = userService.getUserProfile(userDetails.getUserIdx());
            log.info("일반회원 프로필 보기 확인");
        } else if (principal instanceof DefaultOAuth2User) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) principal;
            String userId = oAuth2User.getAttribute("sub"); // Google의 경우 sub가 고유 식별자
            user = userService.getOAuth2UserProfile(userId);
            log.info("OAuth2 로그인 유저id: {}", userId);
        } else {
            log.warn("알 수 없는 사용자 타입: {}", principal.getClass().getName());
        }

        // 3. 사용자 정보가 없을 경우 처리 (기존과 동일)
        if (user == null) {
            log.error("프로필 보기 실패: DB에 사용자 없음");
            return "redirect:/logout";
        }

        // 4. [추가된 로직] /settings의 isGoogleUser 판별 기능 통합
        // UserDTO에 provider 정보가 있다는 가정 하에 진행합니다.
        boolean isGoogleUser = user.getProvider() != null && "GOOGLE".equalsIgnoreCase(user.getProvider());
        model.addAttribute("isGoogleUser", isGoogleUser);
        log.info("Google 사용자 여부 확인: {}", isGoogleUser);


        // 5. 모델에 사용자 정보 추가 후 뷰 렌더링 (기존과 동일)
        model.addAttribute("user", user);
        log.info("프로필 보기 로드: user={}", user);

        // 이제 "user/settings"가 아닌 "user/profile"을 사용합니다.
        return "user/profile";
    }

    // 회원 탈퇴 처리 (소프트 삭제)
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> softDeleteUser(
            @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
            @AuthenticationPrincipal CustomUserDetails currentUser, // ⭐ 다시 CustomUserDetails로 변경 ⭐
            HttpServletRequest request // HttpServletRequest 주입
    ) {
        if (currentUser == null) {
            log.warn("비로그인 사용자의 탈퇴 요청");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인 후 이용하세요."));
        }

        String userId = currentUser.getUsername(); // 현재 로그인된 사용자의 ID 획득
        String userProvider = currentUser.getUserDTO().getProvider(); // ⭐ CustomUserDetails에서 provider 정보 획득 ⭐

        try {
            // provider 컬럼 값으로 유저 유형 판별
            if ("GOOGLE".equalsIgnoreCase(userProvider)) { // "google"은 예시 값. 실제 DB 값과 일치시켜야 함.
                // 구글 간편 로그인 유저: 비밀번호 확인 없이 바로 소프트 삭제
                userService.softDeleteGoogleUser(userId);
                log.info("구글 간편 로그인 계정 소프트 삭제 성공: userId={}", userId);
            } else if ("LOCAL".equalsIgnoreCase(userProvider)) { // "local"도 예시 값. 실제 DB 값과 일치시켜야 함.
                // 로컬 유저: 비밀번호 확인 필요
                if (passwordConfirm == null || passwordConfirm.isEmpty()) {
                    log.warn("로컬 계정 삭제 실패: 비밀번호 누락 (userId={})", userId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "message", "비밀번호를 입력해주세요."));
                }
                userService.softDeleteLocalUser(userId, passwordConfirm);
                log.info("로컬 계정 소프트 삭제 성공: userId={}", userId);
            } else {
                // 알 수 없거나 지원하지 않는 provider 타입
                log.error("지원하지 않는 사용자 provider 타입: userId={}, provider={}", userId, userProvider);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "알 수 없는 사용자 유형입니다."));
            }

            // --- 현재 모든 세션 무효화 부분 추가 ---
            // 계정 삭제 성공 시 현재 사용자의 세션을 무효화하여 강제 로그아웃
            HttpSession session = request.getSession(false); // 기존 세션이 없으면 새로 생성하지 않음
            if (session != null) {
                session.invalidate(); // 세션 무효화
                log.info("사용자 세션이 성공적으로 무효화되었습니다: userId={}", userId);
            }
            // --- 세션 무효화 부분 끝 ---

            return ResponseEntity.ok(Map.of("success", true, "message", "계정이 삭제되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("계정 삭제 실패 (IllegalArgumentException): userId={}, message={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("계정 삭제 중 예상치 못한 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "계정 삭제 중 예상치 못한 오류 발생"));
        }
    }
}