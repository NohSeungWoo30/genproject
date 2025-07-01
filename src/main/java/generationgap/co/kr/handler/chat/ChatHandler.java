package generationgap.co.kr.handler.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import generationgap.co.kr.domain.chat.ChatMessage;
import generationgap.co.kr.mapper.user.UserMapper;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.chat.ChatService;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    //groupId -> 세션들
    private final Map<String, Set<WebSocketSession>> groupSessions = new HashMap<>();

    public ChatHandler(ChatService chatService, UserMapper userMapper) {
        this.chatService = chatService;
        this.userMapper = userMapper;
    }


    private String getParam(WebSocketSession session, String key) {
        try {
            String query = session.getUri().getQuery();
            for (String param : query.split("&")) {
                if (param.startsWith(key + "=")) {
                    return param.split("=")[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }//getParam

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String groupId = getParam(session, "groupId");

        SecurityContext context = (SecurityContext) session.getAttributes().get("SPRING_SECURITY_CONTEXT");
        if (context == null || context.getAuthentication() == null) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"세션에 인증 정보 없음\"}"));
            session.close();
            return;
        }

        Authentication auth = context.getAuthentication();
        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails customUser)) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"인증된 사용자 아님\"}"));
            session.close();
            return;
        }

        Long userIdx = customUser.getUserIdx();
        String nickname = customUser.getNickname();
        String userId = customUser.getUsername();


        // 1. 세션 등록
        groupSessions.computeIfAbsent(groupId, k-> new HashSet<>()).add(session);
        System.out.println("WebSocket 연결됨: groupId = " + groupId);

        //먼저 현재 사용자 식별 메세지 전송하기
        JSONObject identityMsg = new JSONObject();
        identityMsg.put("type", "IDENTIFY");
        identityMsg.put("userId", userId); // 현재 접속자 ID
        identityMsg.put("userIdx", userIdx);
        session.sendMessage(new TextMessage(identityMsg.toString()));

        // 2. 과거 메세지 조회
        List<ChatMessage> history = chatService.getMessagesByGroup(groupId);
        for (ChatMessage msg : history){
            JSONObject json = new JSONObject();
            json.put("type", "INIT");
            json.put("from", msg.getNickname());
            json.put("msg", msg.getContent());
            String msgUserId = userMapper.getUserIdByUserIdx(msg.getSenderIdx());
            json.put("userId", msgUserId);
            json.put("userIdx", msg.getSenderIdx()); // 숫자형 고유 ID 추가
            json.put("messageId", msg.getMessagesIdx());
            json.put("sentAt", msg.getSentAt().toString());
            json.put("isDeleted", msg.getIsDeleted());
            json.put("isEdited", msg.getIsEdited());

            session.sendMessage(new TextMessage(json.toString()));
        }

