package generationgap.co.kr.mapper.chat;

import generationgap.co.kr.domain.chat.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMapper {
    void insertMessage(ChatMessage message);
    List<ChatMessage> getMessagesByGroup(String groupChatIdx);

    // 작성자 확인
    int getSenderIdxByMessageId(@Param("messageId") int messageId);
    // 기존 메세지 확인
    String getMessageContentById(@Param("messageId") int MessageId);
    // 수정 이력 저장
    void insertEditHistory(@Param("messageId") int messageId,
                           @Param("oldContent") String oldContent,
                           @Param("editedBy") String editedBy);
    // 메세지 수정
    void updateMessageContent(@Param("messageId") int messageId,
                              @Param("newContent") String newContent);
    // 메세지 삭제
    int deleteMessage(@Param("messageId") int messageId);

    // 메세지 검색
    List<ChatMessage> searchMessages(@Param("groupId") String groupId,
                                     @Param("keyword") String keyword);

}
