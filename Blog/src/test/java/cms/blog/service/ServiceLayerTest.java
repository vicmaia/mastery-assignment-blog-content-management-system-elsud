package cms.blog.service;

import cms.blog.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class ServiceLayerTest {

    @Autowired
    ServiceLayer testService;

    @Test
    public void addPostUser() {
        Post post = new Post();
        post.setTitle("title");
        assertThrows(AuthorizationException.class, ()-> testService.addPost(post, Permission.USER));
    }

    @Test
    public void editPostManager() {
        Post post = new Post();
        post.setTitle("title");
        try {
            Post addedPost = testService.addPost(post, Permission.MANAGER);
            assertEquals(1, addedPost.getPostId());
            assertEquals(post.getTitle(), addedPost.getTitle());
            assertEquals(Status.IN_WORK, addedPost.getStatus());
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for manager");
        }
    }

    @Test
    public void editPostAdmin() {
        Post post = new Post();
        post.setTitle("title");
        try {
            Post addedPost = testService.addPost(post, Permission.ADMIN);
            assertEquals(1, addedPost.getPostId());
            assertEquals(post.getTitle(), addedPost.getTitle());
            assertEquals(Status.APPROVED, addedPost.getStatus());
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for admin");
        }
    }

    @Test
    public void deletePostUser() {
        assertThrows(AuthorizationException.class, ()-> testService.deletePost(1, Permission.USER));
    }

    @Test
    public void deleteExistingPostAdmin() {
        try {
            assertTrue(testService.deletePost(1, Permission.ADMIN));
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for admin");
        }
    }

    @Test
    public void deleteNotExistingPostAdmin() {
        try {
            assertFalse(testService.deletePost(2, Permission.ADMIN));
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for admin");
        }
    }

    @Test
    public void getExistingPostById() {
        Post post = testService.getPostById(1);
        assertNotNull(post);
        assertEquals("title", post.getTitle());
        assertEquals("content", post.getPostContent());
    }

    @Test
    public void getNotExistingPostById() {
        Post post = testService.getPostById(2);
        assertNull(post);
    }

    @Test
    public void getRejectedPostById() {
        RejectedPost post = testService.getRejectedPostById(1);
        assertNotNull(post);
        assertEquals("title", post.getTitle());
        assertEquals("content", post.getPostContent());
        assertEquals(Status.REJECTED, post.getStatus());
        assertEquals("reason", post.getReason());
    }

    @Test
    public void getNotExistingRejectedPostById() {
        assertNull(testService.getRejectedPostById(2));
    }

    @Test
    public void getPosts() {
        List<Post> posts = testService.getPosts(Permission.MANAGER);
        assertEquals(1, posts.size());
        Post post = posts.get(0);
        assertEquals(1, post.getPostId());
        assertEquals("title", post.getTitle());
    }

    @Test
    public void getPostsByTag() {
         List<Post> posts = testService.getPostsByTag(1, Permission.MANAGER);
         assertEquals(1, posts.size());
         Post post = posts.get(0);
         HashTag tag = post.getHashtags().get(0);
         assertEquals(1, tag.getHashTagId());
         assertEquals("tag", tag.getHashTagName());
    }

    @Test
    public void getPostsByNotExistingTag() {
         List<Post> posts = testService.getPostsByTag(2, Permission.MANAGER);
         assertTrue(posts.isEmpty());
    }

    @Test
    public void getPostsByContent() {
        List<Post> posts = testService.getPostsByContent("content", Permission.MANAGER);
         assertEquals(1, posts.size());
         Post post = posts.get(0);
         assertEquals("content", post.getPostContent());
    }

    @Test
    public void getNotApprovedPostsManager() {
        try {
            List<Post> posts = testService.getNotApprovedPosts(Permission.MANAGER);
            assertEquals(1, posts.size());
            Post post = posts.get(0);
            assertEquals(1, post.getPostId());
            assertEquals("title", post.getTitle());
            assertEquals(Status.IN_WORK, post.getStatus());
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for manager");
        }
    }

    @Test
    public void getNotApprovedPostsAdmin() {
        try {
            List<Post> posts = testService.getNotApprovedPosts(Permission.ADMIN);
            assertEquals(1, posts.size());
            Post post = posts.get(0);
            assertEquals(1, post.getPostId());
            assertEquals("title", post.getTitle());
            assertEquals(Status.IN_WORK, post.getStatus());
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for admin");
        }
    }

    @Test
    public void getNotApprovedPostsUser() {
        assertThrows(AuthorizationException.class, ()-> testService.getNotApprovedPosts(Permission.USER));
    }

    @Test
    public void getRejectedPostsUser() {
        assertThrows(AuthorizationException.class, ()-> testService.getNotApprovedPosts(Permission.USER));
    }

    @Test
    public void getRejectedPostsManager() {
        try {
            List<RejectedPost> posts = testService.getRejectedPosts(Permission.MANAGER);
            assertEquals(1, posts.size());
            RejectedPost post = posts.get(0);
            assertEquals(1, post.getPostId());
            assertEquals("title", post.getTitle());
            assertEquals(Status.REJECTED, post.getStatus());
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for manager");
        }
    }

    @Test
    public void approvePostUser() {
        assertThrows(AuthorizationException.class, ()->testService.approvePost(1, Permission.USER));
    }

    @Test
    public void approvePostManager() {
        assertThrows(AuthorizationException.class, ()->testService.approvePost(1, Permission.MANAGER));
    }

    @Test
    public void approvePostAdmin() {
        try {
            testService.approvePost(1, Permission.ADMIN);
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for admin");
        }
    }

    @Test
    public void rejectPostUser() {
        assertThrows(
                AuthorizationException.class,
                ()->testService.rejectPost(1, "reason", Permission.USER));
    }

    @Test
    public void rejectPostManager() {
        assertThrows(
                AuthorizationException.class,
                ()->testService.rejectPost(1, "reason", Permission.MANAGER));
    }

    @Test
    public void rejectPostAdmin() {
        try {
            testService.rejectPost(1, "reason", Permission.ADMIN);
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for admin");
        }
    }

    @Test
    public void sendToApproveManager() {
        try {
            testService.sendToApprove(1, Permission.MANAGER);
        } catch (AuthorizationException ex) {
            fail("Should not throw exception for manager");
        }
    }

    @Test
    public void sendToApproveUser() {
        assertThrows(
                AuthorizationException.class,
                ()->testService.sendToApprove(1, Permission.USER));
    }

     @Test
    public void sendToApproveUAdmin() {
        assertThrows(
                AuthorizationException.class,
                ()->testService.sendToApprove(1, Permission.ADMIN));
    }

    @Test
    public void getUserByEmail() {
        User user = testService.getUserByEmail("existing");
        assertNotNull(user);
        assertEquals("name", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals("existing", user.getEmail());
    }

    @Test
    public void getNotUserByEmail() {
        User user = testService.getUserByEmail("notExisting");
        assertNull(user);
    }

    @Test
    public void getUserByName() {
        User user = testService.getUserByName("existing");
        assertNotNull(user);
        assertEquals("email", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("existing", user.getName());
    }

    @Test
    public void getNotUserByName() {
        User user = testService.getUserByName("notExisting");
        assertNull(user);
    }
}