/*
        sendSystemMessage(groupId, nickname + " 님이 입장하셨습니다.");
*/

    }//afterConnectionEstablished


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        String payload = message.getPayload();
        JSONObject json = new JSONObject(payload);


        // 1. 데이터 파싱
        String groupId = getParam(session, "groupId");

        // 인증 정보 수동 추출 (Spring Security 6.x 기준)
        SecurityContext context = (SecurityContext) session.getAttributes().get("SPRING_SECURITY_CONTEXT");
        if (context == null || context.getAuthentication() == null) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"세션에 인증 정보 없음\"}"));
            return;
        }

        Authentication auth = context.getAuthentication();
        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails customUser)) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"인증된 사용자 아님\"}"));
            return;
        }

        Long userIdx = customUser.getUserIdx();
        String nickname = customUser.getNickname();
        String userId = customUser.getUsername();


        //EDIT 분기
        if (json.has("type") && json.getString("type").equals("EDIT")) {
            int messageId = json.getInt("messageId");
            String newContent = json.getString("newContent");

            try {
                chatService.editMessageWithHistory(messageId, newContent, userIdx, userId);

                JSONObject response = new JSONObject();
                response.put("type", "EDIT");
                response.put("messageId", messageId);
                response.put("newContent", newContent);
                response.put("editedAt", LocalDateTime.now().toString());
                // 채팅 수정 후 받지 못한 부분 추가
                response.put("from",  nickname); // userMapper.getNicknameByUserId(userId)); // 닉네임
                response.put("msg", newContent); // 수정된 내용
                response.put("userId", userId); // 클라이언트 비교용
                response.put("userIdx", userIdx);     // IDX 추가
                response.put("isEdited", "Y");




                for (WebSocketSession s : groupSessions.getOrDefault(groupId, Set.of())) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(response.toString()));
                    }
                }
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("type", "ERROR");
                error.put("message", "메시지를 수정할 권한이 없습니다.");
                session.sendMessage(new TextMessage(error.toString()));
            }
            return;
        }

        //DELETE 분기
        if (json.has("type") && json.getString("type").equals("DELETE")) {
            int messageId = json.getInt("messageId");

            try {
                chatService.deleteMessage(messageId, userIdx, userId);

                JSONObject response = new JSONObject();
                response.put("type", "DELETE");
                response.put("messageId", messageId);
                response.put("msg", "삭제된 메시지입니다.");
                response.put("from", nickname); //userMapper.getNicknameByUserId(userId));
                response.put("userId", userId);
                response.put("userIdx", userIdx);     // IDX 추가


                for (WebSocketSession s : groupSessions.getOrDefault(groupId, Set.of())) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(response.toString()));
                    }
                }
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("type", "ERROR");
                error.put("message", "메시지를 삭제할 권한이 없습니다.");
                session.sendMessage(new TextMessage(error.toString()));
            }
            return;
        }


        //메세지 보내는 부분
        String msg = json.getString("msg");

        // 3. DB저장

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setGroupChatIdx(Long.parseLong(groupId));
        chatMessage.setSenderIdx(userIdx);
        chatMessage.setNickname(nickname);
        chatMessage.setContent(msg);
        chatMessage.setSentAt(LocalDateTime.now());
        chatMessage.setIsDeleted("N");

        chatService.saveMessage(chatMessage); //서비스 단에서 DB 저장
        Long id = chatMessage.getMessagesIdx();
        System.out.println("저장된 메시지 ID = " + id);

        // 4. 같은 그룹(groupId) 세션에 브로드캐스트
        JSONObject response = new JSONObject();
        response.put("from", nickname);
        response.put("msg", msg);
        response.put("userId", userId); // 본인 메세지만 수정 가능하게 하기 위해 추가됨
        response.put("userIdx", userIdx);     // IDX 추가
        response.put("messageId", id);
        response.put("sentAt", chatMessage.getSentAt().toString());
        response.put("isDeleted", chatMessage.getIsDeleted()); // 'Y' 또는 'N'


        for(WebSocketSession s : groupSessions.getOrDefault(groupId, Set.of())){
            if(s.isOpen()){
                s.sendMessage(new TextMessage(response.toString()));
            }
        }

        System.out.println("userIdx = " + userIdx);
        System.out.println("chatMessage.getSenderIdx() = " + chatMessage.getSenderIdx());
    }//handleTextMessage

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        String groupId = getParam(session, "groupId");

        Set<WebSocketSession> sessions = groupSessions.get(groupId);
        if(sessions != null){
            sessions.remove(session);

            String nickname = "알 수 없음";
            SecurityContext context = (SecurityContext) session.getAttributes().get("SPRING_SECURITY_CONTEXT");
            if (context != null && context.getAuthentication() != null) {
                Object principal = context.getAuthentication().getPrincipal();
                if (principal instanceof CustomUserDetails customUser) {
                    nickname = customUser.getNickname();
                }
            }
/*

            sendSystemMessage(groupId, nickname + " 님이 퇴장하셨습니다.");
*/

            if(sessions.isEmpty()){
                groupSessions.remove(groupId);
            }
        }

        System.out.println("연결 종료: groupId = " + groupId);
    }

    private void sendSystemMessage(String groupId, String content){
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setGroupChatIdx(Long.parseLong(groupId));
        systemMsg.setSenderIdx(1l); // 시스템 user_idx
        systemMsg.setNickname("시스템");
        systemMsg.setContent(content);
        systemMsg.setSentAt(LocalDateTime.now());
        systemMsg.setIsDeleted("N");
        systemMsg.setType("system");

        chatService.saveMessage(systemMsg);

        JSONObject json = new JSONObject();
        json.put("from", "시스템");
        json.put("msg", content);
        json.put("type", "system");
        json.put("messageId", systemMsg.getMessagesIdx());
        json.put("sentAt", systemMsg.getSentAt().toString());
        json.put("userId", "system");
        json.put("userIdx", 1);  // 시스템 user idx
        json.put("isDeleted", "N");



        for(WebSocketSession s : groupSessions.getOrDefault(groupId, Set.of())){
            if(s.isOpen()){
                try{
                    s.sendMessage(new TextMessage(json.toString()));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}