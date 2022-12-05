package cms.blog.dao;

import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;
import cms.blog.dto.Status;
import cms.blog.dto.Tag;
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
        final String GET_STATUS = "SELECT id FROM status WHERE name = ?;";
        int statusId = jdbcTemplate.queryForObject(GET_STATUS, Integer.class, post.getStatus().toString());

        final String ADD_POST = "INSERT INTO post (creationTime, title, description, text, publishDate, "
                + "expireDate, status) VALUES (?, ?, ?, ?, ?, ?, ?);";
        Timestamp publishDate = null;
        Timestamp expireDate = null;
        Timestamp creationTime = null;
        if (post.getCreationTime() != null) {
            creationTime = Timestamp.valueOf(post.getCreationTime());
        }
        if (post.getPublishDate() != null) {
            publishDate = Timestamp.valueOf(post.getPublishDate().atStartOfDay());
        }
        if (post.getExpireDate() != null) {
            expireDate = Timestamp.valueOf(post.getExpireDate().atStartOfDay());
        }
        jdbcTemplate.update(
                ADD_POST, creationTime, post.getTitle(), post.getDescription(),
                post.getText(), publishDate, expireDate, statusId);
        int postId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        post.setId(postId);
        return post;
    }

    /**
     * Update given values of post in database. Post must have id, title and text.
     * @param post Updated instance of post
     * @return boolean if post was updated
     */
    @Override
    public boolean editPost(Post post) {
        final String UPDATE_POST = "UPDATE post SET creationTime=?, title=?, description=?, text=?, "
                + "publishDate=?, expireDate=?, editTime=? WHERE id=?";
        Timestamp publishDate = null;
        Timestamp expireDate = null;
        Timestamp creationTime = null;
        Timestamp editTime = null;
        if (post.getCreationTime() != null) {
            creationTime = Timestamp.valueOf(post.getCreationTime());
        }
        if (post.getPublishDate() != null) {
            publishDate = Timestamp.valueOf(post.getPublishDate().atStartOfDay());
        }
        if (post.getExpireDate() != null) {
            expireDate = Timestamp.valueOf(post.getExpireDate().atStartOfDay());
        }
        if (post.getEditTime() != null) {
            editTime = Timestamp.valueOf(post.getEditTime());
        }
        boolean updated = jdbcTemplate.update(
                UPDATE_POST, creationTime, post.getTitle(), post.getDescription(),
                post.getText(), publishDate, expireDate, editTime, post.getId()) > 0;
        return updated;
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
        final String DELETE_REJECTED_POST = "DELETE FROM rejectedPost WHERE id = ?;";
        final String DELETE_POST = "DELETE FROM post WHERE id = ?;";
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
                + "ON post.status = status.id WHERE post.id = ?;";
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
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "INNER JOIN rejectedPost ON rejectedPost.id = post.id WHERE post.id = ?;";
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
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "WHERE status.name = ? ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), Status.APPROVED.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    /**
     * Get all posts supposed to be visible by users (with approved status, publishDate in past or null
     * and expireDate in future or null) sorted by creationTime
     * @return List of found Posts
     */
    @Override
    public List<Post> getApprovedPostsForUser() {
        // was like
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON "
                + "post.status = status.id WHERE status.name = ? "
                + "AND (post.publishDate IS NULL OR post.publishDate <= ?) "
                + "AND (post.expireDate IS NULL OR post.expireDate >= ?) ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), Status.APPROVED.toString(),
                Timestamp.valueOf(LocalDate.now().atStartOfDay()),
                Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    /**
     * Get all posts for given tag with any publishDate and expireDate, sorted by creationTime
     * @return List of found Posts
     */
    @Override
    public List<Post> getPostsByTagForAdmin(int tagId) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "INNER JOIN postTag ON post.id = postTag.postId WHERE postTag.tagId = ? "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), tagId);
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    /**
     * Get all posts for given tag supposed to be visible by users (with approved status, publishDate in past or null
     * and expireDate in future or null) sorted by creationTime
     * @return List of found Posts
     */
    @Override
    public List<Post> getPostsByTagForUser(int tagId) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "INNER JOIN postTag ON post.id = postTag.postId WHERE postTag.tagId = ? and status.name = ? "
                + "AND (post.publishDate IS NULL OR post.publishDate <= ?) AND (post.expireDate IS NULL OR post.expireDate >= ?) "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), tagId, Status.APPROVED.toString(),
                Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    /**
     * Get all posts with any publishDate and expireDate, which contain given content in title or in text
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<Post> getPostsByContentForAdmin(String content) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "WHERE (post.title LIKE ? OR post.text LIKE ?) "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), "%" + content + "%", "%" + content + "%");
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    /**
     * Get all posts supposed to be visible by users (with approved status, publishDate in past or null
     * and expireDate in future or null) which title or text contains given content
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<Post> getPostsByContentForUser(String content) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "WHERE (post.title LIKE ? OR post.text LIKE ?) AND status.name = ? "
                + "AND (post.publishDate IS NULL OR post.publishDate <= ?) AND (post.expireDate IS NULL OR post.expireDate >= ?) "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), "%" + content + "%", "%" + content + "%", Status.APPROVED.toString(),
                Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }


    /**
     * Get List of Posts which status is in_work. Supposed to be visible only by admin and manager.
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<Post> getNotApprovedPosts() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "WHERE status.name = ? ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), Status.IN_WORK.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    /**
     * Get List of Posts which status is rejected. Suppose to be visible only by manager
     * @return List of found Posts sorted by creationTime
     */
    @Override
    public List<RejectedPost> getRejectedPosts() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN rejectedPost ON rejectedPost.id = post.id "
                + "ORDER BY post.creationTime DESC;";
        List<RejectedPost> posts =  jdbcTemplate.query(GET_SQL, new RejectedPostMapper());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
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
        final String GET_STATUS_ID = "SELECT id FROM status WHERE name = ?;";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.APPROVED.toString());

        final String APPROVE_SQL = "UPDATE post SET status=?, creationTime=? WHERE id = ?;";

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
        final String GET_STATUS_ID = "SELECT id FROM status WHERE name = ?;";
        final String REJECT_SQL = "UPDATE post SET status=? WHERE id = ?;";
        final String ADD_REASON = "INSERT INTO rejectedPost (id, reason) VALUES (?,?);";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.REJECTED.toString());
        jdbcTemplate.update(REJECT_SQL, statusId, postId);
        jdbcTemplate.update(ADD_REASON, postId, reason);
    }

    /**
     * Modify status of rejected post to in_work. Supposed to be called by manager only
     * @param postId int with id of rejected post that was refined and should be checked by admin again
     */
    @Transactional
    @Override
    public void sendToApprove(int postId) {
        final String GET_STATUS_ID = "SELECT id FROM status WHERE name = ?;";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.IN_WORK.toString());

        //final String GET_PUBLISH_TIME = "SELECT publishDate FROM post WHERE id = ?;";
        //Timestamp timeNow = Timestamp.valueOf(LocalDateTime.now());
        //try {
        //    Timestamp publishTime = jdbcTemplate.queryForObject(GET_PUBLISH_TIME, Timestamp.class, postId);
        //    if (publishTime.compareTo(timeNow) >= 0) {
        //        timeNow = publishTime;
        //    }
        //} catch (DataAccessException ex) {

        //}
        //final String UPDATE_STATUS_SQL = "UPDATE post SET status=?, creationTime=? WHERE id = ?;";
        //jdbcTemplate.update(UPDATE_STATUS_SQL, statusId, timeNow, postId);

        final String UPDATE_STATUS_SQL = "UPDATE post SET status=? WHERE id = ?;";
        jdbcTemplate.update(UPDATE_STATUS_SQL, statusId, postId);

        final String DELETE_REJECTED = "DELETE FROM rejectedPost WHERE id = ?;";
        jdbcTemplate.update(DELETE_REJECTED, postId);
    }

    /**
     * Get all tags for post with given id
     * @param postId int - id of the post
     * @return List of related Tags
     */
    private List<Tag> getTags(int postId) {
        final String GET_SQL = "SELECT tag.* FROM tag INNER JOIN postTag "
                + "ON postTag.tagId = tag.id WHERE postTag.postId = ?;";
        return jdbcTemplate.query(GET_SQL, new TagDaoDbImpl.TagMapper(), postId);
    }

    private static final class PostMapper implements RowMapper<Post> {
        @Override
        public Post mapRow(ResultSet resultSet, int i) throws SQLException {
            Post post  = new Post();
            post.setTitle(resultSet.getString("post.title"));
            post.setId(resultSet.getInt("post.id"));
            post.setDescription(resultSet.getString("post.description"));
            post.setText(resultSet.getString("post.text"));
            String statusName = resultSet.getString("status.name");
            post.setStatus(Status.valueOf(statusName.toUpperCase()));
            //post.setApproved(resultSet.getBoolean("approved"));
            Timestamp publishDate = resultSet.getTimestamp("post.publishDate");
            if (publishDate != null) {
                post.setPublishDate(publishDate.toLocalDateTime().toLocalDate());
            }
            Timestamp expireDate = resultSet.getTimestamp("post.expireDate");
            if (expireDate != null) {
                post.setExpireDate(expireDate.toLocalDateTime().toLocalDate());
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
            post.setId(resultSet.getInt("post.id"));
            post.setDescription(resultSet.getString("post.description"));
            post.setText(resultSet.getString("post.text"));
            post.setStatus(Status.REJECTED);
            //post.setApproved(resultSet.getBoolean("post.approved"));
            Timestamp publishDate = resultSet.getTimestamp("post.publishDate");
            if (publishDate != null) {
                post.setPublishDate(publishDate.toLocalDateTime().toLocalDate());
            }
            Timestamp expireDate = resultSet.getTimestamp("post.expireDate");
            if (expireDate != null) {
                post.setExpireDate(expireDate.toLocalDateTime().toLocalDate());
            }
            Timestamp creationTime = resultSet.getTimestamp("post.creationTime");
            if (creationTime != null) {
                post.setCreationTime(creationTime.toLocalDateTime());
            }
            Timestamp editTime = resultSet.getTimestamp("post.editTime");
            if (editTime != null) {
                post.setEditTime(editTime.toLocalDateTime());
            }
            //post.setDisplayDate(resultSet.getTimestamp("post.displayDate").toLocalDateTime().toLocalDate());
            //post.setExpireDate(resultSet.getTimestamp("post.expireDate").toLocalDateTime().toLocalDate());
            //post.setCreationTime(resultSet.getTimestamp("post.creationTime").toLocalDateTime());
            post.setReason(resultSet.getString("rejectedPost.reason"));
            return post;
        }
    }
}
