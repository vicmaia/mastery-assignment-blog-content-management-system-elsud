package cms.blog.service;

import cms.blog.dao.PostDao;
import cms.blog.dao.TagDao;
import cms.blog.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("prod")
public class ServiceLayer {

    private final PostDao postDao;
    private final TagDao tagDao;

    @Autowired
    public ServiceLayer(PostDao postDao, TagDao tagDao) {
        this.postDao = postDao;
        this.tagDao = tagDao;
    }

    public Post addPost(Post post, Permission permission) throws AuthorizationException {
        setPostDate(post);
        switch(permission) {
            case ADMIN:
                post.setStatus(Status.APPROVED);
                break;
            case MANAGER:
                post.setStatus(Status.IN_WORK);
                break;
            default:
                throw new AuthorizationException("Access denied!");
        }
        return postDao.addPost(post);
    }

    public boolean editPost(Post post, Permission permission) throws AuthorizationException {
        setPostDate(post);
        switch(permission) {
            case ADMIN:
                return postDao.editPost(post);
            case MANAGER:
                switch (post.getStatus()) {
                    case IN_WORK:
                    case REJECTED:
                        return postDao.editPost(post);
                    default:
                        throw new AuthorizationException("Access denied!");
                }
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    public void addTagForPost(String tagName, int postId) {
        Tag tag = tagDao.getTagByName(tagName);
        if (tag == null) {
            tag = tagDao.addTag(new Tag(tagName));
        }
        tagDao.addTagForPost(tag, postId);
    }

    public void deleteTagForPost(int tagId, int postId) {
        tagDao.deleteTagForPost(tagId, postId);
    }

    public boolean deletePost(int id, Permission permission) throws AuthorizationException {
        Status status = postDao.getPostById(id).getStatus();
        switch(permission) {
            case ADMIN:
                return postDao.deletePost(id);
            case MANAGER:
                switch (status) {
                    case IN_WORK:
                    case REJECTED:
                        return postDao.deletePost(id);
                    default:
                        throw new AuthorizationException("Access denied!");
                }
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    public Post getPostById(int id) {
        return postDao.getPostById(id);
    }

    public RejectedPost getRejectedPostById(int id) {
        return postDao.getRejectedPostById(id);
    }

    public List<Post> getPosts(Permission permission) {
        switch (permission) {
            case ADMIN:
                return postDao.getApprovedPostsForAdmin();
            case USER:
            default:
                return postDao.getApprovedPostsForUser();
        }
    }

    public List<Post> getPostsByTag(int tagId, Permission permission) {
        switch (permission) {
            case ADMIN:
                return postDao.getPostsByTagForAdmin(tagId);
            case USER:
            default:
                return postDao.getPostsByTagForUser(tagId);
        }
    }

    public List<Post> getNotApprovedPosts(Permission permission) throws AuthorizationException {
        switch (permission) {
            case ADMIN:
            case MANAGER:
                return postDao.getNotApprovedPosts();
            case USER:
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    public List<RejectedPost> getRejectedPosts(Permission permission) throws AuthorizationException {
        switch (permission) {
            case ADMIN:
            case MANAGER:
                return postDao.getRejectedPosts();
            case USER:
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    public void approvePost(int postId, Permission permission) throws AuthorizationException {
        switch(permission) {
            case ADMIN:
                Post post = postDao.getPostById(postId);
                setPostDate(post);
                postDao.approvePost(postId, post.getCreationTime());
                break;
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    public void rejectPost(int postId, String reason, Permission permission) throws AuthorizationException {
        switch(permission) {
            case ADMIN:
                postDao.rejectPost(postId, reason);
                break;
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    public void sendToApprove(int postId, Permission permission) throws AuthorizationException {
        switch(permission) {
            case MANAGER:
                postDao.sendToApprove(postId);
                break;
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    private void setPostDate(Post post) {
        if (post.getDisplayDate() != null && post.getDisplayDate().isAfter(LocalDate.now())) {
            post.setCreationTime(post.getDisplayDate().atStartOfDay());
        }
        else {
            post.setCreationTime(LocalDateTime.now());
        }
    }
}
