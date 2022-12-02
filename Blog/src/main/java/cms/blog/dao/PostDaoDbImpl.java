package cms.blog.dao;

import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;
import cms.blog.dto.Status;
import cms.blog.dto.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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

    @Override
    public Post addPost(Post post) {
        final String GET_STATUS = "SELECT id FROM status WHERE name = ?;";
        int statusId = jdbcTemplate.queryForObject(GET_STATUS, Integer.class, post.getStatus().toString());

        final String ADD_POST = "INSERT INTO post (creationTime, title, description, text, displayDate, "
                + "expireDate, status) VALUES (?, ?, ?, ?, ?, ?, ?);";
        jdbcTemplate.update(
                ADD_POST, Timestamp.valueOf(post.getCreationTime()), post.getTitle(), post.getDescription(),
                post.getText(), Timestamp.valueOf(post.getDisplayDate().atStartOfDay()),
                Timestamp.valueOf(post.getExpireDate().atStartOfDay()), statusId);
        int postId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        //final String ADD_TAGS = "INSERT INTO postTag (postId, tagId) VALUES (?,?);";
        //post.getTags().stream()
        //                .forEach(tag -> jdbcTemplate.update(ADD_TAGS, postId, tag.getId()));
        post.setId(postId);
        return post;
    }

    @Override
    //@Transactional
    public boolean editPost(Post post) {
        // doesn't change status and tags!
        //final String DELETE_TAG_CONN = "DELETE FROM postTag WHERE postId = ?'";
        final String UPDATE_POST = "UPDATE post SET creationTime=?, title=?, description=?, text=?, "
                + "displayDate=?, expireDate=? WHERE id=?";
        //final String ADD_TAGS = "INSERT INTO postTag (postId, tagId) VALUES (?,?);";
        //jdbcTemplate.update(DELETE_TAG_CONN, post.getId());
        boolean updated = jdbcTemplate.update(
                UPDATE_POST, Timestamp.valueOf(post.getCreationTime()), post.getTitle(), post.getDescription(),
                post.getText(), Timestamp.valueOf(post.getDisplayDate().atStartOfDay()),
                Timestamp.valueOf(post.getExpireDate().atStartOfDay()), post.getId()) > 0;
        //int postId = post.getId();
        //post.getTags().stream()
        //                .forEach(tag -> jdbcTemplate.update(ADD_TAGS, postId, tag.getId()));
        return updated;
    }

    @Override
    @Transactional
    public boolean deletePost(int id ) {
        final String DELETE_TAG_CONN = "DELETE FROM postTag WHERE postId = ?'";
        final String DELETE_REJECTED_POST = "DELETE FROM rejectedPost WHERE id = ?;";
        final String DELETE_POST = "DELETE FROM post WHERE id = ?;";
        jdbcTemplate.update(DELETE_TAG_CONN, id);
        jdbcTemplate.update(DELETE_REJECTED_POST, id);
        return jdbcTemplate.update(DELETE_POST, id) > 0;
    }

    @Override
    public Post getPostById(int id) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status "
                + "ON post.status = status.id WHERE id = ?;";
        Post post =  jdbcTemplate.queryForObject(GET_SQL, new PostMapper(), id);
        if (post != null) {
            post.setTags(getTags(id));
        }
        return post;
    }

    @Override
    public RejectedPost getRejectedPostById(int id) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "INNER JOIN rejectedPost ON rejectedPost.id = post.id WHERE id = ?;";
        RejectedPost post =  jdbcTemplate.queryForObject(GET_SQL, new RejectedPostMapper(), id);
        if (post != null) {
            post.setTags(getTags(id));
        }
        return post;
    }

    // all approved posts for admin
    @Override
    public List<Post> getApprovedPostsForAdmin() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "WHERE status.name = ? ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), Status.APPROVED.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    @Override
    public List<Post> getApprovedPostsForUser() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON "
                + "post.status = status.id WHERE status.name = ? "
                + "AND (displayDate IS NULL OR displayDate <= ?) "
                + "AND (expireDate IS NULL OR expireDate >= ?) ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(
                GET_SQL, new PostMapper(), Status.APPROVED.toString(),
                Timestamp.valueOf(LocalDate.now().atStartOfDay()),
                Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    @Override
    public List<Post> getPostsByTagForAdmin(int tagId) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "INNER JOIN postTag ON post.id = postTag.postId WHERE postTag.tagId = ? and status.name = ? "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), tagId, Status.APPROVED.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    @Override
    public List<Post> getPostsByTagForUser(int tagId) {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "INNER JOIN postTag ON post.id = postTag.postId WHERE postTag.tagId = ? and status.name = ? "
                + "AND (displayDate IS NULL OR displayDate <= ?) AND (expireDate IS NULL OR expireDate >= ?) "
                + "ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), tagId, Status.APPROVED.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }


    // for admin - should be buttons to edit, delete, approve, reject for each of them
    // for manager - buttons to edit, delete
    @Override
    public List<Post> getNotApprovedPosts() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN status ON post.status = status.id "
                + "WHERE status.name = ? ORDER BY post.creationTime DESC;";
        List<Post> posts =  jdbcTemplate.query(GET_SQL, new PostMapper(), Status.IN_WORK.toString());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    // for manager - should be buttons to edit, delete, send to approve for each of them
    @Override
    public List<RejectedPost> getRejectedPosts() {
        final String GET_SQL = "SELECT * FROM post INNER JOIN rejectedPost ON rejectedPost.id = post.id "
                + "ORDER BY post.creationTime DESC;";
        List<RejectedPost> posts =  jdbcTemplate.query(GET_SQL, new RejectedPostMapper());
        posts.stream()
                .forEach(post ->post.setTags(getTags(post.getId())));
        return posts;
    }

    @Override
    public void approvePost(int postId, LocalDateTime creationTime) {
        final String GET_STATUS_ID = "SELECT id FROM status WHERE name = ?;";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.APPROVED.toString());

        final String APPROVE_SQL = "UPDATE post SET status=?, creationTime=? WHERE id = ?;";

        jdbcTemplate.update(APPROVE_SQL, statusId, Timestamp.valueOf(creationTime), postId);
    }

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

    @Transactional
    @Override
    public void sendToApprove(int postId) {
        final String GET_STATUS_ID = "SELECT id FROM status WHERE name = ?;";
        int statusId = jdbcTemplate.queryForObject(
                GET_STATUS_ID, Integer.class, Status.IN_WORK.toString());

        final String GET_DISPLAY_TIME = "SELECT displayTime FROM post WHERE id = ?;";
        Timestamp displayTime = jdbcTemplate.queryForObject(GET_DISPLAY_TIME, Timestamp.class, postId);
        Timestamp timeNow = Timestamp.valueOf(LocalDateTime.now());
        if (displayTime == null || displayTime.compareTo(timeNow) < 0) {
            displayTime = timeNow;
        }
        final String UPDATE_STATUS_SQL = "UPDATE post SET status=?, creationTime=? WHERE id = ?;";
        jdbcTemplate.update(UPDATE_STATUS_SQL, statusId, displayTime, postId);

        final String DELETE_REJECTED = "DELETE FROM rejectedPost WHERE id = ?;";
        jdbcTemplate.update(DELETE_REJECTED, postId);
    }

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
            post.setDisplayDate(resultSet.getTimestamp("post.displayDate").toLocalDateTime().toLocalDate());
            post.setExpireDate(resultSet.getTimestamp("post.expireDate").toLocalDateTime().toLocalDate());
            post.setCreationTime(resultSet.getTimestamp("post.creationTime").toLocalDateTime());
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
            post.setDisplayDate(resultSet.getTimestamp("post.displayDate").toLocalDateTime().toLocalDate());
            post.setExpireDate(resultSet.getTimestamp("post.expireDate").toLocalDateTime().toLocalDate());
            post.setCreationTime(resultSet.getTimestamp("post.creationTime").toLocalDateTime());
            post.setReason(resultSet.getString("rejectedPost.reason"));
            return post;
        }
    }
}
