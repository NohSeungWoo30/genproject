package generationgap.co.kr.controller.board;

import generationgap.co.kr.domain.board.Comment;
import generationgap.co.kr.mapper.board.CommentMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class CommentController {

    private final CommentMapper commentMapper;


    //댓글 등록
    @PostMapping("/{postIdx}/comments")
    @ResponseBody
    public String addComment(@PathVariable int postIdx,
                             @RequestParam(required = false) Long parentCommentId,
                             @RequestParam String content,
                             HttpSession session){
        int userIdx = 1; //로그인 세션 처리 전 까지 고정으로 테스트용

        Comment comment = new Comment();
        comment.setPostIdx(postIdx);
        comment.setCommenterIdx(userIdx);
        comment.setContent(content);
        comment.setParentCommentId(parentCommentId);

        commentMapper.insertComment(comment);
        return "ok";
    }

    // 댓글 삭제
    @PostMapping("/comments/{commentIdx}/delete")
    @ResponseBody
    public String deleteComment(@PathVariable int commentIdx, HttpSession session) {
        int userIdx = 1; // 로그인 세션 도입 전까진 고정
        commentMapper.softDeleteComment(commentIdx, userIdx);
        return "ok";
    }

}
