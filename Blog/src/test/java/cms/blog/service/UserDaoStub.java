package cms.blog.service;

import cms.blog.dao.UserDao;
import cms.blog.dto.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class UserDaoStub implements UserDao {
    @Override
    public User getUserByEmail(String email) {
        if (email != "existing") {
            return null;
        }
        User user = new User();
        user.setEmail(email);
        user.setName("name");
        user.setPassword("password");
        return user;
    }

    @Override
    public User getUserByName(String name) {
        if (name != "existing") {
            return null;
        }
        User user = new User();
        user.setEmail("email");
        user.setName(name);
        user.setPassword("password");
        return user;
    }
}
