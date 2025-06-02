package generationgap.co.kr.mapper.user;

import generationgap.co.kr.domain.User;
import generationgap.co.kr.domain.user.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param; // @Param 사용 시 필요

import java.util.List;


@Mapper
public interface UserMapper {
    List<User> getAllUsers();
    void insertUser(UserDTO user);

    UserDTO findByUserId(@Param("userId") String userId); // 사용자 ID로 UserDTO 조회

    void updateUserPassword(UserDTO user);

    @Select("SELECT nickname FROM users WHERE user_id = #{userId}")
    String getNicknameByUserId(String userId);

    @Select("SELECT user_idx FROM users WHERE user_id = #{userId}")
    int getUserIdxByUserId(String userId);

    @Select("SELECT user_id FROM users WHERE user_idx = #{userIdx}")
    String getUserIdByUserIdx(int userIdx);

}
