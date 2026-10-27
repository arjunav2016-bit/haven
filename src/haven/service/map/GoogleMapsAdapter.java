package haven.service.map;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleMapsAdapter implements NavigationService {
    private static final Logger LOGGER = Logger.getLogger(GoogleMapsAdapter.class.getName());

    @Override
    public void navigateTo(String locationLink) {
        try {
            if (locationLink == null || locationLink.trim().isEmpty()) {
                LOGGER.warning("No location link available to navigate.");
                javax.swing.JOptionPane.showMessageDialog(null,
                        "No geographic location link is available for this property.",
                        "Navigation Notice",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String url = locationLink.trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                fallbackToClipboard(url, "Desktop browser navigation is not supported in this environment.");
            }
        } catch (IOException | URISyntaxException | RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to launch browser navigation: " + e.getMessage(), e);
            fallbackToClipboard(locationLink, "Could not launch system browser: " + e.getMessage());
        }
    }

    private void fallbackToClipboard(String url, String message) {
        try {
            if (url != null) {
                java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(url);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                javax.swing.JOptionPane.showMessageDialog(null,
                        message + "\n\nThe location URL has been copied to your clipboard:\n" + url,
                        "Maps Navigation Fallback",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (HeadlessException | IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Clipboard access unavailable: " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Clipboard fallback error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void navigateTo(double latitude, double longitude) {
        navigateTo("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude);
    }

    public void openLocation(String locationLink) {
        navigateTo(locationLink);
    }

    public void openLocation(double latitude, double longitude) {
        navigateTo(latitude, longitude);
    }
}