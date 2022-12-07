package cms.blog.dao;

import cms.blog.dto.User;
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
public class UserDaoDbImplTest {

     @Autowired
     JdbcTemplate jdbcTemplate;

    @Autowired
    UserDao userDao;

    @Before
    public void setUp() {
        final String DELETE_ROLES= "DELETE FROM roles;";
        final String DELETE_USERS= "DELETE FROM users;";
        final String INSERT_USERS = "INSERT INTO users (name, email, password) "
                + "VALUES ('name', 'email', 'password');";

        jdbcTemplate.update(DELETE_ROLES);
        jdbcTemplate.update(DELETE_USERS);
        jdbcTemplate.update(INSERT_USERS);
    }

    @Test
    public void getUserByEmail() {
        User user = userDao.getUserByEmail("email");
        assertNotNull(user);
        assertEquals("email", user.getEmail());
        assertEquals("name", user.getName());
        assertEquals("password", user.getPassword());
    }

    @Test
    public void getNotExistingUserByEmail() {
        User user = userDao.getUserByEmail("notExisting");
        assertNull(user);
    }

    @Test
    public void getUserByName() {
        User user = userDao.getUserByName("name");
        assertNotNull(user);
        assertEquals("email", user.getEmail());
        assertEquals("name", user.getName());
        assertEquals("password", user.getPassword());
    }

    @Test
    public void getNotExistingUserByName() {
        User user = userDao.getUserByName("notExisting");
        assertNull(user);
    }
}