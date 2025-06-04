package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.dto.post.Attachment;
import generationgap.co.kr.mapper.board.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PostServiceImpl implements PostService{

    private final PostMapper postMapper;

    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    public List<Post> getPostListPaged(int offset, int limit) {
        return postMapper.getPostsPaged(offset, limit);
    }

    @Override
    public int getTotalPostCount(){
        return postMapper.getPostCount();
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




}
