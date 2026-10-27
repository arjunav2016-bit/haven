package haven.pattern;

import haven.model.Accommodation;
import haven.model.Hostel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility service managing on-disk storage of property photographs in the local {@code images/} directory.
 * Also generates default architectural preview images on disk for seeded properties when none exist.
 */
public final class PropertyImageStorage {
    private static final Logger LOGGER = Logger.getLogger(PropertyImageStorage.class.getName());
    public static final String IMAGES_ROOT_DIR = "images";
    private static final java.util.Map<String, javax.swing.ImageIcon> ICON_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private PropertyImageStorage() {}

    public static void clearIconCache() {
        ICON_CACHE.clear();
    }

    public static void invalidateIcon(String path) {
        if (path == null) return;
        ICON_CACHE.keySet().removeIf(k -> k.startsWith(path + "@"));
    }

    /**
     * Copies a user-selected image file into {@code images/<accommodationId>/<filename>}
     * and returns the relative file path to store in the database.
     *
     * @param accommodationId property identifier
     * @param sourceFile      source image file selected by the user
     * @return relative path string (e.g. {@code images/BH01/photo1.png})
     * @throws IOException if file copy fails
     */
    public static String copyImageToStorage(String accommodationId, File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IOException("Selected image file does not exist.");
        }
        String safeId = (accommodationId != null && !accommodationId.trim().isEmpty())
                ? accommodationId.trim().replaceAll("[^a-zA-Z0-9_-]", "_")
                : "general";
        Path targetDir = Paths.get(IMAGES_ROOT_DIR, safeId);
        Files.createDirectories(targetDir);

