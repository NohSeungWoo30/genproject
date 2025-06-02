package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Post;

import java.util.List;

public interface PostService {
    List<Post> getPostListPaged(int offset, int limit);

    int getTotalPostCount();

    void savePost(Post post);

    Post getPostById(int postIdx);

    void incrementViewCount(int postIdx);

    boolean toggleLikePost(int UserIdx, int postIdx);

    void softDeletePost(int postIdx, int userIdx);
}

