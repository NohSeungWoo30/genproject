package generationgap.co.kr.mapper;

import generationgap.co.kr.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//예시용 매퍼클래스
@Mapper
public interface UserMapper {
    List<User> getAllUsers();
    void insertUser(User user);

}
