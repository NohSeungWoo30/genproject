package generationgap.co.kr.controller.user;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "user/signup";
    }

    // 회원가입 폼 제출을 POST 요청으로 처리하는 메서드
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute("user") UserDTO user) {
        userService.registerUser(user);
        return "redirect:/user/login"; // 회원가입 성공 후 로그인 페이지로 리다이렉트
    }

    // *** 이 부분 추가: 로그인 페이지를 GET 요청으로 보여주는 메서드 ***
    @GetMapping("/login")
    public String loginPage() {
        return "user/login"; // src/main/resources/templates/user/login.html 반환
    }
}