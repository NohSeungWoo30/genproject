package generationgap.co.kr.mapper.group;

import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.domain.group.GroupMembers;
import generationgap.co.kr.domain.group.Groups;
import org.apache.ibatis.annotations.Mapper;

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
    List<Groups> getGroupByGroupDate();
}
