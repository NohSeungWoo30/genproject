package generationgap.co.kr.controller.group;

import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.service.group.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/group")  // 그룹에 해당하는 모든 경로
public class GroupsController {

    private final GroupService groupService;

    public GroupsController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/group_main")
    public String main(Model model){
        // 그룹 전체 리스트
        List<Groups> groupsList = groupService.getAllGroups();
        model.addAttribute("groupsList",groupsList);

        /* 콘솔 찍어보기 용
        for (Groups group : groupsList) {
            System.out.println(group.getGroupIdx());
        }*/

        return "group/group_main";
    }

    // 새 그룹 생성 폼 페이지를 보여주는 메서드
    @GetMapping("/group_create") // /groups/new 경로 처리
    public String showCreateGroupForm(@RequestParam("user_idx") int user_idx,
                                      @RequestParam("nickname") String nickname,
                                      Model model) {
        List<CategoryMain> categoryMainList = groupService.getAllMainCategory();
        model.addAttribute("categoryMainList",categoryMainList);

        /* 호스트가 누구인지 보여주기 위함 */
        model.addAttribute("nickname", nickname);

        Groups group = new Groups();
        group.setOwnerIdx(user_idx);

        model.addAttribute("group", group);

        return "group/group_create";
    }

    // 그룹 생성 폼에서 데이터를 받아 처리하는 메서드
    @PostMapping("/group_success")
    public String createGroup( @ModelAttribute Groups groupData,
                               @RequestParam("nickname") String nickname,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        // --- 여기에 콘솔 출력 추가 (컨트롤러 단에서 폼 데이터 확인) ---
        System.out.println("--- 컨트롤러에서 받은 폼 데이터 확인 ---");
        System.out.println("OwnerIdx: " + groupData.getOwnerIdx());
        System.out.println("Nickname(저장X): " + nickname); // DB에 저장은 안함
        System.out.println("카테고리 대: " + groupData.getGroupCategoryMainIdx());
        System.out.println("세부사항: " + groupData.getGroupCategorySubIdx());
        System.out.println("Title: " + groupData.getTitle());
        System.out.println("성별 제한: " + groupData.getGenderLimit()); // M F A
        System.out.println("Age Min: " + groupData.getAgeMin());
        System.out.println("Age Max: " + groupData.getAgeMax());
        System.out.println("Group Date: " + groupData.getGroupDate()); //

        try {
            // Service 계층으로 데이터 전달
            // groupService.createGroup(groupData); // 서비스 메서드가 Group 엔티티를 받도록 변경

            redirectAttributes.addFlashAttribute("message", "그룹이 성공적으로 생성되었습니다!");
            redirectAttributes.addFlashAttribute("createdGroup", groupData);

            return "/group/group_success"; // 그룹 목록 페이지 등으로 리다이렉트
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "그룹 생성에 실패했습니다: " + e.getMessage());

            // 실패 시 폼으로 다시 리다이렉트하면서 기존 입력 값을 유지하도록 ModelAttribute를 활용
            return "/group/group_create";
        }
        // 예: groupService.saveGroup(groupData);
        // 저장 후 그룹 목록 페이지로 리다이렉트
    }

    @GetMapping("/api/sub-categories")
    @ResponseBody // 이 메서드가 HTTP 응답 본문에 직접 데이터를 직렬화하여 반환함을 의미 (JSON, XML 등)
    public ResponseEntity<List<CategorySub>> getSubCategoriesByMainIdx(
            @RequestParam("mainCategoryIdx") int mainCategoryIdx) {
        List<CategorySub> subCategories = groupService.getAllSubCategory(mainCategoryIdx);
        return ResponseEntity.ok(subCategories); // HTTP 200 OK와 함께 JSON 데이터 반환
    }
}
