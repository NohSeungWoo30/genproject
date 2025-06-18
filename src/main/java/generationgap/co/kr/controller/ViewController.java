// src/main/java/generationgap/co/kr/controller/ViewController.java
package generationgap.co.kr.controller;

import generationgap.co.kr.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/product-list", "/product-list.html"})
    public String productList(Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails){

        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }

        return "product-list";
    }
}
