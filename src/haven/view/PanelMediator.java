package haven.view;

import haven.model.Accommodation;

/**
 * Coordinates navigation and interaction between decoupled UI panels
 * (Discover, Map, Enquiries, Chat, Gallery, Notifications, Owner, and Admin).
 */
public interface PanelMediator {

    void showDiscoverView();
    void showStudentEnquiriesView();
    void showMapView(Accommodation focusAccommodation);
    void openChatDialog(Accommodation accommodation, String counterpartyUsername, String enquiryId);
    void openImageGalleryDialog(Accommodation accommodation);
    void openNotificationInbox();
    void refreshAllViews();
}
