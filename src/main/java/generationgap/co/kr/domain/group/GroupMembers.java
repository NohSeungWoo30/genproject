package generationgap.co.kr.domain.group;

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

}
