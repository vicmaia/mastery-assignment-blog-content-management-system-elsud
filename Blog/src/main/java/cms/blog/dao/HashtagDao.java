package cms.blog.dao;

import cms.blog.dto.Hashtag;

public interface HashtagDao {

    public Hashtag addTag(Hashtag tag);

    public Hashtag getTagByName(String name);

    public void addTagForPost(Hashtag tag, int postId);

    public void deleteTagForPost(int tagId, int postId);
}
