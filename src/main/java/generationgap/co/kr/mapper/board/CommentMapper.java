package generationgap.co.kr.mapper.board;

import generationgap.co.kr.domain.board.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> getCommentsByPost(int postIdx);
    void insertComment(Comment comment);
    void softDeleteComment(@Param("commentIdx") int commentIdx, @Param("userIdx") int userIdx);
}
