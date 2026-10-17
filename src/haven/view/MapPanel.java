package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.ReviewDAO;
import haven.db.UserDAO;
import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.User;
import haven.pattern.GoogleMapsAdapter;
import haven.pattern.PropertyImageStorage;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.empty.EmptyTileFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MapPanel - Embedded Interactive Map View powered by JXMapViewer2 and OpenStreetMap tiles.
 * Features:
 * - Interactive pan & mouse-wheel zoom over Thiruvananthapuram engineering campuses
 * - Custom Java2D Waypoint & Campus Painter with color-coded accommodation pins:
 *   Emerald = Beds Available, Rose = Fully Booked, Gold Ring = Featured Stay, Indigo = Engineering College
 * - Clickable map pins that select an accommodation and update the right-side Property Inspector Card
 * - Quick Campus Jump selector (SCTCE, CET, GEC Barton Hill, MBCET, LBS)
 */
public class MapPanel extends JPanel {
    private final HostelDAO hostelDAO;
    private final UserDAO userDAO;
    private final EnquiryDAO enquiryDAO;
    private final ReviewDAO reviewDAO;
    private final User currentUser;
    private final Runnable onStateChanged;

    private final JXMapViewer mapViewer;
    private List<Accommodation> accommodations = new ArrayList<>();
    private Accommodation selectedAccommodation;

    // Inspector UI components
    private final JLabel inspectorThumb;
    private final JLabel inspectorTitle;
    private final JLabel inspectorBadge;
    private final JLabel inspectorCollege;
    private final JLabel inspectorRent;
    private final JLabel inspectorVacancies;
    private final JLabel inspectorCoords;
    private final JButton enquireBtn;
    private final JButton chatBtn;
    private final JButton galleryBtn;
    private final JButton externalMapBtn;

    private static final Map<String, GeoPosition> CAMPUS_LOCATIONS = new LinkedHashMap<>();
    static {
        CAMPUS_LOCATIONS.put("All Campuses (Trivandrum)", new GeoPosition(8.5050, 76.9550));
        CAMPUS_LOCATIONS.put("SCTCE (Pappanamcode)", new GeoPosition(8.4708, 76.9804));
        CAMPUS_LOCATIONS.put("CET (Sreekaryam)", new GeoPosition(8.5459, 76.9063));
        CAMPUS_LOCATIONS.put("GEC Barton Hill", new GeoPosition(8.5069, 76.9366));
        CAMPUS_LOCATIONS.put("Mar Baselios (MBCET)", new GeoPosition(8.5488, 76.9388));
        CAMPUS_LOCATIONS.put("LBS Institute (Poojappura)", new GeoPosition(8.4882, 76.9735));
    }

