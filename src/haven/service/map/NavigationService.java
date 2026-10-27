package haven.service.map;

/**
 * Functional interface for navigation and external map provider dispatch.
 */
@FunctionalInterface
public interface NavigationService {
    void navigateTo(String locationLink);

    default void navigateTo(double latitude, double longitude) {
        navigateTo(String.format(java.util.Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f", latitude, longitude));
    }
}
