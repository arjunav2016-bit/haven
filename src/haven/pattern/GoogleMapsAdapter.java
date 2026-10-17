package haven.pattern;

import java.awt.Desktop;
import java.net.URI;

public class GoogleMapsAdapter implements NavigationService {

    @Override
    public void navigateTo(String locationLink) {
        try {
            if (locationLink == null || locationLink.trim().isEmpty()) {
                System.err.println("No location link available to navigate.");
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
        } catch (Exception e) {
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
        } catch (Exception ex) {
            System.err.println("Clipboard fallback error: " + ex.getMessage());
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