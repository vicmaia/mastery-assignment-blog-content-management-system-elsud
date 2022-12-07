package cms.blog.service;

import cms.blog.dao.HashtagDao;
import cms.blog.dto.HashTag;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class HashTagDaoStub implements HashtagDao {
    @Override
    public HashTag addTag(HashTag tag) {
        tag.setHashTagId(1);
        return tag;
    }

    @Override
    public HashTag getTagByName(String name) {
        if (name != "existing") {
            return null;
        }
        HashTag tag = new HashTag();
        tag.setHashTagId(1);
        tag.setHashTagName(name);
        return tag;
    }

    @Override
    public void addTagForPost(HashTag tag, int postId) {
    }

    @Override
    public void deleteTagForPost(int tagId, int postId) {
    }
}
