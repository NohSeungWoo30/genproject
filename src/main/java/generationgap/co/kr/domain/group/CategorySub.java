package generationgap.co.kr.domain.group;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySub {

    private int categorySubIdx; // 세부사항 번호
    private int csCategoryMainIdx; // 카테고리 메인 참조 인덱싱
    private String categorySubName; // 세부사항 이름

}
