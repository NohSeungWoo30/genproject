package generationgap.co.kr.controller;

import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.group.GroupService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    private final GroupService groupService;

    public MainController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/main")
    public String mainPage(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        // 카테고리 전체 리스트
        List<CategoryMain> categoryMainList = groupService.getAllMainCategory();
        model.addAttribute("categoryMainList",categoryMainList);

        // 그룹 전체 리스트
        List<Groups> groupsList = groupService.getAllGroups();
        model.addAttribute("groupsList", groupsList);

        // 인기소셜링 좋아요 그룹방
        List<Groups> recommendGroupsList = groupService.getRecommendGroup();
        model.addAttribute("recommendGroupsList", recommendGroupsList);

        return "main/main";
    }

    @GetMapping("/main/main")
    public String mainPage2() {
        return "main/main"; // main.html 템플릿 반환
    }

    @GetMapping("/main/login")
    public String login2() {
        return "main/login"; // main.html 템플릿 반환
    }
    @GetMapping("/mypage")
    public String mypage() {
        return "mypage"; // main.html 템플릿 반환
    }

}