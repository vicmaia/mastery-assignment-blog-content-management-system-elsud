package cms.blog.dao;

import cms.blog.dto.Hashtag;
import cms.blog.dto.Post;
import cms.blog.dto.Status;
import org.junit.Before;
import org.junit.Test;
//import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

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
    public void getTagByNameAndAddTag() {
        String tagName = "tag";
        assertNull(hashtagDao.getTagByName(tagName));
        Hashtag tag = new Hashtag(tagName);
        Hashtag addedTag = hashtagDao.addTag(tag);
        assertEquals(tagName, addedTag.getName());
        assertEquals(tag, addedTag);
        assertEquals(addedTag, hashtagDao.getTagByName(tagName));
    }

    @Test
    public void addTagForPost() {
        Hashtag tag = hashtagDao.addTag(new Hashtag("tag"));
        Post post = new Post();
        post.setTitle("title");
        post.setPostContent("content");
        post.setStatus(Status.APPROVED);
        post = postDao.addPost(post);
        post = postDao.getPostById(post.getId());
        assertTrue(post.getHashtags().isEmpty());
        hashtagDao.addTagForPost(tag, post.getId());
        post = postDao.getPostById(post.getId());
        assertFalse(post.getHashtags().isEmpty());
        assertEquals(tag, post.getHashtags().get(0));
        assertEquals(1, post.getHashtags().size());
    }

    @Test
    public void deleteTagForPost() {
        Hashtag tag = hashtagDao.addTag(new Hashtag("tag"));
        Post post = new Post();
        post.setTitle("title");
        post.setPostContent("content");
        post.setStatus(Status.APPROVED);
        post = postDao.addPost(post);
        hashtagDao.addTagForPost(tag, post.getId());
        post = postDao.getPostById(post.getId());
        assertFalse(post.getHashtags().isEmpty());
        assertEquals(1, post.getHashtags().size());
        assertEquals(tag, post.getHashtags().get(0));
        hashtagDao.deleteTagForPost(tag.getId(), post.getId());
        post = postDao.getPostById(post.getId());
        assertTrue(post.getHashtags().isEmpty());
    }

}