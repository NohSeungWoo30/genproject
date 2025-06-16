package generationgap.co.kr.mapper.board;

import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.post.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
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

    //첨부파일 추가
    void insertAttachmentWithOriginal(Attachment attachment);

    List<Attachment> getAttachmentsByPostId(Long postIdx);

    void updatePost(Post post);

    void deleteAttachmentsByPostId(Long postIdx);

    void insertPostEditHistory(@Param("postIdx") int postIdx,
                               @Param("titleBefore") String titleBefore,
                               @Param("contentBefore") String contentBefore,
                               @Param("editedBy") int editedBy);

    int getVisibleCommentCountByPost(@Param("postIdx") int postIdx);

    List<Post> getPostsPagedByViews(@Param("offset") int offset, @Param("limit") int limit);
    List<Post> getPostsPagedByLikes(@Param("offset") int offset, @Param("limit") int limit);
    List<Post> getPostsPagedDefault(@Param("offset") int offset, @Param("limit") int limit);

    // 정렬 + 카테고리 필터
    List<Post> getPostsPagedFiltered(@Param("offset") int offset,
                                     @Param("limit") int limit,
                                     @Param("category") String category,
                                     @Param("sort") String sort,
                                     @Param("keyword")String keyword);


    // 필터링된 게시글 수 계산
    int getPostCountFiltered(@Param("category") String category,
                             @Param("keyword")String keyword);


}
