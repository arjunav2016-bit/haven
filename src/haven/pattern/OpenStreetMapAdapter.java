package haven.pattern;

import java.awt.Desktop;
import java.net.URI;
import java.util.function.BiConsumer;

/**
 * Adapter Pattern: Concrete Adapter for OpenStreetMap navigation and embedded JXMapViewer2 centering.
 */
public class OpenStreetMapAdapter implements NavigationService {

    private final BiConsumer<Double, Double> embeddedMapConsumer;

    public OpenStreetMapAdapter() {
        this(null);
    }

    public OpenStreetMapAdapter(BiConsumer<Double, Double> embeddedMapConsumer) {
        this.embeddedMapConsumer = embeddedMapConsumer;
    }

    @Override
    public void navigateTo(String locationLink) {
        try {
            if (locationLink == null || locationLink.trim().isEmpty()) return;
            String url = locationLink.trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://www.openstreetmap.org/search?query=" + java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8);
            }
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            System.err.println("[OpenStreetMapAdapter] Navigation error: " + e.getMessage());
        }
    }

    @Override
    public void navigateTo(double latitude, double longitude) {
        if (embeddedMapConsumer != null) {
            embeddedMapConsumer.accept(latitude, longitude);
            return;
        }
        String osmUrl = String.format(java.util.Locale.US,
                "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=16/%.6f/%.6f",
                latitude, longitude, latitude, longitude);
        navigateTo(osmUrl);
    }
}
