package haven.model;

import haven.model.state.EnquiryState;
import haven.model.state.EnquiryStateFactory;

import java.util.Date;

/**
 * Domain model representing a student accommodation enquiry or booking request.
 * Enforces valid lifecycle status transitions.
 */
public class Enquiry {
    private final String enquiryId;
    private final String accommodationId;
    private final String accommodationName;
    private final String ownerId;
    private final String studentUsername;
    private final String studentPhone;
    private final String message;
    private EnquiryState state;
    private final Date createdAt;
    private String ownerResponse;

    public Enquiry(String enquiryId, String accommodationId, String accommodationName, 
                   String studentUsername, String studentPhone, String message, Date createdAt) {
        this(enquiryId, accommodationId, accommodationName, "", studentUsername, studentPhone, message, "PENDING", createdAt, "");
    }

    public Enquiry(String enquiryId, String accommodationId, String accommodationName, String ownerId,
                   String studentUsername, String studentPhone, String message, Date createdAt) {
        this(enquiryId, accommodationId, accommodationName, ownerId, studentUsername, studentPhone, message, "PENDING", createdAt, "");
    }

    public Enquiry(String enquiryId, String accommodationId, String accommodationName, String ownerId,
                   String studentUsername, String studentPhone, String message, String status, Date createdAt) {
        this(enquiryId, accommodationId, accommodationName, ownerId, studentUsername, studentPhone, message, status, createdAt, "");
    }

    public Enquiry(String enquiryId, String accommodationId, String accommodationName, String ownerId,
                   String studentUsername, String studentPhone, String message, String status, Date createdAt, String ownerResponse) {
        this.enquiryId = enquiryId;
        this.accommodationId = accommodationId;
        this.accommodationName = accommodationName;
        this.ownerId = (ownerId != null) ? ownerId : "";
        this.studentUsername = studentUsername;
        this.studentPhone = studentPhone;
        this.message = message;
        this.state = EnquiryStateFactory.fromStatus(status);
        this.createdAt = (createdAt != null) ? new Date(createdAt.getTime()) : new Date();
        this.ownerResponse = (ownerResponse != null) ? ownerResponse : "";
    }

    /**
     * Transitions this enquiry to the target status if allowed.
     *
     * @param targetStatus target status name
     * @throws IllegalStateException if the transition is invalid from the current state
     */
    public void transitionTo(String targetStatus) {
        this.state = this.state.transitionTo(targetStatus);
    }

    /**
     * Checks whether this enquiry can transition to the target status.
     *
     * @param targetStatus target status name
     * @return true if allowed
     */
    public boolean canTransitionTo(String targetStatus) {
        return this.state.canTransitionTo(targetStatus);
    }

    public EnquiryState getState() { return state; }
    public String getEnquiryId() { return enquiryId; }
    public String getAccommodationId() { return accommodationId; }
    public String getAccommodationName() { return accommodationName; }
    public String getOwnerId() { return ownerId; }
    public String getStudentUsername() { return studentUsername; }
    public String getStudentPhone() { return studentPhone; }
    public String getMessage() { return message; }
    public String getStatus() { return state.getStatusName(); }
    public Date getCreatedAt() { return (createdAt != null) ? new Date(createdAt.getTime()) : null; }
    public String getOwnerResponse() { return ownerResponse; }
    public void setOwnerResponse(String ownerResponse) {
        this.ownerResponse = (ownerResponse != null) ? ownerResponse : "";
    }
}
