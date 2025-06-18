package generationgap.co.kr.mapper.group;

import generationgap.co.kr.domain.group.*;
import generationgap.co.kr.dto.group.GroupDto;
import generationgap.co.kr.dto.group.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupsMapper {

    // 그룹 전체 리스트
    List<Groups> getAllGroups();
    // 메인카테고리 리스트
    List<CategoryMain> getAllMainCategory();
    // 메인에포함 된 서브 카테고리 리스트
    List<CategorySub> getAllSubCategory(int mainCategoryIdx);
    // 좋아요를 받은 호스트가 만든방 리스트(메인 인기소셜링용)
    List<Groups> getRecommendGroup();
    // 모임(그룹)방 생성
    void insertGroup(Groups groups);
    // 모임 멤버 리스트 생성
    void insertHostMember(GroupMembers hostMember);
    // 특정그룹 번호의 그룹정보
    Groups getGroupById(int groupId);
    // 인기 카테고리(장르) 모음
    List<Groups> getGroupByCategory();
    // 최근 생성날 기준 모임
    List<Groups> getGroupByCreateDate();
    // 모임일 시작 일 임박 기준
    List<Groups> getGroupByGroupDate();
    // 모임리스트 필터 리스트
    List<Groups> getfiterGroupList(SearchFilterRequest request);



    List<Groups> getMatchedGroups(@Param("categories") List<Integer> categories,
                                  @Param("minAge") int minAge,
                                  @Param("maxAge") int maxAge,
                                  @Param("gender") String gender);

    List<GroupMembers> getGroupMembersByGroupId(@Param("groupId") int groupId);

    void insertGroupMember(@Param("groupIdx") Long groupIdx,
                           @Param("userIdx") Long userIdx,
                           @Param("nickname") String nickname);

    int isAlreadyMember(@Param("groupIdx") Long groupIdx,
                        @Param("userIdx") Long userIdx);

    void deleteGroupMember(@Param("groupIdx") Long groupIdx, @Param("userIdx") Long userIdx);


    // 그룹 기본 정보 + host 닉네임, 이미지 포함 조회
    GroupDto findGroupById(@Param("groupIdx") int groupIdx);

    // 그룹 참여자 목록 조회 (nickname + avatar)
    List<MemberDto> findParticipantsByGroupIdx(@Param("groupIdx") int groupIdx);

    GroupDto findActiveGroupForUser(@Param("userId") Long userId);

    GroupDto findGroupDetail(@Param("groupIdx") int groupIdx);

    int increasePartyMember(@Param("groupIdx") Long groupIdx);

    GroupDto findCurrentGroup(@Param("userId") long userId);

}

