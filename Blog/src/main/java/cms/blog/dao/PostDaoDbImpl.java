package cms.blog.dao;

import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;
import cms.blog.dto.Status;
import cms.blog.dto.HashTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;

@Repository
@Profile("prod")
public class PostDaoDbImpl implements PostDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Add given post to database. Post must have status, title and text.
     * @param post instance of new Post object
     * @return created post with set id
     */
    @Override
    public Post addPost(Post post) {
        final String GET_STATUS = "SELECT statusId FROM status WHERE statusName = ?;";
        int statusId = jdbcTemplate.queryForObject(GET_STATUS, Integer.class, post.getStatus().toString());

        final String ADD_POST = "INSERT INTO post (creationTime, title, descriptionField, postContent, publishDate, "
                + "expireDate, statusId) VALUES (?, ?, ?, ?, ?, ?, ?);";
        Date publishDate = null;
        Date expireDate = null;
        Timestamp creationTime = null;
        if (post.getCreationTime() != null) {
            creationTime = Timestamp.valueOf(post.getCreationTime());
        }
        if (post.getPublishDate() != null) {
            publishDate = Date.valueOf(post.getPublishDate());
        }
        if (post.getExpireDate() != null) {
            expireDate = Date.valueOf(post.getExpireDate());
        }
        jdbcTemplate.update(
                ADD_POST, creationTime, post.getTitle(), post.getDescription(),
                post.getPostContent(), publishDate, expireDate, statusId);
        int postId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        post.setPostId(postId);
        return post;
    }

    /**
     * Update given values of post in database. Post must have id, title and text.
     * @param post Updated instance of post
     * @return boolean if post was updated
     */
    @Override
    public boolean editPost(Post post) {
        final String UPDATE_POST = "UPDATE post SET creationTime=?, title=?, descriptionField=?, postContent=?, "
                + "publishDate=?, expireDate=?, editTime=? WHERE postId=?";
        //final String UPDATE_POST_WITH_STATUS = "UPDATE post SET creationTime=?, title=?, descriptionField=?, postContent=?, "
        //        + "publishDate=?, expireDate=?, editTime=?, statusId=? WHERE postId=?";
        Date publishDate = null;
        Date expireDate = null;
        Timestamp creationTime = null;
        Timestamp editTime = null;

        if (post.getCreationTime() != null) {
            creationTime = Timestamp.valueOf(post.getCreationTime());
        }
        if (post.getPublishDate() != null) {
            publishDate = Date.valueOf(post.getPublishDate());
        }
        if (post.getExpireDate() != null) {
            expireDate = Date.valueOf(post.getExpireDate());
        }
        if (post.getEditTime() != null) {
            editTime = Timestamp.valueOf(post.getEditTime());
        }
        //if (post.getStatus() == Status.REJECTED) {
        //    int status = sendToApprove(post.getPostId());
        //    return jdbcTemplate.update(
        //        UPDATE_POST_WITH_STATUS, creationTime, post.getTitle(), post.getDescription(),
        //        post.getPostContent(), publishDate, expireDate, editTime, status, post.getPostId()) > 0;
        //}

        return jdbcTemplate.update(
                UPDATE_POST, creationTime, post.getTitle(), post.getDescription(),
                post.getPostContent(), publishDate, expireDate, editTime, post.getPostId()) > 0;
    }

    /**
     * Delete post with given id from database if such post exists.
     * Delete relationships between this post and postTag, rejectedPost as well.
     * @param id int, id of the post to delete
     * @return boolean if post was deleted
     */
    @Override
    @Transactional
    public boolean deletePost(int id ) {
        final String DELETE_TAG_CONN = "DELETE FROM postTag WHERE postId = ?;";
        final String DELETE_REJECTED_POST = "DELETE FROM rejectionReason WHERE postId = ?;";
        final String DELETE_POST = "DELETE FROM post WHERE postId = ?;";
        jdbcTemplate.update(DELETE_TAG_CONN, id);
        jdbcTemplate.update(DELETE_REJECTED_POST, id);
        return jdbcTemplate.update(DELETE_POST, id) > 0;
    }

    /**
     * Search post with given id as well as tags for this post
     * @param id int with id of the post to display
     * @return Post with given id
     */
    @Override
    public Post getPostById(int id) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status "
                + "ON post.statusId = status.statusId WHERE post.postId = ?;";
        try {
            Post post = jdbcTemplate.queryForObject(GET_SQL, new PostMapper(), id);
            post.setTags(getTags(id));
            return post;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /**
     * Search post with rejected status by given id as well as tags and rejection reason for it.
     * @param id int with id of rejected post
     * @return RejectedPost with given id
     */
    @Override
    public RejectedPost getRejectedPostById(int id) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.statusId = status.statusId "
                + "INNER JOIN rejectionReason ON rejectionReason.postId = post.postId WHERE post.postId = ?;";
        try {
            RejectedPost post =  jdbcTemplate.queryForObject(GET_SQL, new RejectedPostMapper(), id);
            post.setTags(getTags(id));
            return post;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /**
     * Get all posts with approved status with any creationTime and expireDate, sorted by creationTime
     * @return List of found Posts
     */
    @Override
    public List<Post> getApprovedPostsForAdmin() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.statusId = status.statusId "
                + "WHERE status.statusName = ? ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), Status.APPROVED.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }

    /**
     * Get all posts supposed to be visible by users (with approved status, publishDate in past or null
     * and expireDate in future or null) sorted by creationTime
     * @return List of found Posts
     */
    @Override
    public List<Post> getApprovedPostsForUser() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON "
                + "post.statusId = status.statusId WHERE status.statusName = ? "
                + "AND (post.publishDate IS NULL OR post.publishDate <= ?) "
                + "AND (post.expireDate IS NULL OR post.expireDate >= ?) ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), Status.APPROVED.toString(),
                Timestamp.valueOf(LocalDate.now().atStartOfDay()),
                Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }

    /**
     * Get all posts for given tag with any publishDate and expireDate, sorted by creationTime
     * @return List of found Posts
     */
    @Override
    public List<Post> getPostsByTagForAdmin(int tagId) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.statusId = status.statusId "
                + "INNER JOIN postTag ON post.postId = postTag.postId WHERE postTag.hashTagId = ? "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), tagId);
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }

    /**
     * Get all posts for given tag supposed to be visible by users (with approved status, publishDate in past or null
     * and expireDate in future or null) sorted by creationTime
     * @return List of found Posts
     */
    @Override
    public List<Post> getPostsByTagForUser(int tagId) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.statusId = status.statusId "
                + "INNER JOIN postTag ON post.postId = postTag.postId WHERE postTag.hashTagId = ? and status.statusName = ? "
                + "AND (post.publishDate IS NULL OR post.publishDate <= ?) AND (post.expireDate IS NULL OR post.expireDate >= ?) "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), tagId, Status.APPROVED.toString(),
                Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }

    /**
     * Get all posts with any publishDate and expireDate, which contain given content in title or in text
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<Post> getPostsByContentForAdmin(String content) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.statusId = status.statusId "
                + "WHERE (post.title LIKE ? OR post.postContent LIKE ?) "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), "%" + content + "%", "%" + content + "%");
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }

    /**
     * Get all posts supposed to be visible by users (with approved status, publishDate in past or null
     * and expireDate in future or null) which title or text contains given content
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<Post> getPostsByContentForUser(String content) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.statusId = status.StatusId "
                + "WHERE (post.title LIKE ? OR post.postContent LIKE ?) AND status.statusName = ? "
                + "AND (post.publishDate IS NULL OR post.publishDate <= ?) AND (post.expireDate IS NULL OR post.expireDate >= ?) "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), "%" + content + "%", "%" + content + "%", Status.APPROVED.toString(),
                Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }


    /**
     * Get List of Posts which status is in_work. Supposed to be visible only by admin and manager.
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<Post> getNotApprovedPosts() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.statusId = status.statusId "
                + "WHERE status.statusName = ? ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), Status.IN_WORK.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }

    /**
     * Get List of Posts which status is rejected. Suppose to be visible only by manager
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<RejectedPost> getRejectedPosts() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN rejectionReason ON rejectionReason.postId = post.postId "
                + "ORDER BY post.creationTime DESC;";
        List<RejectedPost> posts =  jdbcTemplate.query(GET_SQL, new RejectedPostMapper());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getPostId())));
        return posts;
    }

    /**
     * Change status of post to approved
     * @param postId int with id of post to approve
     * @param creationTime LocalDateTime with time that should be set as creationTime
     *                     (can be now or publishDate for scheduled posts)
     */
    @Override
    public void approvePost(int postId, LocalDateTime creationTime) {
        final String GET_STATUS_ID = "SELECT statusId FROM status WHERE statusName = ?;";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.APPROVED.toString());

        final String APPROVE_SQL = "UPDATE post SET statusId=?, creationTime=? WHERE postId = ?;";

        jdbcTemplate.update(APPROVE_SQL, statusId, Timestamp.valueOf(creationTime), postId);
    }

    /**
     * Change status of post to rejected and save the reason of rejection
     * @param postId int with id of post that should be rejected
     * @param reason String with reason of rejection
     */
    @Override
    @Transactional
    public void rejectPost(int postId, String reason) {
        final String GET_STATUS_ID = "SELECT statusId FROM status WHERE statusName = ?;";
        final String REJECT_SQL = "UPDATE post SET statusId=? WHERE postId = ?;";
        final String ADD_REASON = "INSERT INTO rejectionReason (postId, rejectionReason) VALUES (?,?);";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.REJECTED.toString());
        try {
            jdbcTemplate.update(REJECT_SQL, statusId, postId);
            jdbcTemplate.update(ADD_REASON, postId, reason);
        } catch (DataAccessException ex) {
        }
    }

    /**
     * Modify status of rejected post to in_work. Supposed to be called by manager only
     * @param postId int with id of rejected post that was refined and should be checked by admin again
     */
    @Transactional
    @Override
    public void sendToApprove(int postId) {
        final String GET_STATUS_ID = "SELECT statusId FROM status WHERE statusName = ?;";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.IN_WORK.toString());

        final String UPDATE_STATUS_SQL = "UPDATE post SET statusId=? WHERE postId = ?;";
        jdbcTemplate.update(UPDATE_STATUS_SQL, statusId, postId);

        final String DELETE_REJECTED = "DELETE FROM rejectionReason WHERE postId = ?;";
        jdbcTemplate.update(DELETE_REJECTED, postId);
        //return statusId;
    }

    /**
     * Get all tags for post with given id
     * @param postId int - id of the post
     * @return List of related Tags
     */
    private List<HashTag> getTags(int postId) {
        final String GET_SQL = "SELECT hashTag.* FROM hashTag INNER JOIN postTag "
                + "ON postTag.hashTagId = hashTag.hashTagId WHERE postTag.postId = ?;";
        return jdbcTemplate.query(GET_SQL, new HashtagDaoDbImpl.TagMapper(), postId);
    }

    private static final class PostMapper implements RowMapper<Post> {
        @Override
        public Post mapRow(ResultSet resultSet, int i) throws SQLException {
            Post post  = new Post();
            post.setTitle(resultSet.getString("post.title"));
            post.setPostId(resultSet.getInt("post.postId"));
            post.setDescription(resultSet.getString("post.descriptionField"));
            post.setPostContent(resultSet.getString("post.postContent"));
            String statusName = resultSet.getString("status.statusName");
            post.setStatus(Status.valueOf(statusName.toUpperCase()));
            Date publishDate = resultSet.getDate("post.publishDate");
            if (publishDate != null) {
                post.setPublishDate(publishDate.toLocalDate());
            }
            Date expireDate = resultSet.getDate("post.expireDate");
            if (expireDate != null) {
                post.setExpireDate(expireDate.toLocalDate());
            }
            Timestamp creationTime = resultSet.getTimestamp("post.creationTime");
            if (creationTime != null) {
                post.setCreationTime(creationTime.toLocalDateTime());
            }
            Timestamp editTime = resultSet.getTimestamp("post.editTime");
            if (editTime != null) {
                post.setEditTime(editTime.toLocalDateTime());
            }
            return post;
        }
    }

    private static final class RejectedPostMapper implements RowMapper<RejectedPost> {
        @Override
        public RejectedPost mapRow(ResultSet resultSet, int i) throws SQLException {
            RejectedPost post  = new RejectedPost();
            post.setTitle(resultSet.getString("post.title"));
            post.setPostId(resultSet.getInt("post.postId"));
            post.setDescription(resultSet.getString("post.descriptionField"));
            post.setPostContent(resultSet.getString("post.postContent"));
            post.setStatus(Status.REJECTED);
            Date publishDate = resultSet.getDate("post.publishDate");
            if (publishDate != null) {
                post.setPublishDate(publishDate.toLocalDate());
            }
            Date expireDate = resultSet.getDate("post.expireDate");
            if (expireDate != null) {
                post.setExpireDate(expireDate.toLocalDate());
            }
            Timestamp creationTime = resultSet.getTimestamp("post.creationTime");
            if (creationTime != null) {
                post.setCreationTime(creationTime.toLocalDateTime());
            }
            Timestamp editTime = resultSet.getTimestamp("post.editTime");
            if (editTime != null) {
                post.setEditTime(editTime.toLocalDateTime());
            }
            post.setReason(resultSet.getString("rejectionReason.rejectionReason"));
            return post;
        }
    }
}
