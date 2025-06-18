package generationgap.co.kr.controller.group;

import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.domain.group.GroupMembers;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.domain.payment.UserMemberships;
import generationgap.co.kr.dto.group.GroupDto;
import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.repository.payment.UserMembershipsRepository;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.group.GroupService;
import org.springframework.http.HttpStatus;
import generationgap.co.kr.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/group")  // 그룹에 해당하는 모든 경로
@RequiredArgsConstructor
public class GroupsController {
    @Autowired // 회원가입, 첫 구글로그인 횟수를 위한
    private UserMembershipsRepository userMembershipsRepository;

    private final GroupService groupService;
    @Autowired
    private UserService userService;
    // 이미지 저장 경로를 상수로 정의하여 관리 용이성 높임
    private static final String UPLOAD_DIR = "src/main/resources/static/upload/groupImg/";

    @GetMapping("/favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
        // 아무것도 반환하지 않음 favicon 에러 신경쓰여서 넣은거
    }

    /*상단 메뉴 버튼 모임리스트*/
    @GetMapping("/meetinglist")
    public String meetinglist(Model model,
                             @RequestParam(value="mainCategoryId", required = false) String category,
                             @AuthenticationPrincipal CustomUserDetails userDetails){

        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }

        // 카테고리 전체 리스트
        List<CategoryMain> categoryMainList = groupService.getAllMainCategory();
        model.addAttribute("categoryMainList",categoryMainList);
        // 카테고리 확인용
        for (CategoryMain categorys : categoryMainList) {
            System.out.println(categorys.getCmCategoryMainIdx());
            System.out.println(categorys.getCategoryMainName());
        }

        // 그룹 전체 리스트
        List<Groups> groupsList = groupService.getAllGroups();
        model.addAttribute("groupsList",groupsList);

