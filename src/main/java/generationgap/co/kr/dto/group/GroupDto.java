package generationgap.co.kr.dto.group;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GroupDto {

    private int groupIdx;
    private String title;
    private String content;
    private String groupImgUrl;

    // 🙋‍♂️ 모임장
    private String hostNickname;
    private String hostAvatar;

    // 🕒 일정·인원
    private LocalDateTime groupDate;
    private int membersMin;      // ← 추가
    private int membersMax;
    private int partyMember;

    // 📍 장소
    private String placeAddress;     // ← 추가
    private String placeName;   // ← 추가

    private List<MemberDto> participants;
}
