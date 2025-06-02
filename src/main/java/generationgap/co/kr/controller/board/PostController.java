package generationgap.co.kr.controller.board;

import generationgap.co.kr.domain.board.Comment;
import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.mapper.board.CommentMapper;
import generationgap.co.kr.service.board.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentMapper commentMapper;

    @GetMapping("/write")
    public String showWriteForm(Model model){
        model.addAttribute("post", new Post());
        return "board/write";
    }

    @PostMapping("/write")
    public String submitPost(@ModelAttribute Post post){
        post.setAuthorIdx(1); //임시로 1번 유저로 고정
        postService.savePost(post);
        return "redirect:/posts";
    }

    @GetMapping
    public String getPostList(@RequestParam(defaultValue = "1") int page, Model model){
        int pageSize =10;
        int offset = (page - 1) * pageSize;

        List<Post> posts = postService.getPostListPaged(offset, pageSize);
        int totalCount = postService.getTotalPostCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "board/post-list";
    }

    @GetMapping("/{id}")
    public String showPostDetail(@PathVariable("id") int postIdx,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model){
        postService.incrementViewCount(postIdx); // 조회수 증가시키기
        Post post = postService.getPostById(postIdx); // 게시글 가져오기
        if(post==null || "Y".equals(post.getIsDeleted())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않거나 삭제되었습니다.");
        }

        model.addAttribute("post", post);

        List<Comment> comments = commentMapper.getCommentsByPost(postIdx);// 댓글도 같이 조회되도록 추가
        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", page); // 페이징 시 원페이지로 돌아가도록 추가

        return "board/detail";
    }

    @PostMapping("{id}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@PathVariable("id") int postIdx,
                                        HttpSession session){


        // 세션 기능 생기고 나면 넘어가기
        /* Integer userIdx = (Integer) session.getAttribute("userIdx");
        if(userIdx == null){
            return Map.of("status", "fail", "message", "로그인이 필요합니다.");
        }*/

        int userIdx = 1; //테스트용 고정

        boolean liked = postService.toggleLikePost(userIdx, postIdx);
        int likeCount = postService.getPostById(postIdx).getLikeCount();
        return Map.of(
                //"status", liked ? "success" : "duplicate",
                "liked", liked,
                "likeCount", likeCount);
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable("id")int postIdx, HttpSession session){
        int userIdx = 1; //테스트용

        postService.softDeletePost(postIdx, userIdx);
        return "redirect:/posts";
    }


}