    public MapPanel(HostelDAO hostelDAO, UserDAO userDAO, EnquiryDAO enquiryDAO,
                    ReviewDAO reviewDAO, User currentUser, Runnable onStateChanged) {
        this.hostelDAO = hostelDAO;
        this.userDAO = userDAO;
        this.enquiryDAO = enquiryDAO;
        this.reviewDAO = reviewDAO;
        this.currentUser = currentUser;
        this.onStateChanged = onStateChanged;

        setLayout(new BorderLayout());
        setBackground(HavenTheme.BG_CANVAS);

        // Set HTTP User-Agent required by OpenStreetMap Tile Usage Policy
        System.setProperty("http.agent", "HAVEN-StudentAccommodationPlatform/2.0 (JavaSwing; Trivandrum)");

        // 1. Top Map Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(HavenTheme.SURFACE_WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(10, 18, 10, 18)
        ));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);

        JLabel jumpLbl = new JLabel("Center on Campus:");
        jumpLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        jumpLbl.setForeground(HavenTheme.TEXT_BODY);
        leftControls.add(jumpLbl);

        JComboBox<String> campusSelector = new JComboBox<>(CAMPUS_LOCATIONS.keySet().toArray(new String[0]));
        campusSelector.setFont(HavenTheme.FONT_BODY);
        campusSelector.setPreferredSize(new Dimension(210, 30));
        leftControls.add(campusSelector);

        JButton zoomInBtn = ModernButton.secondary("Zoom +");
        zoomInBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        JButton zoomOutBtn = ModernButton.secondary("Zoom -");
        zoomOutBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        JButton resetViewBtn = ModernButton.subtle("Reset View");
        resetViewBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);

        leftControls.add(zoomInBtn);
        leftControls.add(zoomOutBtn);
        leftControls.add(resetViewBtn);
        toolbar.add(leftControls, BorderLayout.WEST);

        // Legend Badges
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        legendPanel.setOpaque(false);
        legendPanel.add(HavenTheme.createBadge("● Available Stay", HavenTheme.SUCCESS, HavenTheme.SUCCESS_BG, HavenTheme.SUCCESS_BORDER));
        legendPanel.add(HavenTheme.createBadge("● Full Stay", HavenTheme.DANGER, HavenTheme.DANGER_BG, HavenTheme.DANGER_BORDER));
        legendPanel.add(HavenTheme.createBadge("★ Featured", HavenTheme.WARNING, HavenTheme.WARNING_BG, HavenTheme.WARNING_BORDER));
        legendPanel.add(HavenTheme.createBadge("■ Campus", HavenTheme.PRIMARY, HavenTheme.PRIMARY_LIGHT, HavenTheme.PRIMARY_BORDER));
        toolbar.add(legendPanel, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);

        // 2. Configure JXMapViewer2 with OpenStreetMap Tile Factory and Local Disk Cache
        mapViewer = new JXMapViewer();
        try {
            TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
            DefaultTileFactory tileFactory = new DefaultTileFactory(info);
            tileFactory.setUserAgent("HAVEN-StudentAccommodationPlatform/2.0 (JavaSwing; Trivandrum; Contact: admin@haven.local)");
            File cacheDir = new File("data/cache/tiles");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));
            tileFactory.setThreadPoolSize(4);
            mapViewer.setTileFactory(tileFactory);
        } catch (Exception ex) {
            System.err.println("[MapPanel] Notice: Initializing offline fallback map tile factory: " + ex.getMessage());
            try {
                mapViewer.setTileFactory(new EmptyTileFactory());
            } catch (Exception ignored) {}
        }

        // Default center: SCTCE Pappanamcode
        mapViewer.setZoom(5);
        mapViewer.setAddressLocation(new GeoPosition(8.4708, 76.9804));

        // Add pan & wheel zoom interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        // Custom Overlay Painter for Campus & Accommodation Pins
        mapViewer.setOverlayPainter(new AccommodationMapPainter());

        // Click listener to detect pin selection
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMapClick(e.getPoint());
            }
        });

        campusSelector.addActionListener(e -> {
            String key = (String) campusSelector.getSelectedItem();
            if (key != null && CAMPUS_LOCATIONS.containsKey(key)) {
                mapViewer.setAddressLocation(CAMPUS_LOCATIONS.get(key));
                mapViewer.setZoom(key.startsWith("All") ? 6 : 4);
                mapViewer.repaint();
            }
        });

        zoomInBtn.addActionListener(e -> {
            mapViewer.setZoom(Math.max(1, mapViewer.getZoom() - 1));
            mapViewer.repaint();
        });

        zoomOutBtn.addActionListener(e -> {
            mapViewer.setZoom(Math.min(15, mapViewer.getZoom() + 1));
            mapViewer.repaint();
        });

        resetViewBtn.addActionListener(e -> {
            campusSelector.setSelectedIndex(0);
            mapViewer.setAddressLocation(new GeoPosition(8.5050, 76.9550));
            mapViewer.setZoom(6);
            mapViewer.repaint();
        });

        add(mapViewer, BorderLayout.CENTER);

        // 3. Right-Side Selected Property Inspector Sidebar
        JPanel inspector = new JPanel();
        inspector.setLayout(new BoxLayout(inspector, BoxLayout.Y_AXIS));
        inspector.setBackground(HavenTheme.SURFACE_WHITE);
        inspector.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(18, 18, 18, 18)
        ));
        inspector.setPreferredSize(new Dimension(295, 500));

        JLabel inspectorHeading = new JLabel("SELECTED MAP PIN");
        inspectorHeading.setFont(HavenTheme.FONT_BADGE);
        inspectorHeading.setForeground(HavenTheme.TEXT_SECONDARY);
        inspectorHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspector.add(inspectorHeading);
        inspector.add(Box.createVerticalStrut(10));

        inspectorThumb = new JLabel();
        inspectorThumb.setPreferredSize(new Dimension(255, 145));
        inspectorThumb.setMaximumSize(new Dimension(255, 145));
        inspectorThumb.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        inspectorThumb.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspectorThumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inspectorThumb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedAccommodation != null) {
                    new ImageGalleryDialog(SwingUtilities.getWindowAncestor(MapPanel.this),
                            selectedAccommodation, currentUser, MapPanel.this::refreshMapData).setVisible(true);
                }
            }
        });
        inspector.add(inspectorThumb);
        inspector.add(Box.createVerticalStrut(12));

        inspectorBadge = HavenTheme.createBadge("SELECT A PIN", HavenTheme.PRIMARY, HavenTheme.PRIMARY_LIGHT, HavenTheme.PRIMARY_BORDER);
        inspectorBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspector.add(inspectorBadge);
        inspector.add(Box.createVerticalStrut(8));

        inspectorTitle = new JLabel("Click any pin on the map");
        inspectorTitle.setFont(HavenTheme.FONT_TITLE);
        inspectorTitle.setForeground(HavenTheme.TEXT_PRIMARY);
        inspectorTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspector.add(inspectorTitle);
        inspector.add(Box.createVerticalStrut(4));

        inspectorCollege = new JLabel("To inspect property details & book");
        inspectorCollege.setFont(HavenTheme.FONT_CAPTION);
        inspectorCollege.setForeground(HavenTheme.TEXT_SECONDARY);
        inspectorCollege.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspector.add(inspectorCollege);
        inspector.add(Box.createVerticalStrut(10));

        inspectorRent = new JLabel("Rent: -");
        inspectorRent.setFont(HavenTheme.FONT_SUBTITLE);
        inspectorRent.setForeground(HavenTheme.PRIMARY);
        inspectorRent.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspector.add(inspectorRent);
        inspector.add(Box.createVerticalStrut(4));

        inspectorVacancies = new JLabel("Vacancies: -");
        inspectorVacancies.setFont(HavenTheme.FONT_BODY_BOLD);
        inspectorVacancies.setForeground(HavenTheme.SUCCESS);
        inspectorVacancies.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspector.add(inspectorVacancies);
        inspector.add(Box.createVerticalStrut(4));

        inspectorCoords = new JLabel("GPS: -");
        inspectorCoords.setFont(HavenTheme.FONT_CAPTION);
        inspectorCoords.setForeground(HavenTheme.TEXT_MUTED);
        inspectorCoords.setAlignmentX(Component.LEFT_ALIGNMENT);
        inspector.add(inspectorCoords);
        inspector.add(Box.createVerticalStrut(16));

        enquireBtn = ModernButton.primary("Enquire & Book Stay");
        enquireBtn.setMaximumSize(new Dimension(255, 34));
        enquireBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        enquireBtn.addActionListener(e -> {
            if (selectedAccommodation != null) {
                ListingCardPanel tempCard = new ListingCardPanel(
                        selectedAccommodation, hostelDAO, userDAO, enquiryDAO, reviewDAO, currentUser,
                        () -> {
                            refreshMapData();
                            if (onStateChanged != null) onStateChanged.run();
                        }, false, null
                );
                tempCard.openEnquiryDialog();
            }
        });
        inspector.add(enquireBtn);
        inspector.add(Box.createVerticalStrut(8));

        chatBtn = ModernButton.secondary("Chat with Property Owner");
        chatBtn.setMaximumSize(new Dimension(255, 32));
        chatBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatBtn.addActionListener(e -> {
            if (selectedAccommodation != null) {
                new ChatDialog(SwingUtilities.getWindowAncestor(this), currentUser, selectedAccommodation).setVisible(true);
            }
        });
        inspector.add(chatBtn);
        inspector.add(Box.createVerticalStrut(8));

        galleryBtn = ModernButton.secondary("Open Photo Gallery");
        galleryBtn.setMaximumSize(new Dimension(255, 32));
        galleryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        galleryBtn.addActionListener(e -> {
            if (selectedAccommodation != null) {
                new ImageGalleryDialog(SwingUtilities.getWindowAncestor(this), selectedAccommodation, currentUser, this::refreshMapData).setVisible(true);
            }
        });
        inspector.add(galleryBtn);
        inspector.add(Box.createVerticalStrut(8));

        externalMapBtn = ModernButton.subtle("Open in Browser Maps");
        externalMapBtn.setMaximumSize(new Dimension(255, 30));
        externalMapBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        externalMapBtn.addActionListener(e -> {
            if (selectedAccommodation != null) {
                new GoogleMapsAdapter().openLocation(selectedAccommodation.getLocationLink());
            }
        });
        inspector.add(externalMapBtn);

        inspector.add(Box.createVerticalGlue());
        add(inspector, BorderLayout.EAST);

        refreshMapData();
    }

    public void refreshMapData() {
        this.accommodations = hostelDAO.getAllAccommodations();
        if (selectedAccommodation != null) {
            for (Accommodation acc : accommodations) {
                if (acc.getId().equals(selectedAccommodation.getId())) {
                    selectedAccommodation = acc;
                    break;
                }
            }
        } else if (!accommodations.isEmpty()) {
            selectedAccommodation = accommodations.get(0);
        }
        updateInspectorCard();
        mapViewer.repaint();
    }

    public void focusOnAccommodation(Accommodation acc) {
        if (acc == null) return;
        this.selectedAccommodation = acc;
        if (acc.getLatitude() != 0.0 && acc.getLongitude() != 0.0) {
            mapViewer.setAddressLocation(new GeoPosition(acc.getLatitude(), acc.getLongitude()));
            mapViewer.setZoom(3);
        }
        updateInspectorCard();
        mapViewer.repaint();
    }

    private void updateInspectorCard() {
        if (selectedAccommodation == null) {
            inspectorTitle.setText("No property selected");
            enquireBtn.setEnabled(false);
            chatBtn.setEnabled(false);
            galleryBtn.setEnabled(false);
            externalMapBtn.setEnabled(false);
            return;
        }

        PropertyImageStorage.ensureDefaultImagesForAccommodation(selectedAccommodation);
        String firstImg = selectedAccommodation.getImagePaths().isEmpty() ? null : selectedAccommodation.getImagePaths().get(0);
        ImageIcon icon = PropertyImageStorage.loadScaledIcon(firstImg, 255, 145);
        inspectorThumb.setIcon(icon);

        boolean isHostel = selectedAccommodation instanceof Hostel;
        String typeStr = (selectedAccommodation.isFeatured() ? "★ FEATURED " : "") + (isHostel ? "HOSTEL" : "PG / STAY");
        inspectorBadge.setText(typeStr + " • " + selectedAccommodation.getGenderAllowed().toUpperCase());

        inspectorTitle.setText(selectedAccommodation.getName());
        inspectorCollege.setText(String.format("%.1f km from %s • %s",
                selectedAccommodation.getDistanceKm(),
                selectedAccommodation.getNearbyCollege(),
                selectedAccommodation.getAddress()));
        inspectorRent.setText(String.format("₹%,.0f / month (Est. Total: ₹%,.0f)",
                selectedAccommodation.getBaseRent(),
                selectedAccommodation.calculateTotalMonthlyCost(1)));

        int vac = selectedAccommodation.getVacancies();
        if (vac > 0) {
            inspectorVacancies.setText("● " + vac + " Bed" + (vac == 1 ? "" : "s") + " Available");
            inspectorVacancies.setForeground(HavenTheme.SUCCESS);
            enquireBtn.setEnabled(true);
        } else {
            inspectorVacancies.setText("● Fully Booked (0 Vacancies)");
            inspectorVacancies.setForeground(HavenTheme.DANGER);
            enquireBtn.setEnabled(false);
        }

        inspectorCoords.setText(String.format("GPS: %.4f° N, %.4f° E",
                selectedAccommodation.getLatitude(), selectedAccommodation.getLongitude()));

        chatBtn.setEnabled(true);
        galleryBtn.setEnabled(true);
        externalMapBtn.setEnabled(true);
    }

    private void handleMapClick(Point clickPoint) {
        Rectangle viewportBounds = mapViewer.getViewportBounds();
        int zoom = mapViewer.getZoom();

        for (Accommodation acc : accommodations) {
            if (acc.getLatitude() == 0.0 && acc.getLongitude() == 0.0) continue;
            GeoPosition pos = new GeoPosition(acc.getLatitude(), acc.getLongitude());
            Point2D worldPt = mapViewer.getTileFactory().geoToPixel(pos, zoom);
            int screenX = (int) (worldPt.getX() - viewportBounds.getX());
            int screenY = (int) (worldPt.getY() - viewportBounds.getY());

            if (clickPoint.distance(screenX, screenY - 12) <= 22) {
                selectedAccommodation = acc;
                updateInspectorCard();
                mapViewer.repaint();
                return;
            }
        }
    }

    /**
     * Custom Java2D Painter rendering campus landmarks and interactive accommodation pins.
     */
    private class AccommodationMapPainter implements Painter<JXMapViewer> {
        @Override
        public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle viewport = map.getViewportBounds();
            int zoom = map.getZoom();

            // Background grid fallback if running in offline mode with EmptyTileFactory
            if (map.getTileFactory() instanceof EmptyTileFactory) {
                g2.setColor(new Color(248, 250, 252));
                g2.fillRect(0, 0, width, height);
                g2.setColor(new Color(226, 232, 240));
                for (int gx = 0; gx < width; gx += 40) {
                    g2.drawLine(gx, 0, gx, height);
                }
                for (int gy = 0; gy < height; gy += 40) {
                    g2.drawLine(0, gy, width, gy);
                }
            }

            // 1. Paint Engineering College Landmarks
            for (Map.Entry<String, GeoPosition> entry : CAMPUS_LOCATIONS.entrySet()) {
                if (entry.getKey().startsWith("All")) continue;
                Point2D pt = map.getTileFactory().geoToPixel(entry.getValue(), zoom);
                int x = (int) (pt.getX() - viewport.getX());
                int y = (int) (pt.getY() - viewport.getY());

                g2.setColor(new Color(79, 70, 229, 45));
                g2.fillOval(x - 18, y - 18, 36, 36);
                g2.setColor(HavenTheme.PRIMARY);
                g2.fillRoundRect(x - 7, y - 7, 14, 14, 4, 4);
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(x - 7, y - 7, 14, 14, 4, 4);

                String shortName = entry.getKey().split(" ")[0];
                g2.setFont(HavenTheme.FONT_BADGE);
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(shortName);
                g2.setColor(HavenTheme.SURFACE_DARK);
                g2.fillRoundRect(x - tw / 2 - 5, y + 10, tw + 10, 16, 6, 6);
                g2.setColor(Color.WHITE);
                g2.drawString(shortName, x - tw / 2, y + 22);
            }

            // 2. Paint Accommodation Pins
            for (Accommodation acc : accommodations) {
                if (acc.getLatitude() == 0.0 && acc.getLongitude() == 0.0) continue;
                GeoPosition pos = new GeoPosition(acc.getLatitude(), acc.getLongitude());
                Point2D pt = map.getTileFactory().geoToPixel(pos, zoom);
                int x = (int) (pt.getX() - viewport.getX());
                int y = (int) (pt.getY() - viewport.getY());

                boolean isSelected = selectedAccommodation != null && selectedAccommodation.getId().equals(acc.getId());
                Color pinColor = acc.getVacancies() > 0 ? HavenTheme.SUCCESS : HavenTheme.DANGER;

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillOval(x - 10, y - 4, 20, 8);

                // Pin stem
                int[] px = {x - 9, x + 9, x};
                int[] py = {y - 14, y - 14, y};
                g2.setColor(pinColor);
                g2.fillPolygon(px, py, 3);

                // Pin head
                int r = isSelected ? 15 : 12;
                g2.setColor(pinColor);
                g2.fillOval(x - r, y - 14 - r, r * 2, r * 2);

                // Featured gold halo or selected white ring
                if (isSelected) {
                    g2.setStroke(new BasicStroke(3f));
                    g2.setColor(HavenTheme.PRIMARY);
                    g2.drawOval(x - r - 2, y - 16 - r, (r + 2) * 2, (r + 2) * 2);
                } else if (acc.isFeatured()) {
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.setColor(new Color(245, 158, 11));
                    g2.drawOval(x - r - 1, y - 15 - r, (r + 1) * 2, (r + 1) * 2);
                } else {
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(Color.WHITE);
                    g2.drawOval(x - r, y - 14 - r, r * 2, r * 2);
                }

                // Price pill above selected or zoomed-in pins
                String label = String.format("₹%.1fk", acc.getBaseRent() / 1000.0);
                g2.setFont(HavenTheme.FONT_BADGE);
                FontMetrics fm = g2.getFontMetrics();
                int lw = fm.stringWidth(label);
                g2.setColor(Color.WHITE);
                g2.drawString(label, x - lw / 2, y - 10 - r);

                if (isSelected) {
                    String nameTag = acc.getName() + " (" + acc.getVacancies() + " beds)";
                    int nw = fm.stringWidth(nameTag);
                    g2.setColor(HavenTheme.PRIMARY);
                    g2.fillRoundRect(x - nw / 2 - 8, y - 48 - r, nw + 16, 20, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.drawString(nameTag, x - nw / 2, y - 34 - r);
                }
            }

            // 3. Status indicator pill in bottom-left
            g2.setFont(HavenTheme.FONT_BADGE);
            String modeStr = (map.getTileFactory() instanceof EmptyTileFactory)
                    ? "● Offline Map Mode (Landmarks & Pins Active)"
                    : "● Map Active (OSM / Local Cache)";
            int mw = g2.getFontMetrics().stringWidth(modeStr);
            g2.setColor(new Color(15, 23, 42, 175));
            g2.fillRoundRect(12, height - 30, mw + 16, 20, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(modeStr, 20, height - 16);

            g2.dispose();
        }
    }
}
