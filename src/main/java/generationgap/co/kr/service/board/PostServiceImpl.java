package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.post.Attachment;
import generationgap.co.kr.mapper.board.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final PostMapper postMapper;




    @Override
    public int getTotalPostCountFiltered(String category){

        return postMapper.getPostCountFiltered(category);
    }

    @Override
    public void writePostWithAttachments(Post post, List<MultipartFile> files){
        postMapper.insertPost(post); //게시글 먼저 저장

        for (MultipartFile file : files) {
            if (!file.isEmpty()){
                String originalName = file.getOriginalFilename();
                String contentType = file.getContentType();
                long size = file.getSize();

               if(contentType==null || !contentType.matches("image/(jpeg|png|gif|webp)")){
                    throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
                }

                if(size > 10 * 1024 * 1024) {
                    throw new IllegalArgumentException("파일은 10MB 이하만 업로드 가능합니다.");
                }

                String storedName = UUID.randomUUID() + "-" + originalName;
                Path uploadDir = Paths.get("C:/uploads");
                try{
                    if(!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                    Path targetPath = uploadDir.resolve(storedName);
                    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("파일 저장 실패", e);
                }
                Attachment attach = new Attachment();
                attach.setPostIdx(post.getPostIdx().intValue());
                attach.setUploaderIdx(post.getAuthorIdx());
                attach.setFileName(storedName);
                attach.setOriginalName(originalName);

                postMapper.insertAttachmentWithOriginal(attach);

            }
        }
    }

    @Override
    public Post getPostById(int postIdx){
        return postMapper.getPostById(postIdx);
    }

    @Override
    public void incrementViewCount(int postIdx){
        postMapper.updateViewCount(postIdx);
    }

    @Override
    public boolean toggleLikePost(int userIdx, int postIdx){
        int count = postMapper.hasUserLikedPost(userIdx, postIdx);
        if(count>0){
            postMapper.deletePostLike(userIdx, postIdx);
            postMapper.decrementLikeCount(postIdx);
            return false; //추천 취소하기
        }else{
            postMapper.insertPostLikeCheck(userIdx, postIdx);
            postMapper.updateLikeCount(postIdx);
            return true; //추천하기
        }
    }

    @Override
    public void softDeletePost(int postIdx, int userIdx){
        postMapper.softDeletePost(postIdx, userIdx);
    }

    @Override
    public List<Attachment> getAttachmentsByPostId(Long postIdx) {
        return postMapper.getAttachmentsByPostId(postIdx); // MyBatis 매퍼에서 조회
    }

    @Override
    public void updatePost(Post post, List<MultipartFile> files){

        // 수정 전 게시글 데이터 조회
        Post originalPost = postMapper.getPostById(post.getPostIdx().intValue());
        if (originalPost == null) {
            throw new IllegalArgumentException("기존 게시글이 존재하지 않습니다.");
        }

        // 수정 이력 저장
        postMapper.insertPostEditHistory(
                post.getPostIdx().intValue(),
                originalPost.getTitle(),
                originalPost.getContent(),
                post.getAuthorIdx()
        );

        //1. 게시글 수정
        postMapper.updatePost(post);

        // 파일이 실제로 업로드되어야 기존 파일 삭제
        boolean hasNewFiles = files != null && !files.isEmpty() && files.stream().anyMatch(file -> !file.isEmpty());

        //2. 첨부파일 조회
        List<Attachment> oldAttachments  = postMapper.getAttachmentsByPostId(post.getPostIdx());

        if(hasNewFiles){


        //3. 기존 첨부파일 있으면 삭제
        for (Attachment attachment : oldAttachments){
            String savedName = attachment.getFileName();
            Path filepath = Paths.get("c:/uploads", savedName);
            try{
                Files.deleteIfExists(filepath);
            } catch (IOException e) {
                System.err.println("파일 삭제 실패: "+savedName);
            }
        }

        //4. DB에서 첨부파일 정보 삭제
        postMapper.deleteAttachmentsByPostId(post.getPostIdx());

        //5. 새 첨부파일 저장
        if(files != null){
            for(MultipartFile file : files){
                if(!file.isEmpty()){
                    String originalName = file.getOriginalFilename();
                    String contentType = file.getContentType();
                    long size = file.getSize();

                    //파일 타입 제한
                    if (contentType == null || !contentType.matches("image/(jpeg|png|gif|webp)")) {
                        throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
                    }

                    if (size > 10 * 1024 * 1024) {
                        throw new IllegalArgumentException("파일은 10MB 이하만 업로드 가능합니다.");
                    }

                    // 저장명 생성 및 저장
                    String storedName = UUID.randomUUID() + "-" + originalName;
                    Path uploadDir = Paths.get("C:/uploads");
                    try {
                        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                        Path targetPath = uploadDir.resolve(storedName);
                        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("파일 저장 실패", e);
                    }

                    // 첨부파일 객체 생성
                    Attachment attach = new Attachment();
                    attach.setPostIdx(post.getPostIdx().intValue());
                    attach.setUploaderIdx(post.getAuthorIdx()); // 작성자 정보
                    attach.setFileName(storedName);
                    attach.setOriginalName(originalName);

                    // DB에 insert
                    postMapper.insertAttachmentWithOriginal(attach);

                }
            }
        }
        }
    }

    @Override
    public Map<Integer, Integer> getVisibleCommentCounts(List<Integer> postIds) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Integer postId : postIds) {
            int count = postMapper.getVisibleCommentCountByPost(postId);
            result.put(postId, count);
        }
        return result;
    }

    @Override
    public List<Post> getPostListPaged(int offset, int limit, String sort) {
        return postMapper.getPostsPagedFiltered(offset, limit, null, sort);

    }

    @Override
    public List<Post> getPostListPagedFiltered(int offset, int limit, String category, String sort) {
        return postMapper.getPostsPagedFiltered(offset, limit, category, sort);
    }



}
