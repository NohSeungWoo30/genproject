package generationgap.co.kr.controller.board;

import generationgap.co.kr.domain.board.Comment;
import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.post.Attachment;
import generationgap.co.kr.mapper.board.CommentMapper;
import generationgap.co.kr.security.CustomUserDetails;
import generationgap.co.kr.service.board.CommentService;
import generationgap.co.kr.service.board.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentMapper commentMapper;
    private final CommentService commentService;

    @GetMapping("/write")
    public String showWriteForm(Model model,
                                @AuthenticationPrincipal CustomUserDetails userDetails
                                ){

        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        model.addAttribute("post", new Post());
        return "board/write";
    }

    @PostMapping("/write")
    public String submitPost(@ModelAttribute Post post,
                             @RequestParam("files")List<MultipartFile> files){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 로그인 여부 및 타입 체크
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "로그인이 필요합니다.");
        }


        CustomUserDetails userDetails = (CustomUserDetails)auth.getPrincipal();

        post.setAuthorIdx(userDetails.getUserIdx().intValue());
        post.setAuthorName(userDetails.getNickname());

        postService.writePostWithAttachments(post, files);
        return "redirect:/posts";
    }

    @GetMapping
    public String getPostList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "new")String sort,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String keyword,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model){

        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }


        int pageSize =10;
        int offset = (page - 1) * pageSize;


        if (category == null || category.equalsIgnoreCase("null") || category.isBlank()) {
            category = null;
        }
        if (keyword == null || keyword.equalsIgnoreCase("null") || keyword.isBlank()) {
            keyword = null;
        }
        if (sort == null || sort.equalsIgnoreCase("null") || sort.isBlank()) {
            sort = "new"; // 기본 정렬 방식 지정
        }

        List<Post> posts = postService.getPostListPagedFiltered(offset, pageSize, category, sort, keyword);


        // 댓글 수 계산 추가
        for (Post post : posts) {
            List<Comment> all = commentService.getFilteredCommentsByPost(post.getPostIdx().intValue());
            int visibleCount = (int) all.stream()
                    .filter(c -> {
                        if ("Y".equals(c.getIsDeleted())) return false;
                        if (c.getParentCommentId() != null) {
                            Comment parent = all.stream()
                                    .filter(p -> p.getCommentIdx().equals(c.getParentCommentId()))
                                    .findFirst().orElse(null);
                            return parent != null && !"Y".equals(parent.getIsDeleted());
                        }
                        return true;
                    }).count();
            post.setCommentCount(visibleCount);
        }


        int totalCount = postService.getTotalPostCountFiltered(category,keyword);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sort", sort);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);


        return "board/post-list";
    }

    @GetMapping("/{id}")
    public String showPostDetail(@PathVariable("id") int postIdx,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(defaultValue = "new") String sort,
                                 @RequestParam(required = false) String keyword,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model,
                                 HttpServletRequest request){

        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }

        postService.incrementViewCount(postIdx); // 조회수 증가시키기
        Post post = postService.getPostById(postIdx); // 게시글 가져오기
        if(post==null || "Y".equals(post.getIsDeleted())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않거나 삭제되었습니다.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String createdAtStr = post.getCreatedAt() != null
                ? post.getCreatedAt().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter)
                : null;

        String updatedAtStr = post.getUpdateAt() != null
                ? post.getUpdateAt().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter)
                : null;

        boolean isEdited = post.getUpdateAt() != null &&
                !post.getUpdateAt().equals(post.getCreatedAt());

        model.addAttribute("post", post);
        model.addAttribute("createdAtStr", createdAtStr);
        model.addAttribute("updatedAtStr", updatedAtStr);
        model.addAttribute("isEdited", isEdited);



        // 첨부파일 목록 조회 추가
        List<Attachment> attachments = postService.getAttachmentsByPostId((long) postIdx);
        model.addAttribute("attachments", attachments);

        //댓글
        List<Comment> allComments = commentService.getFilteredCommentsByPost(postIdx);// 댓글도 같이 조회되도록 추가

        int visibleCommentCount = (int) allComments.stream()          // 카운트용 숫자
                .filter(c->{
                    if("Y".equals(c.getIsDeleted())) return false;
                    if(c.getParentCommentId()!= null){
                        // 대댓글의 경우, 부모가 삭제되었는지도 확인해야 함
                        Comment parent = allComments.stream()
                                .filter(p -> p.getCommentIdx().equals(c.getParentCommentId()))
                                .findFirst()
                                .orElse(null);
                        return parent != null && !"Y".equals(parent.getIsDeleted());
                    }
                    return true;
                })
                .count();

        //댓글 정렬방식 추가
        //원댓글만 필터링
        List<Comment> parentComments = allComments.stream()
                        .filter(c -> c.getParentCommentId() == null)
                        .sorted(Comparator.comparing(Comment::getCommentIdx))
                        .toList();

        //대댓글 그룹핑
        Map<Long, List<Comment>> repliesGroupedByParentId = allComments.stream()
                        .filter(c-> c.getParentCommentId() != null)
                        .sorted(Comparator.comparing(Comment::getCommentIdx))
                        .collect(Collectors.groupingBy(Comment::getParentCommentId));
//        model.addAttribute("post", post);
//        model.addAttribute("attachments", postService.getAttachmentsByPostId((long) postIdx));
        model.addAttribute("parentComments", parentComments);
        model.addAttribute("repliesMap", repliesGroupedByParentId);
        model.addAttribute("currentPage", page);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));





        //대댓글 갯수 파악하기 위해 추가
        Map<Long, Integer> replyCounts = new HashMap<>();
        for (Comment parent : parentComments) {
            List<Comment> replies = repliesGroupedByParentId.get(parent.getCommentIdx());

            // null 방지
            if (replies == null) {
                replyCounts.put(parent.getCommentIdx(), 0);
            } else {
                int visibleCount = (int) replies.stream()
                        .filter(r -> !"Y".equals(r.getIsDeleted()))
                        .count();
                replyCounts.put(parent.getCommentIdx(), visibleCount);
            }
        }
        model.addAttribute("replyCounts", replyCounts);
        model.addAttribute("commentCount", visibleCommentCount);

        //페이징 위해 추가
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("keyword", keyword);


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
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model){

        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
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
                           @RequestParam String category,
                           @RequestParam(required = false) List<MultipartFile> files
                           ){

        int userIdx = getLoginUserIdx();
        Post post = postService.getPostById(postIdx.intValue());

        if(post == null || "Y".equals(post.getIsDeleted())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 글입니다.");
        }

        if (!userIdxEquals(post.getAuthorIdx(), userIdx)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }
        post.setCategory(category);
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
