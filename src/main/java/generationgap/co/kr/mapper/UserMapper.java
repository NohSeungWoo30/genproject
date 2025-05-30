package generationgap.co.kr.mapper;

import generationgap.co.kr.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserMapper {
    List<User> getAllUsers();
    void insertUser(User user);

    @Select("SELECT nickname FROM users WHERE user_id = #{userId}")
    String getNicknameByUserId(String userId);

    @Select("SELECT user_idx FROM users WHERE user_id = #{userId}")
    int getUserIdxByUserId(String userId);

    @Select("SELECT user_id FROM users WHERE user_idx = #{userIdx}")
    String getUserIdByUserIdx(int userIdx);

}