        return "group/Meeting-list";
    }


    /* 모임방 생성 개설 프론트 */
    @GetMapping("/create")
    public String create(Model model,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        Groups group = new Groups();

        // 로그인한 정보 가져옴 (세션 스코프에 저장되어있음)
        if (userDetails != null) {
            // 그룹 객체에 로그인 유저의 호스트 정보 설정
            group.setOwnerIdx(userDetails.getUserIdx());

            //* 현재 로그인 사용자 확인 *//*
            System.out.println(userDetails.getUserIdx()+", "+userDetails.getNickname());

            //* 호스트 닉네임을 보여주기 위함 *//*
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("group", group);
        } else {
            // 방생성 시 로그인 정보가 없으면 로그인 페이지로
            return "redirect:/user/login";
        }
        List<CategoryMain> categoryMainList = groupService.getAllMainCategory();
        model.addAttribute("categoryMainList",categoryMainList);

        return "group/create";
    }

    // 그룹 생성 폼에서 데이터를 받아 처리하는 메서드
    @PostMapping("/group_success")
    @Transactional
    public String createGroup(
            @ModelAttribute Groups groupData,
            @RequestParam(value = "roomImageFile", required = false) MultipartFile roomImageFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("ownerNickname") String ownerNickname,
            RedirectAttributes redirectAttributes) {

        System.out.println("groupImageFile is null? " + (roomImageFile == null));

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        // 이미지 첨부 처리
        String imageUrl = null;
        try {
            imageUrl = saveGroupImage(roomImageFile);
            groupData.setGroupImgUrl(imageUrl); // 그룹 객체에 이미지 URL 설정
        } catch (IOException e) {
            System.err.println("파일 저장 실패: " + e.getMessage());
            // 에러 메시지를 RedirectAttributes에 추가하여 리다이렉트 후에도 메시지를 볼 수 있도록 함
            redirectAttributes.addFlashAttribute("errorMessage", "파일 업로드에 실패했습니다.");
            return "redirect:/group/create"; // 실패 페이지로 리다이렉트
        } catch (IllegalArgumentException e) { // 파일 타입/크기 유효성 검사 실패
            System.err.println("ERROR: 유효성 검사 실패: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/group/create";
        }
        UserDTO currentUserDTO = userDetails.getUserDTO();
        long userIdx = currentUserDTO.getUserIdx();

        Optional<UserMemberships> userMembershipOptional = userMembershipsRepository.findByUserIdx(userIdx);
        if(userMembershipOptional.isEmpty() || userMembershipOptional.get().getRemainingUses() == 0){
            redirectAttributes.addFlashAttribute("errorMessage", "이용권 횟수가 없습니다.");
            return "redirect:/group/create";
        }

        // Optional에서 실제 UserMemberships 객체를 추출
        UserMemberships userMembership = userMembershipOptional.get();
        // 그룹 저장 로직 호출
            try {
                // 남은 사용 횟수 감소 및 저장
                if (userMembership.getRemainingUses() > 0) {
                    userMembership.setRemainingUses(userMembership.getRemainingUses() - 1);
                    userMembershipsRepository.save(userMembership); // DB에 변경사항 반영
                    System.err.println("그룹매퍼전 확인 용");
                }
                // 그룹방 생성과 동시에 해당 그룹번호 리턴
                int createdGroupId = groupService.groupCreate(groupData);
                System.err.println("그룹매퍼후 확인 용");
                int newGroupIdx = groupData.getGroupIdx(); // 생선된 그룹번호
                int HostIdx = groupData.getOwnerIdx().intValue(); // 호스트 유저 인덱스
                String HostNickName = ownerNickname; // 닉네임

                GroupMembers hostMember = new GroupMembers();
                hostMember.setGroupIdx(newGroupIdx);
                hostMember.setUserIdx(HostIdx);
                hostMember.setNickName(HostNickName);

                // 해당 그룹방 참여멤버가 들어갈 테이블 생성
                groupService.insertHostMember(hostMember);
                System.err.println("그룹매퍼후 확인 용");
                redirectAttributes.addFlashAttribute("successMessage", "모임방 개설이 완료되었습니다!<br>이용권 1회 차감되었습니다.");


                // 성공적으로 생성이 되면 스크립트를 통해 디테일 페이지로 방생성번호와 함께 이동
                //return "redirect:/group/detail/" + createdGroupId;
                return "redirect:/group/detail?groupId=" + createdGroupId;
            } catch (Exception e) {
                System.err.println("그룹 저장 실패: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "그룹 생성에 실패했습니다: " + e.getMessage());
                return "redirect:/group/create"; // 실패 페이지로 리다이렉트

        }
    }

    @GetMapping("/api/sub-categories")
    @ResponseBody // 이 메서드가 HTTP 응답 본문에 직접 데이터를 직렬화하여 반환함을 의미 (JSON, XML 등)
    public ResponseEntity<List<CategorySub>> getSubCategoriesByMainIdx(
            @RequestParam("mainCategoryIdx") int mainCategoryIdx) {
        List<CategorySub> subCategories = groupService.getAllSubCategory(mainCategoryIdx);
        return ResponseEntity.ok(subCategories); // HTTP 200 OK와 함께 JSON 데이터 반환
    }

    // url경로로 넘어온 방 번호 숫자 캐치
    //@GetMapping("/detail/{groupId}")
    @GetMapping("/detail")
    //public String detail(@PathVariable("groupId") int groupId,
    public String detail(@RequestParam("groupId") int groupId,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {

        Groups groupDetail = groupService.getGroupById(groupId);
        int hostIndex = groupDetail.getOwnerIdx().intValue();
        System.out.println("Owner Index: " + hostIndex);
        // 호스트의 유저번호, 닉네임, 프로필 이미지
        UserDTO hostData = userService.getUserId_Nick(hostIndex);
        System.out.println("Host Data: " + hostData);
        if (groupDetail != null || hostData != null) {
            model.addAttribute("groupDetail", groupDetail);
            model.addAttribute("hostData", hostData);
            System.out.println("그룹 ID (groupIdx): " + groupDetail.getGroupIdx());
            System.out.println("그룹 제목 (title): " + groupDetail.getTitle());
            System.out.println("모임장 인덱스 (ownerIdx): " + groupDetail.getOwnerIdx());
        } else {
            // 그룹을 찾을 수 없는 경우 처리
            model.addAttribute("errorMessage", "요청하신 모임을 찾을 수 없습니다.");
            return "error/404"; // 또는 커스텀 에러 페이지
        }
        model.addAttribute("groupDetail", groupDetail);
        model.addAttribute("groupId", groupId);
        return  "group/meetingdetail";
    }

    // 파일 업로드 처리
    private String saveGroupImage(MultipartFile groupImageFile) throws IOException {
        System.out.println("DEBUG: saveGroupImage 메서드 진입");

        if (groupImageFile == null || groupImageFile.isEmpty()) {
            return null; // 파일이 없거나 비어있으면 null 반환
        }

        String originalFileName = groupImageFile.getOriginalFilename();
        String contentType = groupImageFile.getContentType();
        long size = groupImageFile.getSize();

        // 1. 파일 타입 유효성 검사
        if (contentType == null || !contentType.matches("image/(jpeg|png|gif|webp)")) {
            System.err.println("ERROR: 허용되지 않는 이미지 파일 형식: " + contentType);
            throw new IllegalArgumentException("이미지 파일 (JPEG, PNG, GIF, WebP)만 업로드할 수 있습니다.");
        }

        // 2. 파일 크기 유효성 검사 (10MB 제한)
        final long MAX_ALLOWED_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
        if (size > MAX_ALLOWED_SIZE_BYTES) {
            System.err.println("ERROR: 파일 크기 초과: " + originalFileName + " (" + size + " bytes). 허용 최대 크기: " + MAX_ALLOWED_SIZE_BYTES + " bytes.");
            throw new IllegalArgumentException("파일은 " + (MAX_ALLOWED_SIZE_BYTES / (1024 * 1024)) + "MB 이하만 업로드 가능합니다.");
        }

        // 3. 업로드 디렉토리 준비
        Path uploadDirPath = Paths.get(UPLOAD_DIR); // UPLOAD_DIR은 이미 "src/main/resources/static/upload/groupImg/"
        if (!Files.exists(uploadDirPath)) {
            try {
                Files.createDirectories(uploadDirPath); // createDirectories는 중간 디렉토리도 모두 생성
                System.out.println("DEBUG: UPLOAD_DIR 생성: " + uploadDirPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("ERROR: 업로드 디렉토리 생성 실패: " + e.getMessage());
                throw new RuntimeException("파일 업로드 디렉토리 생성 중 오류가 발생했습니다.", e);
            }
        }

        // 4. 고유한 파일 이름 생성
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uuidFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadDirPath.resolve(uuidFileName);

        // 5. 파일 저장
        try {
            System.out.println("DEBUG: 파일 저장 시도: " + filePath.toAbsolutePath());
            groupImageFile.transferTo(filePath);
            System.out.println("DEBUG: 파일 저장 성공: " + uuidFileName);
        } catch (IOException e) {
            System.err.println("ERROR: 파일 시스템에 파일 저장 실패: " + e.getMessage());
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        // 6. 웹에서 접근할 수 있는 URL 반환
        return "/upload/groupImg/" + uuidFileName;
    }







    @GetMapping("/match")
    @ResponseBody
    public List<Groups> getMatchedGroups(
            @RequestParam List<Integer> categories,
            @RequestParam int minAge,
            @RequestParam int maxAge,
            @RequestParam String gender
    ) {
        return groupService.getMatchedGroups(categories, minAge, maxAge, gender);
    }


    @GetMapping("/api/current-group")
    @ResponseBody
    public ResponseEntity<GroupDto> getCurrentGroup(@RequestParam Long userId) {
        Optional<GroupDto> group = groupService.findActiveGroupForUser(userId);
        return group.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/api/groups/{id}")
    @ResponseBody
    public ResponseEntity<?> getGroupById(@PathVariable int id) {
        Groups group = groupService.getGroupById(id);

        if (group == null) {
            return ResponseEntity.status(404).body("해당 그룹을 찾을 수 없습니다.");
        }

        // 프론트에서 필요한 필드만 가공하거나, DTO로 변환해서 반환하는 것이 이상적입니다.
        return ResponseEntity.ok(group);
    }


    @PostMapping("/api/groups/{groupId}/join")
    @ResponseBody
    public ResponseEntity<GroupDto> joinGroup(@PathVariable Long groupId,
                                              @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        /* 1) 참가 + 최신 정보까지 한 번에 받아오기 */
        GroupDto groupDto = groupService.joinGroup(
                groupId,                 // 방 번호
                user.getUserIdx(),       // 호출자 PK
                user.getNickname());     // 호출자 닉네임

        /* 2) 그대로 반환 */
        return ResponseEntity.ok(groupDto);
    }


    @PostMapping("/api/groups/{groupIdx}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupIdx, @RequestParam Long userId) {
        groupService.leaveGroup(groupIdx, userId);
        return ResponseEntity.ok(Map.of("message", "방 나가기 성공"));
    }




    @GetMapping("/api/groups/{groupIdx}")
    public ResponseEntity<GroupDto> getGroupDetails(@PathVariable int groupIdx) {
        GroupDto dto = groupService.getGroupDetails(groupIdx);
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/api/groups/detail/{groupIdx}")
    public ResponseEntity<GroupDto> getGroupDetailJson(@PathVariable int groupIdx){
        return ResponseEntity.ok(groupService.getGroupDetails(groupIdx));
    }



}
