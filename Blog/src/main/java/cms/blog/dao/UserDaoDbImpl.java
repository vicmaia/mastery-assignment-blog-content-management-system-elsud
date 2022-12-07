package cms.blog.dao;

import cms.blog.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Profile("prod")
public class UserDaoDbImpl implements UserDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public User getUserByEmail(String email) {
        final String GET_SQL = "SELECT * FROM users WHERE email = ?;";
        try {
            return jdbcTemplate.queryForObject(GET_SQL, new UserMapper(), email);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public User getUserByName(String name) {
        final String GET_SQL = "SELECT * FROM users WHERE name = ?;";
        try {
            return jdbcTemplate.queryForObject(GET_SQL, new UserMapper(), name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private static final class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            user.setUserId(resultSet.getInt("userId"));
            user.setName(resultSet.getString("name"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            return user;
        }
    }
}
