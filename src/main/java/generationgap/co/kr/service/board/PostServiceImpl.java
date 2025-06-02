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


    public List<Post> getPostList() {
        return postMapper.getAllPosts();
    }
}
