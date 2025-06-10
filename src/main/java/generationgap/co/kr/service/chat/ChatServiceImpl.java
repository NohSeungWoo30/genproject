package generationgap.co.kr.service.chat;

import generationgap.co.kr.domain.chat.ChatMessage;
import generationgap.co.kr.exception.AccessDeniedRuntimeException;
import generationgap.co.kr.mapper.chat.ChatMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService{

    private final ChatMapper chatMapper;

    public ChatServiceImpl(ChatMapper chatMapper){
        this.chatMapper = chatMapper;
    }


    @Override
    public void saveMessage(ChatMessage message){
        chatMapper.insertMessage(message);
    }

    @Override
    public List<ChatMessage> getMessagesByGroup(String groupChatIdx){
        return chatMapper.getMessagesByGroup(groupChatIdx);
    }

    // 채팅 수정
    @Override
    public void editMessageWithHistory(int messageId, String newContent, long requestUserIdx, String editedBy){
        //1. 작성자 확인
        int senderIdx = chatMapper.getSenderIdxByMessageId(messageId);
        if(senderIdx != requestUserIdx){
            throw new AccessDeniedRuntimeException("작성자 본인만 수정가능합니다.");
        }
        //2. 이전 내용 조회
        String oldContent = chatMapper.getMessageContentById(messageId);
        //3. 수정 이력 저장
        chatMapper.insertEditHistory(messageId, oldContent, editedBy);
        //4. 메세지 내용 수정
        chatMapper.updateMessageContent(messageId, newContent);
    }//editMessageWithHistory

    // 메세지 삭제
    @Override
    public void deleteMessage(int messageId, long requesterIdx, String requesterId){
        int senderIdx = chatMapper.getSenderIdxByMessageId(messageId);

        if(senderIdx != requesterIdx){
            throw new AccessDeniedRuntimeException("삭제 권한이 없습니다.");
        }
        chatMapper.deleteMessage(messageId);
    }

    @Override
    public List<ChatMessage> searchMessagesByKeyword(String groupId, String keyword){
        return chatMapper.searchMessages(groupId, "%" + keyword + "%");
    }



}
