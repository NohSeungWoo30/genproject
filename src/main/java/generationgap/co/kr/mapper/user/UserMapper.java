package generationgap.co.kr.mapper.user;

import generationgap.co.kr.domain.mypage.UpdateInfoDTO; // UpdateInfoDTO 임포트
import generationgap.co.kr.domain.user.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param; // @Param 사용 시 필요

import java.util.List;


@Mapper
public interface UserMapper {

    void insertUser(UserDTO user);

    UserDTO findByUserId(@Param("userId") String userId); // 사용자 ID로 UserDTO 조회

    void updateUserInfo(UpdateInfoDTO dto);
    void updateUserPassword(UserDTO user);
    void updateUserProfileImage(UserDTO user);

    @Select("SELECT nickname FROM users WHERE user_id = #{userId}")
    String getNicknameByUserId(String userId);

    @Select("SELECT user_idx FROM users WHERE user_id = #{userId}")
    int getUserIdxByUserId(String userId);

    @Select("SELECT user_id FROM users WHERE user_idx = #{userIdx}")
    String getUserIdByUserIdx(int userIdx);

    // user_idx로 사용자 조회 메서드 추가 (필수)
    UserDTO findByUserIdx(@Param("userIdx") Long userIdx);

}
