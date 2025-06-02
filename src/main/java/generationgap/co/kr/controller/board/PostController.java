package generationgap.co.kr.controller.board;

import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.service.board.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public String getPostList(Model model){
        List<Post> posts = postService.getPostList();
        model.addAttribute("posts", posts);
        return "post-list";
    }
}
