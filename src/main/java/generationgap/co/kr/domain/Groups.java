package generationgap.co.kr.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Groups {
    group_idx NUMBER,
    owner_idx NUMBER,
    category_main_idx NUMBER,
    category_sub_idx NUMBER,
    title String,
    gender_limit String,
    age_min NUMBER,
    age_max NUMBER,
    group_date DATE NOT NULL,
    members_max NUMBER NOT NULL,
    party_member NUMBER,
    content VARCHAR2(1000),
    place_name VARCHAR2(255),
    place_category VARCHAR2(100),
    place_address VARCHAR2(500),
    naver_place_id VARCHAR2(50),
    naver_place_url VARCHAR2(1000),
    latitude NUMBER,
    longitude NUMBER,
    group_img_url VARCHAR2(1000),
    groups_status VARCHAR2(20) DEFAULT 'RECRUITING' CONSTRAINT CHK_GROUP_STATUS CHECK (groups_status IN ('RECRUITING', 'COMPLETED', 'CLOSED', 'CANCELLED')),
    created_at DATE DEFAULT SYSDATE NOT NULL,
    deleted_at DATE
}
