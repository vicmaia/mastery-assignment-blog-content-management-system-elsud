package cms.blog.service;

import cms.blog.dao.PostDao;
import cms.blog.dao.HashtagDao;
import cms.blog.dao.UserDao;
import cms.blog.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Profile("prod")
public class ServiceLayer {

    private final PostDao postDao;
    private final HashtagDao tagDao;
    private final UserDao userDao;

    @Autowired
    public ServiceLayer(PostDao postDao, HashtagDao tagDao, UserDao userDao) {
        this.postDao = postDao;
        this.tagDao = tagDao;
        this.userDao = userDao;
    }

    /**
     * Save given post for caller with given permission. Set creationTime according
     * to publishDate. Set approved status for admin, in_work status for manager
     * and throws exception for user.
     * @param post Post object that should be saved
     * @param permission role of the caller
     * @return created Post object
     * @throws AuthorizationException for users
     */
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

    /**
     * Update post for caller with given permissions. Modify creationTime according to
     * publishDate. Can update post or throw AuthorizationException for wrong permission.
     * Admin can update any post, manager can update posts with in_work and rejected status,
     * user cannot update posts. Post must have id to be updated
     * @param post Post object with modified fields
     * @param permission role of the caller
     * @return boolean if posts was edited
     * @throws AuthorizationException for users and in some cases for manager
     */
    public boolean editPost(Post post, Permission permission) throws AuthorizationException {
        setPostDate(post);
        post.setEditTime(LocalDateTime.now());
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

    /**
     * Add tag for post. Create this tag if it doesn't exist
     * @param tagName name of the tag that should be added
     * @param postId id of the post to which tag should be added
     */
    public void addTagForPost(String tagName, int postId) {
        HashTag tag = tagDao.getTagByName(tagName);
        if (tag == null) {
            tag = tagDao.addTag(new HashTag(tagName));
        }
        tagDao.addTagForPost(tag, postId);
    }

    /**
     * Delete connection between tag with given id and post with given id
     * @param tagId id of the tag which should be removed from post
     * @param postId id of the post from which tag should be removed
     */
    public void deleteTagForPost(int tagId, int postId) {
        tagDao.deleteTagForPost(tagId, postId);
    }

    /**
     * Delete post with given id for caller with given permission. Admin can delete
     * any post, manager can delete post with in_work or rejected status,
     * user cannot delete posts.
     * @param id int with id of the post to delete
     * @param permission role of the caller
     * @return boolean if post was deleted
     * @throws AuthorizationException for user caller and in some cases for manager
     */
    public boolean deletePost(int id, Permission permission) throws AuthorizationException {
        switch(permission) {
            case ADMIN:
                return postDao.deletePost(id);
            case MANAGER:
                Post post = postDao.getPostById(id);
                if (post == null) {
                    return false;
                }
                Status status = post.getStatus();
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

    /**
     * Get Post with given id
     * @param id id of the post to return
     * @return  Post object or null
     */
    public Post getPostById(int id) {
        return postDao.getPostById(id);
    }

    /**
     * Get RejectedPost with given id
     * @param id id of the RejectedPost to return
     * @return RejectedPost object or null
     */
    public RejectedPost getRejectedPostById(int id) {
        return postDao.getRejectedPostById(id);
    }

    /**
     * Get list of posts with status approved for caller with given permission.
     * For admin there will be all approved posts, for user there will be only approved posts
     * that published and not expired
     * @param permission role of the caller
     * @return List of Posts
     */
    public List<Post> getPosts(Permission permission) {
        switch (permission) {
            case ADMIN:
                return postDao.getApprovedPostsForAdmin();
            case USER:
            default:
                return postDao.getApprovedPostsForUser();
        }
    }


    /**
     * Get List of Posts related to tag with given id. For admin there will be all posts
     * for this tag, for user there will be only approved posts that published and not expired
     * @param tagId id of the tag to search posts by it
     * @param permission role of the caller
     * @return List of Posts
     */
    public List<Post> getPostsByTag(int tagId, Permission permission) {
        switch (permission) {
            case ADMIN:
                return postDao.getPostsByTagForAdmin(tagId);
            case USER:
            default:
                return postDao.getPostsByTagForUser(tagId);
        }
    }

    /**
     * Get List of Posts which contain given content in title or in postContent. For admin there will be all
     * found posts, for user there will be only approved posts that published and not expired
     * @param content String that should post title or postContent should contain
     * @param permission role of the caller
     * @return List of Posts
     */
    public List<Post> getPostsByContent(String content, Permission permission) {
        switch (permission) {
            case ADMIN:
            case MANAGER:
                return postDao.getPostsByContentForAdmin(content);
            case USER:
            default:
                return postDao.getPostsByContentForUser(content);
        }
    }

    /**
     * Get List of Posts which status is in_work
     * @param permission role of the caller
     * @return List of found Posts
     * @throws AuthorizationException for user caller
     */
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

    /**
     * Get List of Posts which status is rejected
     * @param permission role of the caller
     * @return List of found Posts
     * @throws AuthorizationException for user caller
     */
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

    /**
     * Modify status of post with given id to approved. Can be called by admin only
     * @param postId id of the post that should be approved
     * @param permission role of the caller
     * @throws AuthorizationException for user and manager caller
     */
    public void approvePost(int postId, Permission permission) throws AuthorizationException {
        switch(permission) {
            case ADMIN:
                Post post = postDao.getPostById(postId);
                if (post != null) {
                    setPostDate(post);
                    postDao.approvePost(postId, post.getCreationTime());
                }
                break;
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    /**
     * Modify status of post with given id to rejected. Save the reason of rejection as well.
     * Can be called by admin only
     * @param postId id of the post that should be rejected
     * @param reason String with reason of the rejection
     * @param permission role of the caller
     * @throws AuthorizationException for user and manager caller
     */
    public void rejectPost(int postId, String reason, Permission permission) throws AuthorizationException {
        switch(permission) {
            case ADMIN:
                postDao.rejectPost(postId, reason);
                break;
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    /**
     * Modify status of post with given id to in_work. Delete the reason of rejection.
     * Can be called by manager only
     * @param postId id of the post that should be modified to in_work status
     * @param permission role of the caller
     * @throws AuthorizationException for admin and user caller
     */
    public void sendToApprove(int postId, Permission permission) throws AuthorizationException {
        switch(permission) {
            case MANAGER:
                postDao.sendToApprove(postId);
                break;
            default:
                throw new AuthorizationException("Access denied!");
        }
    }

    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public User getUserByName(String name) {
        return userDao.getUserByName(name);
    }

    /**
     * Set creationTime to publishDate if publishDate is in the future.
     * In other case set creationTime to this moment.
     * @param post Post object to which creationTime should be set
     */
    private void setPostDate(Post post) {
        if (post.getPublishDate() != null && post.getPublishDate().isAfter(LocalDate.now())) {
            post.setCreationTime(post.getPublishDate().atStartOfDay());
        }
        else {
            post.setCreationTime(LocalDateTime.now());
        }
    }
}
