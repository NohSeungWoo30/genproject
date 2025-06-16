package generationgap.co.kr.mapper.user;

import generationgap.co.kr.domain.user.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param; // @Param 사용 시 필요

import java.util.List;
import java.util.Optional;


@Mapper
public interface UserMapper {

    void insertUser(UserDTO user);

    UserDTO findByUserId(@Param("userId") String userId); // 사용자 ID로 UserDTO 조회
    // 기존 updateUserPassword는 이제 이 메서드에서 비밀번호 변경만 담당하도록 명확화
    void updateUserPassword(@Param("userIdx") Long userIdx, @Param("passwordHash") String passwordHash);

    @Select("SELECT nickname FROM users WHERE user_id = #{userId} AND user_status = 'ACTIVE'")
    String getNicknameByUserId(String userId);

    @Select("SELECT user_idx FROM users WHERE user_id = #{userId} AND user_status = 'ACTIVE'")
    long getUserIdxByUserId(String userId);

    @Select("SELECT user_id FROM users WHERE user_idx = #{userIdx} AND user_status = 'ACTIVE'")
    String getUserIdByUserIdx(@Param("userIdx") Long userIdx); // int -> Long으로 변경 권장

    // user_idx로 사용자 조회 메서드 추가 (필수)
    UserDTO findByUserIdx(@Param("userIdx") Long userIdx);

    UserDTO findByOAuth2UserId(@Param("userId") String userId);

    // 회원 일반 정보 수정 메서드 추가
    // UserDTO 객체를 받아서 해당 필드들을 업데이트합니다.
    void updateUserInfo(UserDTO user);

    // 이메일 중복 확인 (선택 사항이지만 유용)
    UserDTO findByEmail(@Param("email") String email);

    // 이름과 전화번호로 사용자 아이디를 찾는 메서드 추가
    String findByUserNameAndPhone(UserDTO userDto);

    // 소프트 삭제 로직에서 사용될, user_status에 관계없이 사용자 정보를 조회하는 메서드 추가
    // 이 메서드는 UNIQUE 컬럼 값을 변경하기 전에 현재 값을 가져오는 데 사용됩니다.
    UserDTO findByUserIdForAuthentication(@Param("userId") String userId);

    // ⭐⭐ 소프트 삭제 메서드: userStatus를 'DELETED'로, UNIQUE 필드와 ghost 필드 업데이트 ⭐⭐
    // UserDTO 객체를 통째로 받아 MyBatis XML에서 업데이트하도록 설정합니다.
    void softDeleteUser(UserDTO user);

    // 새롭게 추가할 중복 확인 메서드들
    int countByUserId(@Param("userId") String userId);
    int countByNickname(@Param("nickname") String nickname);
    int countByEmail(@Param("email") String email);
    int countByPhone(@Param("phone") String phone);
    int countByUserCi(@Param("userCi") String userCi);

    // OAuth2 연동을 위한 신규 메서드 추가
    // provider와 userId(구글의 'sub')로 사용자 조회
    UserDTO findByProviderAndUserId(@Param("provider") String provider, @Param("userId") String userId);

    void insertOAuthUser(UserDTO user);

    void updateProfileName(@Param("userIdx") Long userIdx, @Param("profileName") String profileName);

    // 신고된 유저 설정을 위해 추가 ksm
    void suspendUser(@Param("userId") Long userId);

    // 신고된 유저 자동 해제 ksm
    int releaseExpiredSuspensions();


    //테스트용 ksm
    List<UserDTO> findSuspendCandidates();

    /*//user_idx로 조회하는 매서드 추가 ksm
    UserDTO findByUserIdx(@Param("userIdx") Long userIdx);
*/
}
