package cms.blog.dao;

import cms.blog.dto.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Profile("prod")
public class TagDaoDbImpl implements TagDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public Tag getTag(int id) {
        final String GET_SQL = "SELECT * FROM tag WHERE id = ?;";
        return jdbcTemplate.queryForObject(GET_SQL, new TagMapper(), id);
    }

    @Override
    @Transactional
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
