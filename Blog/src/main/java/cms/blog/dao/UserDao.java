package cms.blog.dao;

import cms.blog.dto.User;

public interface UserDao {
    public User getUserByEmail(String email);
    public User getUserByName(String name);
}
