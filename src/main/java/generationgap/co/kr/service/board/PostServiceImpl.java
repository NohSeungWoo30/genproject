package generationgap.co.kr.service.board;

import generationgap.co.kr.domain.board.Post;
import generationgap.co.kr.mapper.board.PostMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void savePost(Post post){
        postMapper.insertPost(post);
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


}
