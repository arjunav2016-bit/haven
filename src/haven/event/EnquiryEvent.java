package haven.event;

import haven.model.Enquiry;

/**
 * Event published when an enquiry is sent or its status/response is updated.
 */
public class EnquiryEvent extends HavenEvent {

    public enum Type {
        SUBMITTED,
        STATUS_UPDATED,
        STATUS_CHANGED
    }

    private final Type type;
    private final Enquiry enquiry;
    private final String enquiryId;
    private final String newStatus;

    public EnquiryEvent(Type type, Enquiry enquiry) {
        this.type = type;
        this.enquiry = enquiry;
        this.enquiryId = (enquiry != null) ? enquiry.getEnquiryId() : null;
        this.newStatus = (enquiry != null) ? enquiry.getStatus() : null;
    }

    /** Lightweight constructor for status-change events where full Enquiry isn't available. */
    public EnquiryEvent(Type type, String enquiryId, String newStatus) {
        this.type = type;
        this.enquiry = null;
        this.enquiryId = enquiryId;
        this.newStatus = newStatus;
    }

    public Type getType() {
        return type;
    }

    public Enquiry getEnquiry() {
        return enquiry;
    }

    public String getEnquiryId() {
        return enquiryId;
    }

    public String getNewStatus() {
        return newStatus;
    }
}

