package generationgap.co.kr.mapper.board;

import generationgap.co.kr.domain.board.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> getAllPosts();
}
