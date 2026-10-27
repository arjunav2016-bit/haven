package haven.repository;

/**
 * Factory interface for creating families of related repository objects
 * across persistence engines (MongoDB vs in-memory fallback).
 */
public interface RepositoryFactory {

    AccommodationRepository getAccommodationRepository();
    UserRepository getUserRepository();
    EnquiryRepository getEnquiryRepository();
    ReviewRepository getReviewRepository();
    ImageRepository getImageRepository();
    MessageRepository getMessageRepository();
    NotificationRepository getNotificationRepository();
}
