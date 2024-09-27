import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {
    private int id;
    private int authorId;
    private String status;
    private String content;
    private int rating;
    private Date date;
    private Date lastModified;

    public Review(int id, int authorId, String status, String content, int rating, Date date, Date lastModified) {
        this.id = id;
        this.authorId = authorId;
        this.status = status;
        this.content = content;
        this.rating = rating;
        this.date = date;
        this.lastModified = lastModified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}