        String originalName = sourceFile.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String targetFileName = System.currentTimeMillis() + "_" + originalName;
        Path targetPath = targetDir.resolve(targetFileName);

        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        String relativePath = targetPath.toString().replace('\\', '/');
        invalidateIcon(relativePath);
        return relativePath;
    }

    /**
     * Ensures that an accommodation has at least two sample architectural images on disk
     * in {@code images/<id>/} so thumbnails and gallery views work immediately out-of-the-box.
     *
     * @param acc target accommodation
     */
    public static void ensureDefaultImagesForAccommodation(Accommodation acc) {
        if (acc == null || acc.getId() == null) return;
        String safeId = acc.getId().trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        File dir = new File(IMAGES_ROOT_DIR, safeId);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File exteriorFile = new File(dir, "exterior.png");
        File roomFile = new File(dir, "room_interior.png");

        if (!exteriorFile.exists()) {
            renderSampleImage(exteriorFile, acc, true);
        }
        if (!roomFile.exists()) {
            renderSampleImage(roomFile, acc, false);
        }

        String extPath = exteriorFile.getPath().replace('\\', '/');
        String rmPath = roomFile.getPath().replace('\\', '/');
        if (acc.getImagePaths().isEmpty()) {
            acc.addImagePath(extPath);
            acc.addImagePath(rmPath);
        }
    }

    private static void renderSampleImage(File outFile, Accommodation acc, boolean isExterior) {
        int w = 640;
        int h = 420;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean isHostel = acc instanceof Hostel;
        int hash = Math.abs(acc.getId().hashCode());

        if (isExterior) {
            // Sky gradient
            Color topSky = isHostel ? new Color(30, 58, 138) : new Color(15, 118, 110);
            Color botSky = isHostel ? new Color(99, 102, 241) : new Color(45, 212, 191);
            g2.setPaint(new GradientPaint(0, 0, topSky, 0, h, botSky));
            g2.fillRect(0, 0, w, h);

            // Ground
            g2.setColor(new Color(30, 41, 59));
            g2.fillRect(0, h - 80, w, 80);

            // Building facade
            int bw = 320 + (hash % 60);
            int bh = 220;
            int bx = (w - bw) / 2;
            int by = h - 80 - bh;

            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(bx, by, bw, bh, 12, 12);

            // Roof accent
            g2.setColor(isHostel ? new Color(79, 70, 229) : new Color(13, 148, 136));
            g2.fillRoundRect(bx - 10, by - 14, bw + 20, 22, 8, 8);

            // Windows grid
            int cols = 4;
            int rows = 3;
            int ww = 42;
            int wh = 34;
            int gapX = (bw - (cols * ww)) / (cols + 1);
            int gapY = 22;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int wx = bx + gapX + c * (ww + gapX);
                    int wy = by + 24 + r * (wh + gapY);
                    g2.setColor(((r + c + hash) % 3 == 0) ? new Color(254, 240, 138) : new Color(148, 163, 184));
                    g2.fillRoundRect(wx, wy, ww, wh, 6, 6);
                }
            }

            // Entrance door
            g2.setColor(new Color(51, 65, 85));
            g2.fillRoundRect(w / 2 - 24, h - 80 - 44, 48, 44, 6, 6);
        } else {
            // Warm interior room illustration
            g2.setPaint(new GradientPaint(0, 0, new Color(241, 245, 249), 0, h, new Color(226, 232, 240)));
            g2.fillRect(0, 0, w, h);

            // Floor
            g2.setColor(new Color(203, 213, 225));
            g2.fillRect(0, h - 100, w, 100);

            // Window with daylight
            g2.setColor(new Color(186, 230, 253));
            g2.fillRoundRect(w / 2 - 90, 50, 180, 120, 10, 10);
            g2.setColor(new Color(100, 116, 139));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(w / 2 - 90, 50, 180, 120, 10, 10);
            g2.drawLine(w / 2, 50, w / 2, 170);

            // Study Desk
            g2.setColor(new Color(120, 53, 15));
            g2.fillRoundRect(90, h - 170, 150, 14, 4, 4);
            g2.fillRect(105, h - 156, 12, 56);
            g2.fillRect(215, h - 156, 12, 56);

            // Bed frame & mattress
            g2.setColor(new Color(79, 70, 229));
            g2.fillRoundRect(320, h - 155, 230, 40, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(330, h - 170, 60, 20, 8, 8);
            g2.setColor(new Color(30, 41, 59));
            g2.fillRect(320, h - 115, 230, 18);
        }

        // Bottom caption overlay banner
        g2.setColor(new Color(15, 23, 42, 215));
        g2.fillRect(0, h - 64, w, 64);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString(acc.getName(), 20, h - 36);

        g2.setColor(new Color(203, 213, 225));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        String viewLabel = isExterior ? "Exterior & Campus Proximity View" : "Furnished Student Room & Study Area";
        g2.drawString(viewLabel + "  •  Near " + acc.getNearbyCollege() + "  •  Rs. " + String.format("%.0f/mo", acc.getBaseRent()), 20, h - 15);

        g2.dispose();
        try {
            ImageIO.write(img, "png", outFile);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write sample image to " + outFile.getAbsolutePath(), e);
        }
    }

    /**
     * Loads and scales an image from disk into a Swing {@link javax.swing.ImageIcon}.
     * Employs an in-memory ConcurrentHashMap cache to eliminate synchronous disk I/O on the Swing EDT.
     */
    public static javax.swing.ImageIcon loadScaledIcon(String path, int width, int height) {
        if (path == null || path.trim().isEmpty()) return null;
        String cacheKey = path.trim() + "@" + width + "x" + height;
        javax.swing.ImageIcon cached = ICON_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            File file = new File(path.trim());
            if (!file.exists() || !file.isFile()) return null;
            BufferedImage img = ImageIO.read(file);
            if (img == null) return null;
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(scaled);
            ICON_CACHE.put(cacheKey, icon);
            return icon;
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.FINE, "Failed to load and scale image from " + path, e);
            return null;
        }
    }

    /**
     * Creates a fallback placeholder image in memory when an image file is unavailable.
     */
    public static BufferedImage createPlaceholderImage(String title, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(30, 58, 138), width, height, new Color(79, 70, 229)));
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        String label = (title != null && title.length() > 22) ? title.substring(0, 19) + "..." : (title != null ? title : "HAVEN Stay");
        g2.drawString(label, 10, height / 2);
        g2.dispose();
        return img;
    }
}
