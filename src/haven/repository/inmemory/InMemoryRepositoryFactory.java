package haven.repository.inmemory;

import haven.repository.*;

/**
 * Concrete Factory implementing RepositoryFactory for In-Memory &amp; File-backed repositories.
 */
public class InMemoryRepositoryFactory implements RepositoryFactory {
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final EnquiryRepository enquiryRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;

    public InMemoryRepositoryFactory() {
        this.accommodationRepository = new InMemoryAccommodationRepository();
        this.userRepository = new InMemoryUserRepository();
        this.enquiryRepository = new InMemoryEnquiryRepository();
        this.reviewRepository = new InMemoryReviewRepository();
        this.imageRepository = new InMemoryImageRepository();
        this.messageRepository = new InMemoryMessageRepository();
        this.notificationRepository = new InMemoryNotificationRepository();
    }

    @Override
    public AccommodationRepository getAccommodationRepository() {
        return accommodationRepository;
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    public EnquiryRepository getEnquiryRepository() {
        return enquiryRepository;
    }

    @Override
    public ReviewRepository getReviewRepository() {
        return reviewRepository;
    }

    @Override
    public ImageRepository getImageRepository() {
        return imageRepository;
    }

    @Override
    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    @Override
    public NotificationRepository getNotificationRepository() {
        return notificationRepository;
    }
}
