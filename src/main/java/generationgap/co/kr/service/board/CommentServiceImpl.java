package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Comment;
import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.notification.NotificationDto;
import generationgap.co.kr.mapper.board.CommentMapper;
import generationgap.co.kr.mapper.board.PostMapper;
import generationgap.co.kr.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    private final PostMapper postMapper;


    @Override
    public List<Comment> getFilteredCommentsByPost(int postIdx) {
        List<Comment> all = commentMapper.getCommentsByPost(postIdx);

        //날짜 포맷터 준비
        DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // commentIdx → Comment 매핑
        Map<Long, Comment> commentMap = all.stream()
                .peek(c -> {
                    LocalDateTime baseTime = null;
                    if (c.getUpdateAt() != null) {
                        baseTime = c.getUpdateAt();//수정 시간 우선
                        c.setEdited(true);       // ✅ 수정됨 표시

                    }else if(c.getCreatedAt()!=null){
                        baseTime = c.getCreatedAt();//작성 시간

                    }
                    if (baseTime !=null){
                        c.setFormattedDisplayTime(baseTime.format(formatter));

                    }
                })
                .collect(Collectors.toMap(Comment::getCommentIdx, c -> c));

        return all.stream()
                .filter(c -> {
                    if ("Y".equals(c.getIsDeleted())) return true; // 삭제된 댓글은 표시만

                    Long parentId = c.getParentCommentId();
                    if (parentId != null) {
                        Comment parent = commentMap.get(parentId);
                        // 🔥 부모가 삭제된 대댓글은 제외!
                        return parent != null && !"Y".equals(parent.getIsDeleted());
                    }

                    return true; // 원댓글은 살아있으면 표시
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void addComment(Comment comment, int currentUserId) {
        commentMapper.insertComment(comment);  // DB 저장
        int postId = comment.getPostIdx(); //게시글 ID 확보하기

        // 알림 대상 결정
        if (comment.getParentCommentId() == null) {
            // 원댓글 → 게시글 작성자에게 알림
            int postWriterId = commentMapper.getPostWriterByPostIdx(comment.getPostIdx());
            if (postWriterId != currentUserId) {
                sendNotificationTo(postWriterId, postId, "/posts/" + comment.getPostIdx());
                System.out.println("🎯 대상은 게시글 작성자");

            }
        } else {
            // 대댓글 → 부모 댓글 작성자에게 알림
            Comment parent = commentMapper.getCommentById(comment.getParentCommentId().intValue());
            if (parent != null && parent.getCommenterIdx() != currentUserId) {
                sendNotificationTo(parent.getCommenterIdx(), postId, "/posts/" + comment.getPostIdx());
                System.out.println("🎯 대상은 부모 댓글 작성자");

            }
        }
    }

    private void sendNotificationTo(int recipientId, int postId, String url) {
        NotificationDto dto = new NotificationDto();
        Post post = postMapper.getPostById(postId);

        dto.setRecipientId((long) recipientId);     // 수신자: 유저 ID
        dto.setNotiTypeIdx(1L);                     // 예: 댓글 알림
        dto.setNotiUrl(url);
        dto.setVariables(Map.of("title", post.getTitle())); // 실제 게시글 제목 치환

        notificationService.sendNotification(dto);
    }


}
