package haven.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Domain model representing a persistent user notification in the HAVEN Notification Inbox.
 */
public class Notification {
    public enum Type {
        ENQUIRY,
        CHAT,
        REVIEW,
        ADMIN,
        SYSTEM
    }

    private final String id;
    private final String recipientUsername;
    private final String title;
    private final String message;
    private final Type type;
    private final String linkedEntityId;
    private final long timestamp;
    private boolean read;

    public Notification(String id, String recipientUsername, String title, String message,
                        Type type, String linkedEntityId, long timestamp, boolean read) {
        this.id = (id != null && !id.trim().isEmpty()) ? id.trim() : "NTF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.recipientUsername = (recipientUsername != null) ? recipientUsername.trim() : "";
        this.title = (title != null) ? title.trim() : "Notification";
        this.message = (message != null) ? message.trim() : "";
        this.type = (type != null) ? type : Type.SYSTEM;
        this.linkedEntityId = (linkedEntityId != null) ? linkedEntityId.trim() : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.read = read;
    }

    public Notification(String recipientUsername, String title, String message, Type type, String linkedEntityId) {
        this(null, recipientUsername, title, message, type, linkedEntityId, System.currentTimeMillis(), false);
    }

    public String getId() { return id; }
    public String getRecipientUsername() { return recipientUsername; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Type getType() { return type; }
    public String getLinkedEntityId() { return linkedEntityId; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getFormattedTime() {
        return new SimpleDateFormat("dd MMM, HH:mm").format(new Date(timestamp));
    }
}
