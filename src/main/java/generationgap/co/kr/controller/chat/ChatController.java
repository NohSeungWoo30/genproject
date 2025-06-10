package generationgap.co.kr.controller.chat;

import generationgap.co.kr.domain.chat.ChatMessage;
import generationgap.co.kr.service.chat.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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


    /*@GetMapping("/chat")
    public String chatPage(){

        return "chat";
    }*/
    //http.csrf().enable(); 이어서 수동으로 CSRF 토큰 강제 삽입
    @GetMapping("/chat")
    public String chatPage(Model model, HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token != null) {
            model.addAttribute("_csrf", token);
        }
        return "chat";
    }


    @GetMapping("/chat/search")
    @ResponseBody
    public List<ChatMessage> searchMessages(@RequestParam String groupId, @RequestParam String keyword){
        return chatService.searchMessagesByKeyword(groupId, keyword);
    }
}
