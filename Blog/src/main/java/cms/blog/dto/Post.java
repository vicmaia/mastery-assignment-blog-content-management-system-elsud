package cms.blog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Post {

    private int id;
    private LocalDateTime creationTime;
    private LocalDateTime editTime;
    private String title;
    private String description;
    private String text;
    private LocalDate publishDate;
    private LocalDate expireDate;

    private Status status;

    private List<Tag>  tags;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
