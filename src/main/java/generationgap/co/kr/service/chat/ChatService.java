package generationgap.co.kr.service.chat;

import generationgap.co.kr.domain.chat.ChatMessage;
import generationgap.co.kr.mapper.chat.ChatMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatMapper chatMapper;

    public ChatService(ChatMapper chatMapper){
        this.chatMapper = chatMapper;
    }

    public void saveMessage(ChatMessage message){
        chatMapper.insertMessage(message);
    }

    public List<ChatMessage> getMessagesByGroup(String groupChatIdx){
        return chatMapper.getMessagesByGroup(groupChatIdx);
    }

}
