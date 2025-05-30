/*
package generationgap.co.kr.controller.group;


import generationgap.co.kr.domain.group.CategorySub;
import generationgap.co.kr.service.group.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // 이 클래스의 메서드들이 RESTful API를 제공함을 선언
@RequestMapping("/api/categories") // API 엔드포인트의 기본 경로
public class GroupApiController {

    @Autowired
    private final GroupService groupService;

    public GroupApiController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 메인 카테고리 ID에 해당하는 서브 카테고리 목록을 JSON으로 반환
    // 예: GET /api/categories/sub?mainCategoryIdx=1
    @GetMapping("/sub")
    public ResponseEntity<List<CategorySub>> getSubCategoriesByMainIdx(
            @RequestParam("mainCategoryIdx") int mainCategoryIdx) {
        List<CategorySub> subCategories = groupService.getAllSubCategory(mainCategoryIdx);
        return ResponseEntity.ok(subCategories); // HTTP 200 OK와 함께 JSON 데이터 반환
    }
}
*/
