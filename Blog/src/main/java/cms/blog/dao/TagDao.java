package cms.blog.dao;

import cms.blog.dto.Tag;

import java.util.List;

public interface TagDao {

    public Tag getTag(int id);

    public Tag addTag(Tag tag);
}
