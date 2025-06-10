package generationgap.co.kr.service.chat;

import generationgap.co.kr.domain.chat.ChatMessage;

import java.util.List;


public interface ChatService {

    void saveMessage(ChatMessage message);

    List<ChatMessage> getMessagesByGroup(String groupChatIdx);

    void editMessageWithHistory(int messageId, String newContent, long requestUserIdx, String editedBy);

    void deleteMessage(int messageId, long requesterIdx, String requesterId);

    List<ChatMessage> searchMessagesByKeyword(String groupId, String keyword);
}
