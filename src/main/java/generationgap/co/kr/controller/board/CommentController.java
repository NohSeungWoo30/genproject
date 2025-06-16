package generationgap.co.kr.controller.board;

import generationgap.co.kr.domain.board.Comment;
import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.notification.NotificationDto;
import generationgap.co.kr.mapper.board.CommentMapper;
import generationgap.co.kr.mapper.board.PostMapper;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.board.CommentService;
import generationgap.co.kr.service.notification.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
@Slf4j
public class CommentController {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final NotificationService notificationService;
    private final CommentService commentService;


    //댓글 등록
    @PostMapping("/{postIdx}/comments")
    @ResponseBody
    public String addComment(@PathVariable int postIdx,
                             @RequestParam(required = false) Long parentCommentId,
                             @RequestParam String content,
                             HttpSession session){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if(!(principal instanceof CustomUserDetails)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "로그인이 필요합니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        int userIdx = userDetails.getUserIdx().intValue();

        Comment comment = new Comment();
        comment.setPostIdx(postIdx);
        comment.setCommenterIdx(userIdx);
        comment.setContent(content);
        comment.setParentCommentId(parentCommentId);

        commentService.addComment(comment, userIdx);

        // 2. 알림 전송
        try {

            System.out.println("컨트롤러에서 sendNotification 진입");

            Post post = postMapper.getPostById(postIdx);
            if (post != null && post.getAuthorIdx() != userIdx) {
                NotificationDto dto = new NotificationDto();
                dto.setRecipientId(post.getAuthorIdx().longValue());
                dto.setNotiTypeIdx(1L);
                dto.setVariables(Map.of("title", post.getTitle()));
                dto.setNotiUrl("/posts/" + postIdx);

                notificationService.sendNotification(dto);
            }
        } catch (Exception e) {
            log.error("❗ 댓글 알림 처리 중 오류 발생", e);
        }
        return "ok";
    }

    // 댓글 삭제
    @PostMapping("/comments/{commentIdx}/delete")
    @ResponseBody
    public String deleteComment(@PathVariable int commentIdx, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "로그인이 필요합니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        int userIdx = userDetails.getUserIdx().intValue();

        //댓글 정보 조회
        Comment comment = commentMapper.getCommentById(commentIdx);
        if(comment == null || "Y".equals(comment.getIsDeleted())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글이 존재하지 않거나 이미 삭제되었습니다.");
        }

        //작성자 검사
        Integer authorIdx = comment.getCommenterIdx();
        if(authorIdx ==null || !authorIdx.equals(userIdx)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 삭제할 수 있습니다.");
        }

        commentMapper.softDeleteComment(commentIdx, userIdx);
        return "ok";
    }

    @PostMapping("/comments/{commentIdx}/edit")
    @ResponseBody
    public Map<String, Object> editComment(@PathVariable int commentIdx,
                                           @RequestBody Map<String, String> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        int userIdx = userDetails.getUserIdx().intValue();

        Comment original = commentMapper.getCommentById(commentIdx);
        if (original == null || "Y".equals(original.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 댓글입니다.");
        }
        if (original.getCommenterIdx() !=userIdx) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 댓글만 수정할 수 있습니다.");
        }


        String newContent = payload.get("content");

        commentMapper.updateCommentContent(commentIdx, newContent);
        // 기존 내용 수정 이력 테이블에 기록
        commentMapper.insertCommentEdit(commentIdx, original.getContent(), userIdx);

        Comment updated = commentMapper.getCommentById(commentIdx);


        String formattedTime = "";
        if (updated.getUpdateAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            formattedTime = updated.getUpdateAt().format(formatter);
        }

        return Map.of("updatedContent", newContent,
                "formattedDisplayTime", formattedTime,
                "isEdited", true);
    }

}
