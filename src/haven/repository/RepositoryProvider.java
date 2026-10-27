package haven.repository;

import haven.pattern.MongoDBConnection;
import haven.repository.inmemory.InMemoryRepositoryFactory;
import haven.repository.mongo.MongoRepositoryFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service Locator and Factory Provider for managing repository access across HAVEN.
 * Automatically resolves between MongoRepositoryFactory and InMemoryRepositoryFactory
 * based on MongoDB availability.
 */
public class RepositoryProvider {
    private static final Logger LOGGER = Logger.getLogger(RepositoryProvider.class.getName());
    private static volatile RepositoryFactory factory;

    private RepositoryProvider() {}

    public static synchronized RepositoryFactory getFactory() {
        if (factory == null) {
            initialize();
        }
        return factory;
    }

    public static synchronized void setFactory(RepositoryFactory customFactory) {
        factory = customFactory;
    }

    public static synchronized void reset() {
        factory = null;
    }

    public static synchronized void initialize() {
        if (MongoDBConnection.getInstance().isOfflineMode() || !MongoDBConnection.getInstance().testConnection()) {
            MongoDBConnection.getInstance().setOfflineMode(true);
            factory = new InMemoryRepositoryFactory();
        } else {
            try {
                factory = new MongoRepositoryFactory();
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Failed to initialize MongoRepositoryFactory, falling back to in-memory", e);
                MongoDBConnection.getInstance().setOfflineMode(true);
                factory = new InMemoryRepositoryFactory();
            }
        }
    }

    public static AccommodationRepository getAccommodationRepository() {
        return getFactory().getAccommodationRepository();
    }

    public static UserRepository getUserRepository() {
        return getFactory().getUserRepository();
    }

    public static EnquiryRepository getEnquiryRepository() {
        return getFactory().getEnquiryRepository();
    }

    public static ReviewRepository getReviewRepository() {
        return getFactory().getReviewRepository();
    }

    public static ImageRepository getImageRepository() {
        return getFactory().getImageRepository();
    }

    public static MessageRepository getMessageRepository() {
        return getFactory().getMessageRepository();
    }

    public static NotificationRepository getNotificationRepository() {
        return getFactory().getNotificationRepository();
    }

    public static boolean isOffline() {
        return MongoDBConnection.getInstance().isOfflineMode();
    }
}
