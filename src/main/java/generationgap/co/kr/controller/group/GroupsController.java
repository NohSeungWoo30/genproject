package generationgap.co.kr.controller.group;

import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.service.groups.GroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String showCreateGroupForm() {
        return "group/group_create";
    }

    // 그룹 생성 폼에서 데이터를 받아 처리하는 메서드
    @PostMapping("/create") // /groups/create 경로 처리
    public String createGroup(/* @ModelAttribute Groups groupData */) {
        // 여기에 폼 데이터를 받아 DB에 저장하는 로직 (서비스 계층 호출)
        // 예: groupService.saveGroup(groupData);
        // 저장 후 그룹 목록 페이지로 리다이렉트
        return "redirect:/group";
    }
}
