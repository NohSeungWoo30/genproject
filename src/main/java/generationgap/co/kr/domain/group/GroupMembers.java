package generationgap.co.kr.domain.group;

import generationgap.co.kr.domain.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GroupMembers {

    int membersIdx; // 자동넘버
    int groupIdx; // 그룹방 넘버
    int userIdx; // 호스트(유저)번호
    String nickName; // 닉네임
    String isConfirmed; // Y or N
    LocalDateTime joinedAt; // 날짜



    private UserDTO user; //GroupMembers에 UserDTO user를 조인해서 받아오기 위해 추가

}
