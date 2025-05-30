package generationgap.co.kr.mapper.group;

import generationgap.co.kr.domain.group.Groups;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupsMapper {

    // 그룹 전체 리스트
    List<Groups> getAllGroups();

}
