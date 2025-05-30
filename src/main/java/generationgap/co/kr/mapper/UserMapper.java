package generationgap.co.kr.mapper;

import generationgap.co.kr.domain.user.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserMapper {
    List<Users> getAllUsers();
    void insertUser(Users user);

    @Select("SELECT nickname FROM users WHERE user_id = #{userId}")
    String getNicknameByUserId(String userId);

    @Select("SELECT user_idx FROM users WHERE user_id = #{userId}")
    int getUserIdxByUserId(String userId);
}
