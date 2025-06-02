package generationgap.co.kr.domain.group;

import generationgap.co.kr.domain.user.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Groups {
    private int groupIdx;
    private int ownerIdx;
    private int groupCategoryMainIdx;
    private int groupCategorySubIdx;
    private String title;
    private String genderLimit;
    private int ageMin;
    private int ageMax;
    private String groupDate;
    private int membersMax;
    private int partyMember;
    private String content;
    private String placeName;
    private String placeCategory;
    private String placeAddress;
    private String naverPlaceId;
    private String naverPlaceUrl;
    private int latitude;
    private int longitude;
    private String groupImgUrl;
    private String groupsStatus;
    private String createdAt;
    private String deletedAt;

    // 조인용 객체 선언
    private User owner;                     // 호스트
    private CategoryMain categoryMain;     // 대분류
    private CategorySub categorySub;       // 소분류

}
