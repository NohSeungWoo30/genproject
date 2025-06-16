package generationgap.co.kr.service.group;


import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.domain.group.GroupMembers;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.mapper.group.GroupsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired // 생성자 자동생성 의존성 주입
    private GroupsMapper groupsMapper;   // GroupMapper 의존성 주입

    public List<Groups> getAllGroups(){
        return groupsMapper.getAllGroups();
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

}
