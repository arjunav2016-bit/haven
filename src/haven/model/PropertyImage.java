package haven.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Domain model representing a property photograph stored on disk in the local {@code images/} directory.
 */
public class PropertyImage {
    private final String id;
    private final String accommodationId;
    private final String filePath;
    private final String caption;
    private final String uploadedBy;
    private final long uploadedAt;
    private boolean primary;

    public PropertyImage(String id, String accommodationId, String filePath, String caption, String uploadedBy, long uploadedAt, boolean primary) {
        this.id = (id != null && !id.trim().isEmpty()) ? id.trim() : "IMG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.accommodationId = accommodationId;
        this.filePath = filePath;
        this.caption = (caption != null) ? caption : "";
        this.uploadedBy = (uploadedBy != null) ? uploadedBy : "unknown";
        this.uploadedAt = uploadedAt > 0 ? uploadedAt : System.currentTimeMillis();
        this.primary = primary;
    }

    public PropertyImage(String accommodationId, String filePath, String caption, String uploadedBy) {
        this(null, accommodationId, filePath, caption, uploadedBy, System.currentTimeMillis(), false);
    }

    public String getId() { return id; }
    public String getAccommodationId() { return accommodationId; }
    public String getFilePath() { return filePath; }
    public String getCaption() { return caption; }
    public String getUploadedBy() { return uploadedBy; }
    public long getUploadedAt() { return uploadedAt; }
    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }

    public String getFormattedDate() {
        return new SimpleDateFormat("dd MMM yyyy").format(new Date(uploadedAt));
    }
}
