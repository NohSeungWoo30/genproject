package generationgap.co.kr.controller.board;

import generationgap.co.kr.domain.board.Comment;
import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.post.Attachment;
import generationgap.co.kr.mapper.board.CommentMapper;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.board.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    public String submitPost(@ModelAttribute Post post,
                             @RequestParam("files")List<MultipartFile> files){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)auth.getPrincipal();

        post.setAuthorIdx(userDetails.getUserIdx().intValue());
        post.setAuthorName(userDetails.getNickname());

        postService.writePostWithAttachments(post, files);
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
                                 Model model,
                                 HttpServletRequest request){
        postService.incrementViewCount(postIdx); // 조회수 증가시키기
        Post post = postService.getPostById(postIdx); // 게시글 가져오기
        if(post==null || "Y".equals(post.getIsDeleted())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않거나 삭제되었습니다.");
        }

        model.addAttribute("post", post);

        // 첨부파일 목록 조회 추가
        List<Attachment> attachments = postService.getAttachmentsByPostId((long) postIdx);
        model.addAttribute("attachments", attachments);

        List<Comment> comments = commentMapper.getCommentsByPost(postIdx);// 댓글도 같이 조회되도록 추가
        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", page); // 페이징 시 원페이지로 돌아가도록 추가


        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("_csrf", csrfToken);

        return "board/detail";
    }

    @PostMapping("{id}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@PathVariable("id") int postIdx,
                                        HttpSession session){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 로그인 안했으면 실패 처리
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")){
            return Map.of(
                    "status", "fail",
                    "message", "로그인이 필요합니다."
            );
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        int userIdx = userDetails.getUserIdx().intValue();

        boolean liked = postService.toggleLikePost(userIdx, postIdx);
        int likeCount = postService.getPostById(postIdx).getLikeCount();
        return Map.of(
                //"status", liked ? "success" : "duplicate",
                "liked", liked,
                "likeCount", likeCount);
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable("id")int postIdx, HttpSession session){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            // 로그인되지 않았거나, principal이 String인 경우
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "로그인이 필요합니다.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        int userIdx = userDetails.getUserIdx().intValue();

        // ① 게시글을 가져와서 authorIdx를 꺼낸다
        Post post = postService.getPostById(postIdx);
        if (post == null || "Y".equals(post.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않거나 이미 삭제되었습니다.");
        }

        // ② authorIdx가 null인지 먼저 확인
        Integer authorIdx = post.getAuthorIdx();
        if (authorIdx == null) {
            // DB에서 authorIdx가 비어있다면, 작성자가 없는 상태이므로 삭제 불가
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자 정보가 없어서 삭제할 수 없습니다.");
        }

        // ③ 현재 로그인한 userIdx와 authorIdx를 비교
        if (!authorIdx.equals(userIdx)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인만 삭제할 수 있습니다.");
        }

        // ④ 정상적으로 작성자 일치 → 소프트 삭제 수행
        postService.softDeletePost(postIdx, userIdx);
        return "redirect:/posts";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") int postIdx,
                               Model model){
        int userIdx = getLoginUserIdx();
        Post post = postService.getPostById(postIdx);

        if(post == null || "Y".equals(post.getIsDeleted())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 글입니다.");
        }

        if (!userIdxEquals(post.getAuthorIdx(), userIdx)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"작성자만 수정할 수 있습니다.");
        }

        model.addAttribute("post", post);
        return "board/edit";
    }


    @PostMapping("/{id}/edit")
    public String editPost(@PathVariable("id") Long postIdx,
                           @RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(required = false) List<MultipartFile> files ){
        int userIdx = getLoginUserIdx();
        Post post = postService.getPostById(postIdx.intValue());

        if(post == null || "Y".equals(post.getIsDeleted())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 글입니다.");
        }

        if (!userIdxEquals(post.getAuthorIdx(), userIdx)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }
        post.setPostIdx(postIdx);
        post.setTitle(title);
        post.setContent(content);
        postService.updatePost(post, files);

        return "redirect:/posts/" + postIdx;
    }
    //수정관련 유틸 매서드
    private boolean userIdxEquals(Integer a, Integer b) {
        return a != null && a.equals(b);
    }

    private int getLoginUserIdx(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "로그인이 필요합니다.");
        }

        return ((CustomUserDetails) principal).getUserIdx().intValue();
    }


}
