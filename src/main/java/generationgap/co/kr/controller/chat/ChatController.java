package generationgap.co.kr.controller.chat;

import generationgap.co.kr.domain.chat.ChatMessage;
import generationgap.co.kr.service.chat.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @GetMapping("/chat")
    public String chatPage(){
        return "chat";
    }

    @GetMapping("/chat/search")
    @ResponseBody
    public List<ChatMessage> searchMessages(@RequestParam String groupId, @RequestParam String keyword){
        return chatService.searchMessagesByKeyword(groupId, keyword);
    }
}
