package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Comment;
import java.util.List;

public interface CommentService {
    List<Comment> getFilteredCommentsByPost(int postIdx);

}