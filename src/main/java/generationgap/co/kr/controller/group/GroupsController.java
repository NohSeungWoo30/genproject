package generationgap.co.kr.controller.group;

import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.group.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/group")  // 그룹에 해당하는 모든 경로
public class GroupsController {

    private final GroupService groupService;

    public GroupsController(GroupService groupService) {
        this.groupService = groupService;
    }
    @GetMapping("/test")
    public String test(Model model) {
        return  "group/test";
    }
    @GetMapping("/group_main")
    public String group_main(Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails){
        // 그룹 전체 리스트
        List<Groups> groupsList = groupService.getAllGroups();
        model.addAttribute("groupsList",groupsList);

        /* 콘솔 찍어보기 용
        for (Groups group : groupsList) {
            System.out.println(group.getGroupIdx());
        }*/

        return "group/group_main";
    }
    /*@GetMapping("/meetingcreate")
    public String meetingcreate(Model model) {
        Groups group = new Groups();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 로그인한 정보 가져옴 (세션 스코프에 저장되어있음)
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();


            // 그룹 객체에 호스트 정보 설정
            group.setOwnerIdx(userDetails.getUserIdx());

            *//* 현재 로그인 사용자 확인 *//*
            System.out.println(userDetails.getUserIdx()+", "+userDetails.getNickname());

            *//* 호스트 닉네임을 보여주기 위함 *//*
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("group", group);
        } else {
            // 로그인하지 않은 사용자 또는 익명 사용자일 경우
            // 로그인 페이지로 리다이렉트하여 로그인하도록 유도
            return "redirect:/user/login"; // 로그인 페이지의 실제 URL로 변경하세요.
        }
        List<CategoryMain> categoryMainList = groupService.getAllMainCategory();
        model.addAttribute("categoryMainList",categoryMainList);

        return "group/meetingcreate";
    }*/
    /* 모임방 생성 개설 프론트 */
    @GetMapping("/create")
    public String create(@RequestParam(required = false) Long ownerIdx, Model model) {
        Groups group = new Groups();

        if (ownerIdx != null) {
            group.setOwnerIdx(ownerIdx);
            model.addAttribute("nickname", "닉1"); // 임시 닉네임
            model.addAttribute("group", group);
        } else {
            return "redirect:/user/login";
        }

        List<CategoryMain> categoryMainList = groupService.getAllMainCategory();
        model.addAttribute("categoryMainList", categoryMainList);

        return "group/create";
    }

    // 그룹 생성 폼에서 데이터를 받아 처리하는 메서드
    @PostMapping("/group_success")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody Groups groupData,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (groupData.getOwnerIdx() == null || !groupData.getOwnerIdx().equals(userDetails.getUserIdx())) {
            // 클라이언트에서 보낸 ownerIdx와 실제 로그인된 사용자의 userIdx가 다를 경우 (보안상 중요)
            // 또는 ownerIdx가 누락된 경우 서버에서 주입
            groupData.setOwnerIdx(userDetails.getUserIdx());
            System.out.println("WARN: 회원번호가 클라이언트에서 누락되었거나 불일치하여, 로그인된 사용자로 설정합니다: " + userDetails.getUserIdx());
        }

        // --- 여기에 콘솔 출력 추가 (컨트롤러 단에서 JSON 데이터 확인) ---
        System.out.println("--- 컨트롤러에서 받은 JSON 데이터 확인 ---");
        System.out.println("OwnerIdx: " + groupData.getOwnerIdx());
        System.out.println("카테고리 대: " + groupData.getGroupCategoryMainIdx());
        System.out.println("세부사항: 현재사용안함" + groupData.getGroupCategorySubIdx());
        System.out.println("Title: " + groupData.getTitle());
        System.out.println("성별 제한: " + groupData.getGenderLimit());
        System.out.println("Age Min: " + groupData.getAgeMin());
        System.out.println("Age Max: " + groupData.getAgeMax());
        System.out.println("Meeting DateTime: " + groupData.getGroupDate()); // meetingDateTime이 groupDate에 매핑되는지 확인
        System.out.println("membersMin: " + groupData.getMembersMin());
        System.out.println("membersMax: " + groupData.getMembersMax());
        System.out.println("모임방 상세내용: " + groupData.getContent());
        System.out.println("장소명: " + groupData.getPlaceName());
        System.out.println("링크(있으면): " + groupData.getNaverPlaceUrl());
        System.out.println("분류: " + groupData.getPlaceCategory());
        System.out.println("주소: " + groupData.getPlaceAddress());
        System.out.println("위도: " + groupData.getLatitude());
        System.out.println("경도: " + groupData.getLongitude());

        try {
            // 실제 비즈니스 로직 호출
            int createdGroupId = groupService.groupCreate(groupData);
            System.out.println("방금 만든 방번호 : "+ createdGroupId);
            // 성공 응답 (JSON 형태로 클라이언트에 반환)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "그룹이 성공적으로 생성되었습니다!");

            return ResponseEntity.ok(response); // HTTP 200 OK와 함께 JSON 데이터 반환
        } catch (Exception e) {
            e.printStackTrace(); // 서버 콘솔에 에러 스택 트레이스 출력
            // 실패 응답 (JSON 형태로 클라이언트에 에러 메시지 반환)
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "그룹 생성에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // HTTP 500 에러와 함께 JSON 데이터 반환
        }
    }

    @GetMapping("/api/sub-categories")
    @ResponseBody // 이 메서드가 HTTP 응답 본문에 직접 데이터를 직렬화하여 반환함을 의미 (JSON, XML 등)
    public ResponseEntity<List<CategorySub>> getSubCategoriesByMainIdx(
            @RequestParam("mainCategoryIdx") int mainCategoryIdx) {
        List<CategorySub> subCategories = groupService.getAllSubCategory(mainCategoryIdx);
        return ResponseEntity.ok(subCategories); // HTTP 200 OK와 함께 JSON 데이터 반환
    }

}
