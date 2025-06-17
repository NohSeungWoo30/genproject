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

        // 인기 장르 모음
        List<Groups> groupByCategory = groupService.getGroupByCategory();
        model.addAttribute("groupByCategory", groupByCategory);

        // 최근 생성날 기준 모임
        List<Groups> groupByCreateDate = groupService.getGroupByCreateDate();
        model.addAttribute("groupByCreateDate", groupByCreateDate);

        // 모임 임박 기준 리스트
        List<Groups> groupByGroupDate = groupService.getGroupByGroupDate();
        model.addAttribute("groupByGroupDate", groupByGroupDate);

        if (groupByCategory != null && !groupByCategory.isEmpty()) {
            System.out.println("--- recommendGroupsList Start ---");
            System.out.println("Total recommend groups found: " + groupByCategory.size());
            for (int i = 0; i < groupByCategory.size(); i++) {
                Groups group = groupByCategory.get(i);
                System.out.println("Group " + (i + 1) + ":");
                System.out.println("  groupIdx: " + group.getGroupIdx());
                System.out.println("  title: " + group.getTitle());
                System.out.println("  ownerIdx: " + group.getOwnerIdx());

            }
            System.out.println("--- recommendGroupsList End ---");
        } else {
            System.out.println("recommendGroupsList is null or empty.");
        }

        return "main/main";
    }

}