package haven.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Domain model representing an in-app chat message between a Student and a Property Owner.
 * Supports both enquiry-linked messages (with {@code enquiryId}) and standalone pre-booking messages.
 */
public class ChatMessage {
    private final String id;
    private final String senderUsername;
    private final String receiverUsername;
    private final String accommodationId;
    private final String accommodationName;
    private final String enquiryId; // Empty string if standalone pre-booking chat
    private final String content;
    private final long timestamp;
    private boolean read;

    public ChatMessage(String id, String senderUsername, String receiverUsername,
                       String accommodationId, String accommodationName, String enquiryId,
                       String content, long timestamp, boolean read) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Chat message content cannot be empty.");
        }
        this.id = (id != null && !id.trim().isEmpty()) ? id.trim() : "MSG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.senderUsername = (senderUsername != null) ? senderUsername.trim() : "";
        this.receiverUsername = (receiverUsername != null) ? receiverUsername.trim() : "";
        this.accommodationId = (accommodationId != null) ? accommodationId.trim() : "";
        this.accommodationName = (accommodationName != null) ? accommodationName.trim() : "";
        this.enquiryId = (enquiryId != null) ? enquiryId.trim() : "";
        this.content = content.trim();
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.read = read;
    }

    public ChatMessage(String senderUsername, String receiverUsername,
                       String accommodationId, String accommodationName, String enquiryId, String content) {
        this(null, senderUsername, receiverUsername, accommodationId, accommodationName, enquiryId, content, System.currentTimeMillis(), false);
    }

    public String getId() { return id; }
    public String getSenderUsername() { return senderUsername; }
    public String getReceiverUsername() { return receiverUsername; }
    public String getAccommodationId() { return accommodationId; }
    public String getAccommodationName() { return accommodationName; }
    public String getEnquiryId() { return enquiryId; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isEnquiryLinked() {
        return enquiryId != null && !enquiryId.trim().isEmpty();
    }

    public String getFormattedTime() {
        return new SimpleDateFormat("dd MMM, HH:mm").format(new Date(timestamp));
    }
}
