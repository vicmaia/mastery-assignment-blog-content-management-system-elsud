package cms.blog.dao;

import cms.blog.dto.HashTag;

public interface HashtagDao {

    public HashTag addTag(HashTag tag);

    public HashTag getTagByName(String name);

    public void addTagForPost(HashTag tag, int postId);

    public void deleteTagForPost(int tagId, int postId);
}
