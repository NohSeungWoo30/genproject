package generationgap.co.kr.handler.chat;

import generationgap.co.kr.domain.chat.ChatMessage;
import generationgap.co.kr.mapper.UserMapper;
import generationgap.co.kr.service.chat.ChatService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ChatHandler extends TextWebSocketHandler {

    //groupId ->[WebSocketSession, ...]
    private final Map<String, Set<WebSocketSession>> groupSessions = new HashMap<>();

    private final ChatService chatService;

    @Autowired
    private UserMapper userMapper;

    public ChatHandler(ChatService chatService, UserMapper userMapper){
        this.chatService = chatService;
        this.userMapper = userMapper;
    }



    private String getNickname(String userId){
        return userMapper.getNicknameByUserId(userId);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        String groupId = extractParam(session, "groupId");
        groupSessions.computeIfAbsent(groupId, k-> new HashSet<>()).add(session);
        System.out.println("✅ WebSocket 연결됨: groupId = " + groupId);

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        String payload = message.getPayload();
        JSONObject json = new JSONObject(payload);
        String msg = json.getString("msg");

        // 1. 데이터 파싱
        String groupId = extractParam(session, "groupId");
        String userId = extractParam(session, "userId");


        // 2. 닉네임 조회
        String nickname = userMapper.getNicknameByUserId(userId);


        // 3. DB저장
        int userIdx = userMapper.getUserIdxByUserId(userId);



        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setGroupChatIdx(groupId);
        chatMessage.setSenderIdx(userIdx);
        chatMessage.setNickname(nickname);
        chatMessage.setContent(msg);
        chatMessage.setSentAt(LocalDateTime.now());
        chatMessage.setIsDeleted("N");
        chatService.saveMessage(chatMessage);

        // 4. 같은 그룹(groupId) 세션에 브로드캐스트
        JSONObject response = new JSONObject();
        response.put("from", nickname);
        response.put("msg", msg);

        for(WebSocketSession s : groupSessions.getOrDefault(groupId, Set.of())){
            if(s.isOpen()){
                s.sendMessage(new TextMessage(response.toString()));
            }
        }


    }//handleTextMessage

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        String groupId = extractParam(session, "groupId");
        Set<WebSocketSession> sessions = groupSessions.get(groupId);
        if(sessions != null){
            sessions.remove(session);
            if(sessions.isEmpty()){
                groupSessions.remove(groupId);
            }
        }
        System.out.println("❌ 연결 종료: groupId = " + groupId);
    }

    private String extractParam(WebSocketSession session, String paramName) {
        try {
            String query = session.getUri().getQuery();
            for (String param : query.split("&")) {
                if (param.startsWith(paramName + "=")) {
                    return param.split("=")[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

}
