package cms.blog.dao;

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
    public HashTag getTagByName(String name) {
        final String GET_SQL = "SELECT * FROM hashTag WHERE hashTagName = ?;";
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
    public void addTagForPost(HashTag tag, int postId) {
        try {
            final String ADD_TAG_CONN = "INSERT INTO postTag (postId, hashTagId) VALUES (?,?);";
            jdbcTemplate.update(ADD_TAG_CONN, postId, tag.getHashTagId());
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
        final String DELETE_TAG_CONN ="DELETE FROM postTag WHERE postId = ? AND hashTagId = ?;";
        jdbcTemplate.update(DELETE_TAG_CONN, postId, tagId);
    }

    /**
     * Create new Tag
     * @param tag tag that should be created, must have name
     * @return created tag with set id
     */
    @Transactional
    @Override
    public HashTag addTag(HashTag tag) {
        final String INSERT_SQL = "INSERT INTO hashTag (hashTagName) VALUES (?);";
        jdbcTemplate.update(INSERT_SQL, tag.getHashTagName());
        int tagId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        tag.setHashTagId(tagId);
        return tag;
    }

    public static final class TagMapper implements RowMapper<HashTag> {
        @Override
        public HashTag mapRow(ResultSet resultSet, int i) throws SQLException {
            HashTag tag = new HashTag();
            tag.setHashTagId(resultSet.getInt("hashTagId"));
            tag.setHashTagName(resultSet.getString("hashTagName"));
            return tag;
        }
    }
}
