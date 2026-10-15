package haven.pattern.mediator;

import haven.model.Accommodation;

/**
 * Mediator Pattern interface coordinating navigation and interaction between decoupled
 * UI panels (Discover, Map, Enquiries, Chat, Gallery, Notifications, Owner, and Admin).
 */
public interface PanelMediator {

    /**
     * Switches the main view to the Student Discover view.
     */
    void showDiscoverView();

    /**
     * Switches the main view to the Student Enquiries / Booking History tab.
     */
    void showStudentEnquiriesView();

    /**
     * Switches to the interactive Map View and optionally centers on the given accommodation.
     *
     * @param focusAccommodation accommodation to highlight, or null for overview
     */
    void showMapView(Accommodation focusAccommodation);

    /**
     * Opens the in-app Chat Dialog for a specific accommodation and optional enquiry ID.
     *
     * @param accommodation target property
     * @param counterpartyUsername other participant username (owner or student)
     * @param enquiryId linked enquiry ID (or empty string for pre-booking chat)
     */
    void openChatDialog(Accommodation accommodation, String counterpartyUsername, String enquiryId);

    /**
     * Opens the Image Gallery Dialog for the given accommodation.
     *
     * @param accommodation target property
     */
    void openImageGalleryDialog(Accommodation accommodation);

    /**
     * Opens the Notification Inbox Dialog for the active user.
     */
    void openNotificationInbox();

    /**
     * Triggers a full refresh of all active UI views.
     */
    void refreshAllViews();
}
