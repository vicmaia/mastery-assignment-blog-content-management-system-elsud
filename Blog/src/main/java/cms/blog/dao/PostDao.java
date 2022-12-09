package cms.blog.dao;

import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;

import java.time.LocalDateTime;
import java.util.List;

public interface PostDao {

    public Post addPost(Post post);

    public boolean editPost(Post post);

    public boolean deletePost(int id);

    public Post getPostById(int id);

    public RejectedPost getRejectedPostById(int id);

    public List<Post> getApprovedPostsForAdmin();

    public List<Post> getPostsByTagForAdmin(int tagId);

    public List<Post> getPostsByTagForUser(int tagId);

    public List<Post> getPostsByContentForAdmin(String content);

    public List<Post> getPostsByContentForUser(String content);

    public List<Post> getApprovedPostsForUser();

    public List<Post> getNotApprovedPosts();

    public List<RejectedPost> getRejectedPosts();

    public void approvePost(int postId, LocalDateTime creationTime);

    public void rejectPost(int postId, String reason);

    public void sendToApprove(int postId);
}
