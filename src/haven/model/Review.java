package haven.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Domain model representing a verified student review and star rating (1 to 5).
 */
public class Review {
    private String id;
    private String accommodationId;
    private String studentUsername;
    private String studentFullName;
    private int rating; // 1 to 5
    private String comment;
    private long timestamp;
    private boolean flagged;

    public Review(String id, String accommodationId, String studentUsername, String studentFullName, int rating, String comment, long timestamp) {
        this.id = (id != null && !id.trim().isEmpty()) ? id.trim() : "REV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.accommodationId = accommodationId;
        this.studentUsername = studentUsername;
        this.studentFullName = studentFullName;
        this.rating = Math.max(1, Math.min(5, rating));
        this.comment = comment;
        this.timestamp = timestamp;
        this.flagged = false;
    }

    public Review(String accommodationId, String studentUsername, String studentFullName, int rating, String comment) {
        this(null, accommodationId, studentUsername, studentFullName, rating, comment, System.currentTimeMillis());
    }

    public Review(String accommodationId, String studentUsername, String studentFullName, int rating, String comment, long timestamp) {
        this(null, accommodationId, studentUsername, studentFullName, rating, comment, timestamp);
    }

    public String getId() { return id; }
    public void setId(String id) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id.trim();
        }
    }

    public String getAccommodationId() { return accommodationId; }
    public String getStudentUsername() { return studentUsername; }
    public String getUsername() { return studentUsername; }
    public String getStudentFullName() { return studentFullName; }
    public String getAuthorName() { return studentFullName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }
    public Date getCreatedAt() { return new Date(timestamp); }
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(new Date(timestamp));
    }

    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rating; i++) sb.append("★");
        for (int i = rating; i < 5; i++) sb.append("☆");
        return sb.toString();
    }
}
