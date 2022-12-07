package cms.blog.dao;

import cms.blog.dto.HashTag;
import cms.blog.dto.Post;
import cms.blog.dto.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HashtagDaoDbImplTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    HashtagDao hashtagDao;

    @Autowired
    PostDao postDao;

    @Before
    public void setUp() {
        final String DELETE_REJECTION_REASON = "DELETE FROM rejectionReason;";
        final String DELETE_POST_TAG= "DELETE FROM postTag;";
        final String DELETE_POST = "DELETE FROM post;";
        final String DELETE_HASHTAG = "DELETE FROM hashTag;";


        jdbcTemplate.update(DELETE_REJECTION_REASON);
        jdbcTemplate.update(DELETE_POST_TAG);
        jdbcTemplate.update(DELETE_POST);
        jdbcTemplate.update(DELETE_HASHTAG);
    }

    @Test
    public void getTagByNameAndAddTag() {
        String tagName = "tag";
        assertNull(hashtagDao.getTagByName(tagName));
        HashTag tag = new HashTag(tagName);
        HashTag addedTag = hashtagDao.addTag(tag);
        assertEquals(tagName, addedTag.getHashTagName());
        assertEquals(tag, addedTag);
        assertEquals(addedTag, hashtagDao.getTagByName(tagName));
    }

    @Test
    public void addTagForPost() {
        HashTag tag = hashtagDao.addTag(new HashTag("tag"));
        Post post = new Post();
        post.setTitle("title");
        post.setPostContent("content");
        post.setStatus(Status.APPROVED);
        post = postDao.addPost(post);
        post = postDao.getPostById(post.getPostId());
        assertTrue(post.getHashtags().isEmpty());
        hashtagDao.addTagForPost(tag, post.getPostId());
        post = postDao.getPostById(post.getPostId());
        assertFalse(post.getHashtags().isEmpty());
        assertEquals(tag, post.getHashtags().get(0));
        assertEquals(1, post.getHashtags().size());
    }

    @Test
    public void deleteTagForPost() {
        HashTag tag = hashtagDao.addTag(new HashTag("tag"));
        Post post = new Post();
        post.setTitle("title");
        post.setPostContent("content");
        post.setStatus(Status.APPROVED);
        post = postDao.addPost(post);
        hashtagDao.addTagForPost(tag, post.getPostId());
        post = postDao.getPostById(post.getPostId());
        assertFalse(post.getHashtags().isEmpty());
        assertEquals(1, post.getHashtags().size());
        assertEquals(tag, post.getHashtags().get(0));
        hashtagDao.deleteTagForPost(tag.getHashTagId(), post.getPostId());
        post = postDao.getPostById(post.getPostId());
        assertTrue(post.getHashtags().isEmpty());
    }

}