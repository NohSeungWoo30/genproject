package generationgap.co.kr.service.group;


import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.domain.group.GroupMembers;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.dto.group.GroupDto;
import generationgap.co.kr.dto.group.MemberDto;
import generationgap.co.kr.mapper.group.GroupsMapper;
import generationgap.co.kr.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired // 생성자 자동생성 의존성 주입
    private GroupsMapper groupsMapper;   // GroupMapper 의존성 주입

    @Autowired
    private UserMapper userMapper;

    public List<Groups> getAllGroups(){
        List<Groups> fullAllGroups = groupsMapper.getAllGroups();

        if (fullAllGroups != null && !fullAllGroups.isEmpty()) {
            List<Groups> processedList = fullAllGroups.stream()
                    .filter(java.util.Objects::nonNull)
                    .limit(10) // 뽑아온 리스트중 최대10까지만 가져옴
                    .map(group -> {
                        // 각 그룹 객체에 대해 주소에서 지역구를 추출하여 설정
                        String address = group.getPlaceAddress();
                        if (address != null) {
                            group.setDistrict(extractDistrict(address));
                        }
                        return group;
                    })
                    .collect(Collectors.toList());
            return processedList;

        } else {
            return new java.util.ArrayList<>();
        }
    }

    public List<Groups> getRecommendGroup(){
        List<Groups> fullRecommendGroupsList =  groupsMapper.getRecommendGroup();

        if (fullRecommendGroupsList != null && !fullRecommendGroupsList.isEmpty()) {
            List<Groups> processedList = fullRecommendGroupsList.stream()
                    .filter(java.util.Objects::nonNull)
                    .limit(10) // 뽑아온 리스트중 최대10까지만 가져옴
                    .map(group -> {
                        // 각 그룹 객체에 대해 주소에서 지역구를 추출하여 설정
                        String address = group.getPlaceAddress();
                        if (address != null) {
                            group.setDistrict(extractDistrict(address));
                        }
                        return group;
                    })
                    .collect(Collectors.toList());
            return processedList;

        } else {
            return new java.util.ArrayList<>();
        }
    }

    // 주소에서 '구' 정보를 추출하는 헬퍼 메서드
    private String extractDistrict(String address) {
        // 'XX구' 형태의 문자열을 찾는 정규 표현식
        // 예: 강남구, 서초구, 해운대구
        // 한글 두 글자 + '구' (이름이 세 글자 이상인 구도 있을 수 있으므로 확장 가능)
        Pattern pattern = Pattern.compile("([가-힣]+)구"); // 최소 2글자 이상의 한글 + '구'
        Matcher matcher = pattern.matcher(address);

        if (matcher.find()) {
            return matcher.group(0); // 'xx구' 전체를 반환
        }

        return ""; // 또는 null
    }

    public List<CategoryMain> getAllMainCategory(){
        return groupsMapper.getAllMainCategory();
    }

    public List<CategorySub> getAllSubCategory(int mainCategoryIdx){
        return groupsMapper.getAllSubCategory(mainCategoryIdx);
    }

    public int groupCreate(Groups groups){
        groupsMapper.insertGroup(groups);

        return groups.getGroupIdx();
    }

    public void insertHostMember(GroupMembers hostMember){
        groupsMapper.insertHostMember(hostMember);
    }
    public Groups getGroupById(int groupId){
        return groupsMapper.getGroupById(groupId);
    }
    public List<Groups> getGroupByCategory(){
        List<Groups> fullGroupByCategory =  groupsMapper.getGroupByCategory();

        if (fullGroupByCategory != null && !fullGroupByCategory.isEmpty()) {
            List<Groups> processedList = fullGroupByCategory.stream()
                    .filter(java.util.Objects::nonNull)
                    .limit(10) // 뽑아온 리스트중 최대10까지만 가져옴
                    .map(group -> {
                        // 각 그룹 객체에 대해 주소에서 지역구를 추출하여 설정
                        String address = group.getPlaceAddress();
                        if (address != null) {
                            group.setDistrict(extractDistrict(address));
                        }
                        return group;
                    })
                    .collect(Collectors.toList());
            return processedList;

        } else {
            return new java.util.ArrayList<>();
        }
    }

    public List<Groups> getGroupByCreateDate(){
        List<Groups> fullGroupByCreateDate =  groupsMapper.getGroupByCreateDate();

        if (fullGroupByCreateDate != null && !fullGroupByCreateDate.isEmpty()) {
            List<Groups> processedList = fullGroupByCreateDate.stream()
                    .filter(java.util.Objects::nonNull)
                    .limit(10) // 뽑아온 리스트중 최대10까지만 가져옴
                    .map(group -> {
                        // 각 그룹 객체에 대해 주소에서 지역구를 추출하여 설정
                        String address = group.getPlaceAddress();
                        if (address != null) {
                            group.setDistrict(extractDistrict(address));
                        }
                        return group;
                    })
                    .collect(Collectors.toList());
            return processedList;

        } else {
            return new java.util.ArrayList<>();
        }
    }

    public List<Groups> getGroupByGroupDate(){
        List<Groups> fullGroupByGroupDate =  groupsMapper.getGroupByCreateDate();

        if (fullGroupByGroupDate != null && !fullGroupByGroupDate.isEmpty()) {
            List<Groups> processedList = fullGroupByGroupDate.stream()
                    .filter(java.util.Objects::nonNull)
                    .limit(10) // 뽑아온 리스트중 최대10까지만 가져옴
                    .map(group -> {
                        // 각 그룹 객체에 대해 주소에서 지역구를 추출하여 설정
                        String address = group.getPlaceAddress();
                        if (address != null) {
                            group.setDistrict(extractDistrict(address));
                        }
                        return group;
                    })
                    .collect(Collectors.toList());
            return processedList;

        } else {
            return new java.util.ArrayList<>();
        }
    }







    public List<Groups> getMatchedGroups(List<Integer> categories, int minAge, int maxAge, String gender) {
        return groupsMapper.getMatchedGroups(categories, minAge, maxAge, gender);
    }


    public GroupDto toDto(Groups group, List<GroupMembers> members) {
        GroupDto dto = new GroupDto();
        dto.setGroupIdx(group.getGroupIdx());
        dto.setTitle(group.getTitle());
        dto.setContent(group.getContent());
        dto.setGroupImgUrl(group.getGroupImgUrl());
        dto.setGroupDate(group.getGroupDate());
        dto.setMembersMax(group.getMembersMax());
        dto.setPartyMember(members.size());

        // ✅ 참가자 리스트 가공
        List<MemberDto> participants = members.stream().map(m -> {
            MemberDto md = new MemberDto();
            md.setNickname(m.getNickName());
            md.setAvatar(m.getUser() != null ? m.getUser().getProfileName() : null);
            return md;
        }).collect(Collectors.toList());
        dto.setParticipants(participants);

        // ✅ 여기서 방장 정보 확인
        System.out.println("✅ group.getOwnerIdx() = " + group.getOwnerIdx());
        for (GroupMembers m : members) {
            System.out.println("👤 member.userIdx = " + m.getUserIdx());
        }

        GroupMembers hostMember = members.stream()
                .filter(m -> m.getUserIdx() == group.getOwnerIdx())
                .findFirst()
                .orElse(null);

        if (hostMember != null && hostMember.getUser() != null) {
            dto.setHostNickname(hostMember.getUser().getNickname());
            dto.setHostAvatar(hostMember.getUser().getProfileName());
        } else {
            System.out.println("🚨 hostMember 또는 hostMember.getUser()가 null입니다.");
        }

        return dto;
    }

    public List<GroupMembers> getGroupMembers(int groupId) {
        return groupsMapper.getGroupMembersByGroupId(groupId); // Mapper에 해당 메서드 있어야 함
    }

   /* public void joinGroup(Long groupIdx, Long userIdx) {
        System.out.println("✅ joinGroup() 호출됨: groupIdx = " + groupIdx + ", userIdx = " + userIdx);

        int count = groupsMapper.isAlreadyMember(groupIdx, userIdx); // ← 여기 수정
        if (count == 0) {  // ← count가 0이면 참여 안 한 상태
            String nickname = userMapper.findNicknameById(userIdx);
            System.out.println("🧾 참가 요청 닉네임 확인: userIdx = " + userIdx + ", nickname = " + nickname);
            groupsMapper.insertGroupMember(groupIdx, userIdx, nickname);
        } else {
            System.out.println("⚠ 이미 참가한 유저입니다: userIdx = " + userIdx + ", groupIdx = " + groupIdx);
        }
    }*/

    public GroupDto joinGroup(Long groupId, Long userIdx, String nickname) {

        /* 1) 이미 참가했는지 확인 */
        int dup = groupsMapper.isAlreadyMember(groupId, userIdx);
        if (dup == 0) {
            /* 2) 멤버 INSERT */
            groupsMapper.insertGroupMember(groupId, userIdx, nickname);

            /* 3) party_member +1 (NULL→1 포함) */
            groupsMapper.increasePartyMember(groupId);
        }

        /* 4) 최신 상세 DTO 반환 */
        return groupsMapper.findGroupDetail(groupId.intValue());
    }


    @Transactional
    public void leaveGroup(Long groupIdx, Long userIdx) {
        System.out.println("🧹 leaveGroup 진입: groupIdx=" + groupIdx + ", userIdx=" + userIdx);

        groupsMapper.deleteGroupMember(groupIdx, userIdx);
        System.out.println("🚪 유저가 방에서 나감: groupIdx = " + groupIdx + ", userIdx = " + userIdx);
    }


    /*public GroupDto getGroupDetails(int groupIdx) {
        GroupDto dto = groupsMapper.findGroupById(groupIdx); // 기본 그룹 정보

        if (dto == null) {
            throw new IllegalArgumentException("해당 그룹을 찾을 수 없습니다: " + groupIdx);
        }

        List<MemberDto> participants = groupsMapper.findParticipantsByGroupIdx(groupIdx); // 참여자 목록
        dto.setParticipants(participants);

        return dto;
    }*/


    public GroupDto getGroupDetails(int groupIdx) {

        // ① 새 상세 쿼리: host·주소·인원 정보까지 포함
        GroupDto dto = groupsMapper.findGroupDetail(groupIdx);

        // ② participants까지 필요하면 그대로 추가
        List<MemberDto> participants =
                groupsMapper.findParticipantsByGroupIdx(groupIdx);
        dto.setParticipants(participants);

        return dto;
    }

    public Optional<GroupDto> findActiveGroupForUser(Long userId) {
        return Optional.ofNullable(groupsMapper.findActiveGroupForUser(userId));

    }

    public Optional<GroupDto> findCurrentGroup(long userId) {
        return Optional.ofNullable(groupsMapper.findCurrentGroup(userId));
    }


}
