package generationgap.co.kr.mapper.post;

import generationgap.co.kr.domain.mypage.PostDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BoardPostMapper {
    List<PostDto> findPostsByAuthorIdx(Long authorIdx);
}