package generationgap.co.kr.service.group;


import generationgap.co.kr.domain.group.CategoryMain;
import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.domain.group.Groups;
import generationgap.co.kr.mapper.group.GroupsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    @Autowired // 생성자 자동생성 의존성 주입
    private GroupsMapper groupsMapper;   // GroupMapper 의존성 주입

    public List<Groups> getAllGroups(){
        return groupsMapper.getAllGroups();
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

}
