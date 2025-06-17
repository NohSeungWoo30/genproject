package generationgap.co.kr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/main")
    public String mainPage() {
        return "main"; // main.html 템플릿 반환
    }

    @GetMapping("/main/main")
    public String mainPage2() {
        return "main/main"; // main.html 템플릿 반환
    }

    @GetMapping("/main/login")
    public String login2() {
        return "main/login"; // main.html 템플릿 반환
    }
    @GetMapping("/mypage")
    public String mypage() {
        return "mypage"; // main.html 템플릿 반환
    }
}