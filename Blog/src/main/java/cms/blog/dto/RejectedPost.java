package cms.blog.dto;

public class RejectedPost  extends Post {

    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}