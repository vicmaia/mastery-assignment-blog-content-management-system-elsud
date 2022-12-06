package cms.blog.dto;

import java.util.Objects;

public class Hashtag {

    private int hashTagId;
    private String hashTagName;

    public Hashtag(){}
    public Hashtag(String name) {
        this.hashTagName = name;
    }

    public Hashtag(int id, String name) {
        this.hashTagId = id;
        this.hashTagName = name;
    }

    public int getId() {
        return hashTagId;
    }

    public void setId(int id) {
        this.hashTagId = id;
    }

    public String getName() {
        return hashTagName;
    }

    public void setName(String name) {
        this.hashTagName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hashtag)) return false;
        Hashtag hashtag = (Hashtag) o;
        return hashTagId == hashtag.hashTagId && hashTagName.equals(hashtag.hashTagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashTagId, hashTagName);
    }
}
