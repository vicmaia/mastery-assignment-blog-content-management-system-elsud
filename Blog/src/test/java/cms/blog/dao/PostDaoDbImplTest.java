package cms.blog.dao;

import cms.blog.dto.Hashtag;
import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;
import cms.blog.dto.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PostDaoDbImplTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    HashtagDao hashtagDao;

    @Autowired
    PostDao postDao;

    @Before
    public void setUp() {
        final String DELETE_REJECTIONREASON = "DELETE FROM rejectionreason;";
        final String DELETE_POSTTAG= "DELETE FROM posttag;";
        final String DELETE_POST = "DELETE FROM post;";
        final String DELETE_HASHTAG = "DELETE FROM hashtag;";

        jdbcTemplate.update(DELETE_REJECTIONREASON);
        jdbcTemplate.update(DELETE_POSTTAG);
        jdbcTemplate.update(DELETE_POST);
        jdbcTemplate.update(DELETE_HASHTAG);
    }


    @Test
    public void addAndGetPost() {
        Post post = createPost();
        Post addedPost = postDao.addPost(post);
        Post receivedPost = postDao.getPostById(addedPost.getId());
        assertEquals(post.getTitle(), addedPost.getTitle());
        assertEquals(post.getTitle(), receivedPost.getTitle());
        assertEquals(post.getPostContent(), addedPost.getPostContent());
        assertEquals(post.getPostContent(), receivedPost.getPostContent());
        assertEquals(post.getStatus(), addedPost.getStatus());
        assertEquals(post.getStatus(), receivedPost.getStatus());
    }

    @Test
    public void editPost() {
        Post post = postDao.addPost(createPost());
        Post receivedPost = postDao.getPostById(post.getId());
        assertEquals(post.getTitle(), receivedPost.getTitle());
        assertEquals(post.getStatus(), receivedPost.getStatus());
        assertEquals(post.getPostContent(), receivedPost.getPostContent());
        post.setExpireDate(LocalDate.parse("2022-12-12"));
        post.setTitle("new");
        assertTrue(postDao.editPost(post));
        Post editedPost = postDao.getPostById(post.getId());
        assertEquals(post.getTitle(), editedPost.getTitle());
        assertEquals(post.getPostContent(), editedPost.getPostContent());
        assertEquals(post.getStatus(), editedPost.getStatus());
        assertEquals(post.getExpireDate(), editedPost.getExpireDate());
        assertNotEquals(receivedPost.getTitle(), editedPost.getTitle());
        assertNotEquals(receivedPost.getExpireDate(), editedPost.getExpireDate());
        assertEquals(receivedPost.getId(), editedPost.getId());
    }

    @Test
    public void deleteNotExistingPost() {
        assertFalse(postDao.deletePost(3));
    }

    @Test
    public void deleteExistingPost() {
        Post post = postDao.addPost(createPost());
        int postId = post.getId();
        assertNotNull(postDao.getPostById(postId));
        assertTrue(postDao.deletePost(postId));
        assertNull(postDao.getPostById(postId));
    }

    @Test
    public void deletePostWithTags() {
        Post post = createPost();
        post = postDao.addPost(post);
        Hashtag tag = hashtagDao.addTag(new Hashtag("tag"));
        hashtagDao.addTagForPost(tag, post.getId());
        post = postDao.getPostById(post.getId());
        assertNotNull(post);
        assertEquals(1, post.getHashtags().size());
        assertTrue(post.getHashtags().contains(tag));
        assertTrue(postDao.deletePost(post.getId()));
        assertNull(postDao.getPostById(post.getId()));
        assertNotNull(hashtagDao.getTagByName(tag.getName()));
    }

    @Test
    public void deleteRejectedPost() {
        Post post = createPost();
        post.setStatus(Status.IN_WORK);
        post = postDao.addPost(post);
        postDao.rejectPost(post.getId(), "reason");
        post = postDao.getPostById(post.getId());
        assertEquals(Status.REJECTED, post.getStatus());
        List<RejectedPost> rejectedPosts = postDao.getRejectedPosts();
        assertEquals(1, rejectedPosts.size());
        RejectedPost rejected = postDao.getRejectedPostById(post.getId());
        assertTrue(rejectedPosts.contains(rejected));
        assertEquals("reason", rejected.getReason());
        postDao.deletePost(post.getId());
        assertNull(postDao.getPostById(post.getId()));
        assertNull(postDao.getRejectedPostById(post.getId()));
        assertTrue(postDao.getRejectedPosts().isEmpty());
    }


    @Test
    public void getApprovedPostsForAdmin() {
        Post post1 = createPost();
        Post post2 = createPost();
        Post post3 = createPost();
        post1.setExpireDate(LocalDate.now().minusDays(1));
        post2.setPublishDate(LocalDate.now().plusDays(1));
        post3.setStatus(Status.REJECTED);
        postDao.addPost(post1);
        postDao.addPost(post2);
        postDao.addPost(post3);
        List<Post> approvedPosts = postDao.getApprovedPostsForAdmin();
        assertEquals(2, approvedPosts.size());
        assertTrue(approvedPosts.contains(post1));
        assertTrue(approvedPosts.contains(post2));
        assertFalse(approvedPosts.contains(post3));
    }

    @Test
    public void getApprovedPostsForUser() {
        Post post1 = createPost();
        Post post2 = createPost();
        Post post3 = createPost();
        Post post4 = createPost();
        post1.setExpireDate(LocalDate.now().minusDays(1));
        post2.setPublishDate(LocalDate.now().plusDays(1));
        post3.setStatus(Status.REJECTED);
        postDao.addPost(post1);
        postDao.addPost(post2);
        postDao.addPost(post3);
        postDao.addPost(post4);
        List<Post> approvedPosts = postDao.getApprovedPostsForUser();
        assertEquals(1, approvedPosts.size());
    }

    @Test
    public void getPostsByTagForAdmin() {
        Post post1 = createPost();
        Post post2 = createPost();
        post2.setTitle("new");
        post2.setStatus(Status.REJECTED);
        post1 = postDao.addPost(post1);
        post2 = postDao.addPost(post2);
        Hashtag tag1 = hashtagDao.addTag(new Hashtag("tag"));
        Hashtag tag2 = hashtagDao.addTag(new Hashtag("another tag"));
        hashtagDao.addTagForPost(tag1, post1.getId());
        hashtagDao.addTagForPost(tag2, post1.getId());
        hashtagDao.addTagForPost(tag2, post2.getId());
        List<Post> posts = postDao.getPostsByTagForAdmin(tag1.getId());
        assertEquals(1, posts.size());
        assertTrue(posts.contains(post1));
        assertFalse(posts.contains(post2));
        posts = postDao.getPostsByTagForAdmin(tag2.getId());
        assertEquals(2, posts.size());
        assertTrue(posts.contains(post1));
        assertTrue(posts.contains(post2));
        assertTrue(postDao.getPostsByTagForAdmin(23).isEmpty());
    }

    @Test
    public void getPostsByTagForUser() {
        Post post1 = createPost();
        Post post2 = createPost();
        post2.setTitle("new");
        post1 = postDao.addPost(post1);
        post2 = postDao.addPost(post2);
        Hashtag tag1 = hashtagDao.addTag(new Hashtag("tag"));
        Hashtag tag2 = hashtagDao.addTag(new Hashtag("another tag"));
        hashtagDao.addTagForPost(tag1, post1.getId());
        hashtagDao.addTagForPost(tag2, post1.getId());
        hashtagDao.addTagForPost(tag2, post2.getId());
        List<Post> posts = postDao.getPostsByTagForUser(tag1.getId());
        assertEquals(1, posts.size());
        assertTrue(posts.contains(post1));
        assertFalse(posts.contains(post2));
        posts = postDao.getPostsByTagForUser(tag2.getId());
        assertEquals(2, posts.size());
        assertTrue(posts.contains(post1));
        assertTrue(posts.contains(post2));
        assertTrue(postDao.getPostsByTagForUser(23).isEmpty());
        post1.setPublishDate(LocalDate.now().plusDays(2));
        post2.setExpireDate(LocalDate.now().minusDays(2));;
        postDao.editPost(post1);
        postDao.editPost(post2);
        assertTrue(postDao.getPostsByTagForUser(tag2.getId()).isEmpty());
    }

    @Test
    public void getPostsByContentForAdmin() {
        Post post1 = createPost();
        Post post2 = createPost();
        post2.setStatus(Status.IN_WORK);
        post1.setPublishDate(LocalDate.now().plusDays(2));
        post1 = postDao.addPost(post1);
        post2 = postDao.addPost(post2);
        List<Post> posts = postDao.getPostsByContentForAdmin("title");
        assertEquals(2, posts.size());
        assertTrue(posts.contains(post1));
        assertTrue(posts.contains(post2));
        posts = postDao.getPostsByContentForAdmin("not existing");
        assertTrue(posts.isEmpty());
        posts = postDao.getPostsByContentForAdmin("content");
        assertEquals(2, posts.size());
        assertTrue(posts.contains(post1));
        assertTrue(posts.contains(post2));
        post2. setTitle("new");
        postDao.editPost(post2);
        posts = postDao.getPostsByContentForAdmin("title");
        assertEquals(1, posts.size());
        assertTrue(posts.contains(post1));
        posts = postDao.getPostsByContentForAdmin("new");
        assertEquals(1, posts.size());
        assertTrue(posts.contains(post2));
    }

    @Test
    public void getPostsByContentForUser() {
        Post post1 = createPost();
        Post post2 = createPost();
        post1 = postDao.addPost(post1);
        post2 = postDao.addPost(post2);
        List<Post> posts = postDao.getPostsByContentForUser("title");
        assertEquals(2, posts.size());
        assertTrue(posts.contains(post1));
        assertTrue(posts.contains(post2));
        posts = postDao.getPostsByContentForUser("not existing");
        assertTrue(posts.isEmpty());
        posts = postDao.getPostsByContentForUser("content");
        assertEquals(2, posts.size());
        assertTrue(posts.contains(post1));
        assertTrue(posts.contains(post2));
        post2. setTitle("new");
        postDao.editPost(post2);
        posts = postDao.getPostsByContentForUser("title");
        assertEquals(1, posts.size());
        assertTrue(posts.contains(post1));
        posts = postDao.getPostsByContentForUser("new");
        assertEquals(1, posts.size());
        assertTrue(posts.contains(post2));
        post2.setExpireDate(LocalDate.now().minusDays(2));
        post1.setPublishDate(LocalDate.now().plusDays(2));
        postDao.editPost(post1);
        postDao.editPost(post2);
        assertTrue(postDao.getPostsByContentForUser("content").isEmpty());
    }

    @Test
    public void getNotApprovedPosts() {
        Post post1 = createPost();
        Post post2 = createPost();
        post2.setStatus(Status.IN_WORK);
        post2.setTitle("new");
        postDao.addPost(post1);
        postDao.addPost(post2);
        List<Post> posts = postDao.getNotApprovedPosts();
        assertEquals(1, posts.size());
        assertTrue(posts.contains(post2));
        assertFalse(posts.contains(post1));
    }

    @Test
    public void getRejectedPosts() {
        Post post1 = createPost();
        Post post2 = createPost();
        //post2.setStatus(Status.REJECTED);
        post2.setTitle("new");
        postDao.addPost(post1);
        post2 = postDao.addPost(post2);
        List<RejectedPost> posts = postDao.getRejectedPosts();
        assertEquals(0, posts.size());
        postDao.rejectPost(post2.getId(), "reason");
        posts = postDao.getRejectedPosts();
        assertEquals(1, posts.size());
        assertEquals(post2.getTitle(), posts.get(0).getTitle());
        assertEquals(post2.getId(), posts.get(0).getId());
        assertEquals(post2.getPostContent(), posts.get(0).getPostContent());
    }

    @Test
    public void approvePost() {
        Post post = createPost();
        post.setStatus(Status.IN_WORK);
        post = postDao.addPost(post);
        post = postDao.getPostById(post.getId());
        assertTrue(postDao.getNotApprovedPosts().contains(post));
        assertEquals(Status.IN_WORK, post.getStatus());
        postDao.approvePost(post.getId(), LocalDateTime.now());
        post = postDao.getPostById(post.getId());
        assertTrue(postDao.getNotApprovedPosts().isEmpty());
        assertEquals(Status.APPROVED, post.getStatus());

    }

    @Test
    public void rejectPostAndGetRejected() {
        Post post = createPost();
        post.setStatus(Status.IN_WORK);
        post = postDao.addPost(post);
        post = postDao.getPostById(post.getId());
        assertTrue(postDao.getNotApprovedPosts().contains(post));
        assertEquals(Status.IN_WORK, post.getStatus());
        postDao.rejectPost(post.getId(), "reason");
        post = postDao.getPostById(post.getId());
        assertTrue(postDao.getNotApprovedPosts().isEmpty());
        assertEquals(Status.REJECTED, post.getStatus());
        List<RejectedPost> rejectedPosts = postDao.getRejectedPosts();
        assertEquals(1, rejectedPosts.size());
        RejectedPost rejected = postDao.getRejectedPostById(post.getId());
        assertTrue(rejectedPosts.contains(rejected));
        assertEquals("reason", rejected.getReason());
    }

    @Test
    public void sendToApprove() {
        Post post = createPost();
        post.setStatus(Status.IN_WORK);
        post = postDao.addPost(post);
        assertTrue(postDao.getNotApprovedPosts().contains(post));
        assertEquals(Status.IN_WORK, post.getStatus());
        postDao.rejectPost(post.getId(), "reason");
        post = postDao.getPostById(post.getId());
        assertTrue(postDao.getNotApprovedPosts().isEmpty());
        assertEquals(Status.REJECTED, post.getStatus());
        postDao.sendToApprove(post.getId());
        assertNull(postDao.getRejectedPostById(post.getId()));
        assertFalse(postDao.getNotApprovedPosts().isEmpty());
        post = postDao.getPostById(post.getId());
        assertTrue(postDao.getNotApprovedPosts().contains(post));
        assertEquals(Status.IN_WORK, post.getStatus());
    }

    private Post createPost() {
        Post post = new Post();
        post.setTitle("title");
        post.setPostContent("content");
        post.setStatus(Status.APPROVED);
        return post;
    }
}