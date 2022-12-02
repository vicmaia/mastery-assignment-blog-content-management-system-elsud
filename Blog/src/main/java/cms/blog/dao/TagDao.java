package cms.blog.dao;

import cms.blog.dto.Tag;

import java.util.List;

public interface TagDao {

    public Tag addTag(Tag tag);

    public Tag getTagByName(String name);

    public void addTagForPost(Tag tag, int postId);

    public void deleteTagForPost(int tagId, int postId);
}
