package haven.pattern;

import java.awt.Desktop;
import java.net.URI;

public class GoogleMapsAdapter implements NavigationService {
    @Override
    public void navigateTo(double latitude, double longitude) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                String uriString = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                Desktop.getDesktop().browse(new URI(uriString));
            } else {
                System.err.println("Desktop browser navigation is not supported.");
            }
        } catch (Exception e) {
            System.err.println("Failed to launch Google Maps: " + e.getMessage());
        }
    }
}