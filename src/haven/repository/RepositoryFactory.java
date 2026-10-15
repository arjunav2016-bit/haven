package haven.repository;

/**
 * Abstract Factory Pattern interface for creating families of related repository objects
 * without specifying their concrete classes (MongoDB vs In-Memory / File-backed).
 */
public interface RepositoryFactory {

    /**
     * Creates or provides the AccommodationRepository instance.
     */
    AccommodationRepository getAccommodationRepository();

    /**
     * Creates or provides the UserRepository instance.
     */
    UserRepository getUserRepository();

    /**
     * Creates or provides the EnquiryRepository instance.
     */
    EnquiryRepository getEnquiryRepository();

    /**
     * Creates or provides the ReviewRepository instance.
     */
    ReviewRepository getReviewRepository();

    /**
     * Creates or provides the ImageRepository instance.
     */
    ImageRepository getImageRepository();

    /**
     * Creates or provides the MessageRepository instance.
     */
    MessageRepository getMessageRepository();

    /**
     * Creates or provides the NotificationRepository instance.
     */
    NotificationRepository getNotificationRepository();
}
