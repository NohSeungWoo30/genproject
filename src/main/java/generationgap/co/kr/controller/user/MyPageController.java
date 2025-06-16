package generationgap.co.kr.controller.user;

import generationgap.co.kr.domain.mypage.ChangePasswordDTO;
import generationgap.co.kr.domain.mypage.UpdateInfoDTO;
import generationgap.co.kr.mapper.user.UserMapper;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.user.MyPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final UserMapper userMapper;

    @Autowired
    public MyPageController(MyPageService myPageService, UserMapper userMapper) {
        this.myPageService = myPageService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public String showMyPage(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return "redirect:/user/login";
        }
        Long userIdx = customUserDetails.getUserIdx();

        model.addAttribute("user", userMapper.findByUserIdx(userIdx));
        model.addAttribute("posts", myPageService.getMyPosts(userIdx));
        model.addAttribute("history", myPageService.getMyHistory(userIdx));
        model.addAttribute("payments", myPageService.getMyPayments(userIdx));

        return "mypage";
    }

    @PostMapping("/update")
    public String updateMyInfo(@ModelAttribute UpdateInfoDTO dto,
                               @AuthenticationPrincipal CustomUserDetails customUserDetails,
                               RedirectAttributes redirectAttributes) {
        dto.setUserIdx(customUserDetails.getUserIdx());
        myPageService.updateInfo(dto);
        redirectAttributes.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        return "redirect:/mypage";
    }

    @PostMapping("/change-password")
    public String changeMyPassword(@ModelAttribute ChangePasswordDTO dto,
                                   @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                   RedirectAttributes redirectAttributes) {
        dto.setUserIdx(customUserDetails.getUserIdx());
        boolean isSuccess = myPageService.changePassword(dto);

        if (isSuccess) {
            redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
        }
        return "redirect:/mypage#info-section";
    }

    @PostMapping("/update-profile-image")
    public String updateProfileImage(@RequestParam("profileImage") MultipartFile profileImage,
                                     @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            myPageService.updateProfileImage(customUserDetails.getUserIdx(), profileImage);
            redirectAttributes.addFlashAttribute("message", "프로필 이미지가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "이미지 업로드 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage";
    }
}