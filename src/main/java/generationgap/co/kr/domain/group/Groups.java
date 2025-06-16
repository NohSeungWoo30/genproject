package generationgap.co.kr.domain.group;

import generationgap.co.kr.domain.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Groups {
    private int groupIdx;
    private Long ownerIdx;
    private int groupCategoryMainIdx;
    private int groupCategorySubIdx;
    private String title;
    private String genderLimit;
    private int ageMin;
    private int ageMax;
    private LocalDateTime groupDate;
    private int membersMin;
    private int membersMax;
    private int partyMember;
    private String content;

    // 지도 정보
    private String placeName; // 장소명
    private String placeCategory; // 분류
    private String placeAddress; // 주소(도로명) 하나만
    private String naverPlaceId; // 이거는 힘들듯
    private String naverPlaceUrl; // 링크주소(홈페이지, 네이버 플레이스)
    private double latitude; // 위도
    private double longitude; // 경도

    private String groupImgUrl; // 이미지 참조용
    private String groupsStatus;
    private String createdAt;
    private String deletedAt;

    // 조인용 객체 선언
    private UserDTO owner;                     // 호스트
    private CategoryMain categoryMain;     // 대분류
    private CategorySub categorySub;       // 소분류

}
