package generationgap.co.kr.mapper.user;

import generationgap.co.kr.domain.user.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserMapper {
    void insertUser(UserDTO user);

    UserDTO findByUserId(@Param("userId") String userId); // 사용자 ID로 UserDTO 조회

    void updateUserPassword(UserDTO user);

    @Select("SELECT nickname FROM users WHERE user_id = #{userId}")
    String getNicknameByUserId(String userId);

    @Select("SELECT user_idx FROM users WHERE user_id = #{userId}")
    Long getUserIdxByUserId(String userId);

    @Select("SELECT user_id FROM users WHERE user_idx = #{userIdx}")
    String getUserIdByUserIdx(long userIdx);


    // 신고된 유저 설정을 위해 추가 ksm
    void suspendUser(@Param("userId") Long userId);

    // 신고된 유저 자동 해제 ksm
    int releaseExpiredSuspensions();


    //테스트용 ksm
    List<UserDTO> findSuspendCandidates();

    //user_idx로 조회하는 매서드 추가 ksm
    UserDTO findByUserIdx(@Param("userIdx") Long userIdx);




}
