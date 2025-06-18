// src/main/java/generationgap/co/kr/controller/ViewController.java
package generationgap.co.kr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/product-list", "/product-list.html"})
    public String productList() {
        return "product-list";
    }
}