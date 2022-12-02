package cms.blog.dao;

import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;

import java.util.List;

public interface PostDao {

    public Post addPost(Post post);

    public boolean editPost(Post post);

    public boolean deletePost(int id);

    public Post getPostById(int id);

    public List<Post> getPosts();

    public List<Post> getPosts(int tagId);

    public List<Post> getApprovedPosts();

    public List<Post> getNotApprovedPosts();

    public List<RejectedPost> getRejectedPosts();

    public void approvePost(int postId);

    public void rejectPost(int postId, String reason);

    public void sendToApprove(int postId);
}
