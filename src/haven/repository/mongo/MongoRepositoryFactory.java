package haven.repository.mongo;

import haven.repository.*;

/**
 * Concrete Factory implementing RepositoryFactory for MongoDB persistence.
 */
public class MongoRepositoryFactory implements RepositoryFactory {
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final EnquiryRepository enquiryRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;

    public MongoRepositoryFactory() {
        this.accommodationRepository = new MongoAccommodationRepository();
        this.userRepository = new MongoUserRepository();
        this.enquiryRepository = new MongoEnquiryRepository();
        this.reviewRepository = new MongoReviewRepository();
        this.imageRepository = new MongoImageRepository();
        this.messageRepository = new MongoMessageRepository();
        this.notificationRepository = new MongoNotificationRepository();
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
