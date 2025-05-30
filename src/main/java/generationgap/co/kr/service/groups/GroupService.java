package generationgap.co.kr.service.groups;


import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.mapper.group.GroupsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private GroupsMapper groupsMapper;   // GroupMapper 의존성 주입

    public GroupService(GroupsMapper groupsMapper) {
        this.groupsMapper = groupsMapper;
    }

    public List<Groups> getAllGroups(){
        return groupsMapper.getAllGroups();
    }


    /*private List<Groups> cachedGroups;

    public GroupService(GroupsMapper groupsMapper) {
        this.groupsMapper = groupsMapper;    // 생성자 주입
    }

    @PostConstruct
    public void init() {
        cachedGroups = groupsMapper.getAllGroups();  // DB에서 데이터 조회
        System.out.println("초기 그룹 데이터 로딩 완료: " + cachedGroups.size() + "개");
    }

    public List<Groups> getCachedGroups() {
        return cachedGroups;
    }*/



}
