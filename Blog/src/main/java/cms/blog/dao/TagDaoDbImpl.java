package cms.blog.dao;

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

@Repository
@Profile("prod")
public class TagDaoDbImpl implements TagDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public Tag getTagByName(String name) {
        final String GET_SQL = "SELECT * FROM tag WHERE name LIKE ?;";
        try {
            return jdbcTemplate.queryForObject(GET_SQL, new TagMapper(), name);
        } catch (DataAccessException ex) {
            return null;
        }

    }

    @Override
    public void addTagForPost(Tag tag, int postId) {

        final String ADD_TAG_CONN = "INSERT INTO postTag (postId, tagId) VALUES (?,?);";
        jdbcTemplate.update(ADD_TAG_CONN, postId, tag.getId());
    }

    @Override
    public void deleteTagForPost(int tagId, int postId) {
        final String DELETE_TAG_CONN ="DELETE FROM postTag WHERE postId = ? AND tagId = ?;";
        jdbcTemplate.update(DELETE_TAG_CONN, postId, tagId);
    }

    @Transactional
    @Override
    public Tag addTag(Tag tag) {
        final String INSERT_SQL = "INSERT INTO tag (name) VALUES (?);";
        jdbcTemplate.update(INSERT_SQL, tag.getName());
        int tagId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        tag.setId(tagId);
        return tag;
    }

    public static final class TagMapper implements RowMapper<Tag> {
        @Override
        public Tag mapRow(ResultSet resultSet, int i) throws SQLException {
            Tag tag = new Tag();
            tag.setId(resultSet.getInt("id"));
            tag.setName(resultSet.getString("name"));
            return tag;
        }
    }
}
