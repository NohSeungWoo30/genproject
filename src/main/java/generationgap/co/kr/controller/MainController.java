package generationgap.co.kr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/main")
    public String mainPage() {
        return "main"; // main.html2 템플릿 반환
    }

    @GetMapping("/main/MAIN.html")
    public String mainPage2() {
        return "main/MAIN.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/Meeting-list.html")
    public String meetinglist() {
        return "main/Meeting-list.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/board.html")
    public String board() {
        return "main/board.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/detailboard.html")
    public String detail() {
        return "main/detailboard.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/login.html")
    public String login() {
        return "main/login.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/signup.html")
    public String signup() {
        return "main/signup.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/write.html")
    public String write() {
        return "main/write.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/templates/meetingcreate.html")
    public String meetingcreate() {
        return "/templates/meetingcreate.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/meetingdetail.html")
    public String meetingdetail() {
        return "/main/meetingdetail.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/templates/product-list.html")
    public String product() {
        return "/templates/product-list.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/api/payments/confirml")
    public String payments() {
        return "/api/payments/confirml"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/templates/payment_success.html")
    public String payment_success() {
        return "/templates/mpayment_success.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/templates/payment_fail.html")
    public String payment_fail() {
        return "/templates/payment_fail.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/templates/mypage.html")
    public String mypage() {
        return "/templates/mypage.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/Id_Passwordfind.html")
    public String Id_Passwordfind() {
        return "/main/Id_Passwordfind.html"; // src/main/resources/templates/main/main.html을 찾음
    }

    @GetMapping("/main/user-profile.html")
    public String userprofile() {
        return "/main/user-profile.html"; // src/main/resources/templates/main/main.html을 찾음
    }

}