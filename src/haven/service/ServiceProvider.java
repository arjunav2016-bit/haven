package haven.service;

/**
 * Service Locator / Factory providing application-wide access to decoupled domain services.
 * Implements architectural layering (Views & Controllers -> Services -> Repositories -> Persistence).
 */
public final class ServiceProvider {
    private static volatile AccommodationService accommodationService;
    private static volatile EnquiryService enquiryService;
    private static volatile AuthService authService;

    private ServiceProvider() {}

    public static AccommodationService getAccommodationService() {
        if (accommodationService == null) {
            synchronized (ServiceProvider.class) {
                if (accommodationService == null) {
                    accommodationService = new AccommodationService();
                }
            }
        }
        return accommodationService;
    }

    public static EnquiryService getEnquiryService() {
        if (enquiryService == null) {
            synchronized (ServiceProvider.class) {
                if (enquiryService == null) {
                    enquiryService = new EnquiryService();
                }
            }
        }
        return enquiryService;
    }

    public static AuthService getAuthService() {
        if (authService == null) {
            synchronized (ServiceProvider.class) {
                if (authService == null) {
                    authService = new AuthService();
                }
            }
        }
        return authService;
    }

    /**
     * Resets service singletons (useful during test suite execution or factory reinitialization).
     */
    public static synchronized void reset() {
        accommodationService = null;
        enquiryService = null;
        authService = null;
    }
}
