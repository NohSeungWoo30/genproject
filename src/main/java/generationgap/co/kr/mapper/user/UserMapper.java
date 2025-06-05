package generationgap.co.kr.mapper.user;

import generationgap.co.kr.domain.user.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param; // @Param 사용 시 필요

import java.util.Optional;

//import java.util.Optional; // Optional을 위해 필요

@Mapper
public interface UserMapper {

    void insertUser(UserDTO user);

    UserDTO findByUserId(@Param("userId") String userId); // 사용자 ID로 UserDTO 조회
    // 기존 updateUserPassword는 이제 이 메서드에서 비밀번호 변경만 담당하도록 명확화
    void updateUserPassword(@Param("userIdx") Long userIdx, @Param("passwordHash") String passwordHash);

    @Select("SELECT nickname FROM users WHERE user_id = #{userId}")
    String getNicknameByUserId(String userId);

    @Select("SELECT user_idx FROM users WHERE user_id = #{userId}")
    long getUserIdxByUserId(String userId);

    @Select("SELECT user_id FROM users WHERE user_idx = #{userIdx}")
    String getUserIdByUserIdx(@Param("userIdx") Long userIdx); // int -> Long으로 변경 권장

    // user_idx로 사용자 조회 메서드 추가 (필수)
    UserDTO findByUserIdx(@Param("userIdx") Long userIdx);

    // 회원 일반 정보 수정 메서드 추가
    // UserDTO 객체를 받아서 해당 필드들을 업데이트합니다.
    void updateUserInfo(UserDTO user);

    // 이메일 중복 확인 (선택 사항이지만 유용)
    UserDTO findByEmail(@Param("email") String email);

    // 이름과 전화번호로 사용자 아이디를 찾는 메서드 추가
    Optional<String> findByUserNameAndPhone(UserDTO userDto);
}
