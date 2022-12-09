package cms.blog.service;

import cms.blog.dao.PostDao;
import cms.blog.dto.HashTag;
import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;
import cms.blog.dto.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("test")
public class PostDaoStub implements PostDao {
    @Override
    public Post addPost(Post post) {
        post.setPostId(1);
        return post;
    }

    @Override
    public boolean editPost(Post post) {
        if (post.getTitle() == "existing") {
            return true;
        }
        return false;
    }

    @Override
    public boolean deletePost(int id) {
        if (id == 1) {
            return true;
        }
        return false;
    }

    @Override
    public Post getPostById(int id) {
        if (id == 1) {
            Post post = new Post();
            post.setPostContent("content");
            post.setTitle("title");
            post.setPostId(id);
            return post;
        }
        return null;
    }

    @Override
    public RejectedPost getRejectedPostById(int id) {
        if (id == 1) {
            RejectedPost post = new RejectedPost();
            post.setPostContent("content");
            post.setTitle("title");
            post.setPostId(id);
            post.setStatus(Status.REJECTED);
            post.setReason("reason");
            return post;
        }
        return null;
    }

    @Override
    public List<Post> getApprovedPostsForAdmin() {
        Post post = new Post();
        post.setPostContent("content");
        post.setTitle("title");
        post.setPostId(1);
        post.setStatus(Status.APPROVED);
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        return posts;
    }

    @Override
    public List<Post> getPostsByTagForAdmin(int tagId) {
        if (tagId != 1) {
            return new ArrayList<>();
        }
        Post post = new Post();
        post.setPostContent("content");
        post.setTitle("title");
        post.setPostId(1);
        List<HashTag> tags = new ArrayList<>();
        HashTag tag = new HashTag();
        tag.setHashTagName("tag");
        tag.setHashTagId(tagId);
        tags.add(tag);
        post.setTags(tags);
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        return posts;
    }

    @Override
    public List<Post> getPostsByTagForUser(int tagId) {
        return getPostsByTagForAdmin(tagId);
    }

    @Override
    public List<Post> getPostsByContentForAdmin(String content) {
        Post post = new Post();
        post.setPostContent(content);
        post.setTitle("title");
        post.setPostId(1);
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        return posts;
    }

    @Override
    public List<Post> getPostsByContentForUser(String content) {
        return getPostsByContentForAdmin(content);
    }

    @Override
    public List<Post> getApprovedPostsForUser() {
        return getApprovedPostsForAdmin();
    }

    @Override
    public List<Post> getNotApprovedPosts() {
        Post post = new Post();
        post.setPostContent("content");
        post.setTitle("title");
        post.setPostId(1);
        post.setStatus(Status.IN_WORK);
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        return posts;
    }

    @Override
    public List<RejectedPost> getRejectedPosts() {
        RejectedPost post = new RejectedPost();
        post.setPostContent("content");
        post.setTitle("title");
        post.setPostId(1);
        post.setStatus(Status.REJECTED);
        post.setReason("reason");
        List<RejectedPost> posts = new ArrayList<>();
        posts.add(post);
        return posts;
    }

    @Override
    public void approvePost(int postId, LocalDateTime creationTime) {
    }

    @Override
    public void rejectPost(int postId, String reason) {
    }

    @Override
    public void sendToApprove(int postId) {

    }
}
