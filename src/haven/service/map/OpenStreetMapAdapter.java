package haven.service.map;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapter for OpenStreetMap navigation and embedded map centering.
 */
public class OpenStreetMapAdapter implements NavigationService {
    private static final Logger LOGGER = Logger.getLogger(OpenStreetMapAdapter.class.getName());

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
        } catch (IOException | URISyntaxException | RuntimeException e) {
            LOGGER.log(Level.WARNING, "OpenStreetMap navigation error: " + e.getMessage(), e);
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
