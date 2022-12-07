package cms.blog.dto;

import java.util.Objects;

public class HashTag {

    private int hashTagId;
    private String hashTagName;

    public HashTag(){}
    public HashTag(String name) {
        this.hashTagName = name;
    }

    public HashTag(int id, String name) {
        this.hashTagId = id;
        this.hashTagName = name;
    }

    public int getHashTagId() {
        return hashTagId;
    }

    public void setHashTagId(int id) {
        this.hashTagId = id;
    }

    public String getHashTagName() {
        return hashTagName;
    }

    public void setHashTagName(String name) {
        this.hashTagName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashTag)) return false;
        HashTag hashtag = (HashTag) o;
        return hashTagId == hashtag.hashTagId && hashTagName.equals(hashtag.hashTagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashTagId, hashTagName);
    }
}
