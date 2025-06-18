package generationgap.co.kr.controller.group;

import generationgap.co.kr.domain.group.GroupMembers;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.dto.group.GroupDto;
import generationgap.co.kr.service.group.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupsApiController {

    private final GroupService groupService;

    public GroupsApiController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable int id) {
        Groups group = groupService.getGroupById(id);
        if (group == null) return ResponseEntity.notFound().build();

        List<GroupMembers> members = groupService.getGroupMembers(id);
        GroupDto dto = groupService.toDto(group, members);

        return ResponseEntity.ok(dto);
    }
}