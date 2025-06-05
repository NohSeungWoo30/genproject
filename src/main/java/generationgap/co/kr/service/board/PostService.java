package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.post.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    List<Post> getPostListPaged(int offset, int limit);

    int getTotalPostCount();

    void writePostWithAttachments(Post post, List<MultipartFile> files);

    Post getPostById(int postIdx);

    void incrementViewCount(int postIdx);

    boolean toggleLikePost(int UserIdx, int postIdx);

    void softDeletePost(int postIdx, int userIdx);

    List<Attachment> getAttachmentsByPostId(Long postIdx);

    void updatePost(Post post, List<MultipartFile> files);
}

