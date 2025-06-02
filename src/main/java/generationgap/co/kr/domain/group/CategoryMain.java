package generationgap.co.kr.domain.group;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryMain {

    private int cmCategoryMainIdx; // PRIMARY KEY
    private String categoryMainName; // NOT NULL UNIQUE

}
