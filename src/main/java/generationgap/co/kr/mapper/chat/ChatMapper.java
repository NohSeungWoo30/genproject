package generationgap.co.kr.mapper.chat;

import generationgap.co.kr.domain.chat.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMapper {
    void insertMessage(ChatMessage message);
    List<ChatMessage> getMessagesByGroup(String groupChatIdx);
}
