package cms.blog.dao;

import cms.blog.dto.Hashtag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Profile("prod")
public class HashtagDaoDbImpl implements HashtagDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Get Tag object with given name
     * @param name String of name to search
     * @return Tag
     */
    @Override
    public Hashtag getTagByName(String name) {
        final String GET_SQL = "SELECT * FROM hashtag WHERE hashTagName = ?;";
        try {
            return jdbcTemplate.queryForObject(GET_SQL, new TagMapper(), name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /**
     * Save relationship between given tag and post with given id
     * @param tag Tag object related to Post with given id
     * @param postId int - id of the post to which tag should be added
     */
    @Override
    public void addTagForPost(Hashtag tag, int postId) {
        try {
            final String ADD_TAG_CONN = "INSERT INTO posttag (postId, hashTagId) VALUES (?,?);";
            jdbcTemplate.update(ADD_TAG_CONN, postId, tag.getId());
        } catch (DataAccessException ex) {
        }
    }

    /**
     * Delete relationship between tag with given id and post with given id
     * @param tagId id of the tag to be removed from the post
     * @param postId id of the post from which tag should be removed
     */
    @Override
    public void deleteTagForPost(int tagId, int postId) {
        final String DELETE_TAG_CONN ="DELETE FROM posttag WHERE postId = ? AND hashTagId = ?;";
        jdbcTemplate.update(DELETE_TAG_CONN, postId, tagId);
    }

    /**
     * Create new Tag
     * @param tag tag that should be created, must have name
     * @return created tag with set id
     */
    @Transactional
    @Override
    public Hashtag addTag(Hashtag tag) {
        final String INSERT_SQL = "INSERT INTO hashtag (hashTagName) VALUES (?);";
        jdbcTemplate.update(INSERT_SQL, tag.getName());
        int tagId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        tag.setId(tagId);
        return tag;
    }

    public static final class TagMapper implements RowMapper<Hashtag> {
        @Override
        public Hashtag mapRow(ResultSet resultSet, int i) throws SQLException {
            Hashtag tag = new Hashtag();
            tag.setId(resultSet.getInt("hashTagId"));
            tag.setName(resultSet.getString("hashTagName"));
            return tag;
        }
    }
}
