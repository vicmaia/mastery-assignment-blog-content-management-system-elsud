package cms.blog.service;

import cms.blog.dao.HashtagDao;
import cms.blog.dao.PostDao;
import cms.blog.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class ServiceLayerCopy extends ServiceLayer{

    @Autowired
    public ServiceLayerCopy(PostDao postDao, HashtagDao tagDao, UserDao userDao) {
        super(postDao, tagDao, userDao);
    }
}
