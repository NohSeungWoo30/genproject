package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Comment;
import generationgap.co.kr.mapper.board.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

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


}
