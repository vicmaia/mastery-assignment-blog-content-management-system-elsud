package cms.blog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Post {

    private int postId;
    private LocalDateTime creationTime;
    private LocalDateTime editTime;
    private String title;
    private String description;
    private String postContent;
    private LocalDate publishDate;
    private LocalDate expireDate;
    private Status status;
    private List<HashTag>  hashtags;

    public int getPostId() {
        return postId;
    }

    public void setPostId(int id) {
        this.postId = id;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getEditTime() {
        return editTime;
    }

    public void setEditTime(LocalDateTime editTime) {
        this.editTime = editTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String content) {
        this.postContent = content;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

     public void setPublishDate(String publishDate) {
        this.publishDate = LocalDate.parse(publishDate);
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

     public void setExpireDate(String expireDate) {
        this.expireDate = LocalDate.parse(expireDate);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<HashTag> getHashtags() {
        return hashtags;
    }

    public void setTags(List<HashTag> hashtags) {
        this.hashtags = hashtags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return getTitle().equals(post.getTitle()) && getPostContent().equals(post.getPostContent()) && getStatus() == post.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getPostContent(), getStatus());
    }
}
