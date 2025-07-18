package generationgap.co.kr.mapper.board;

import generationgap.co.kr.domain.board.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> getPostsPaged(int offset, int limit);

    int getPostCount();

    void insertPost(Post post);

    Post getPostById(int postIdx);

    void updateViewCount(int postIdx);

    // 좋아요(추천)
    void insertPostLikeCheck(@Param("userIdx") int userIdx, @Param("postIdx") int postIdx);
    void updateLikeCount(int postIdx);

    int hasUserLikedPost(@Param("userIdx")int userIdx, @Param("postIdx")int postIdx);
    void decrementLikeCount(int postIdx);
    void deletePostLike(@Param("userIdx")int userIdx, @Param("postIdx")int postIdx);

    //삭제
    void softDeletePost(@Param("postIdx") int postIdx, @Param("userIdx") int userIdx);
}
