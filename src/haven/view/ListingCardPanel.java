package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.UserDAO;
import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.PG;
import haven.model.User;
import haven.service.map.GoogleMapsAdapter;
import haven.util.PropertyImageStorage;
import haven.model.decorator.FeaturedAccommodationDecorator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ListingCardPanel - Modern Three-Column Card Component for Accommodations.
 * Features:
 * - Left Property Photo Thumbnail (clickable to launch ImageGalleryDialog carousel)
 * - Center details column with featured badge, verified ratings, metrics, and amenities
 * - Right pricing callout and action column (Enquire & Book, Direct Chat, Map, Wishlist)
 */
public class ListingCardPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ListingCardPanel.class.getName());
    private final Accommodation accommodation;
    private final HostelDAO hostelDAO;
    private final UserDAO userDAO;
    private final EnquiryDAO enquiryDAO;
    private final haven.db.ReviewDAO reviewDAO;
    private final User currentUser;
    private final Runnable onStateChanged;
    private final Consumer<Accommodation> onViewMapCallback;

    private final ModernButton wishlistBtn;
    private final Border defaultBorder;
    private final Border hoverBorder;

    public ListingCardPanel(Accommodation accommodation, HostelDAO hostelDAO, UserDAO userDAO,
                            EnquiryDAO enquiryDAO, User currentUser, Runnable onStateChanged) {
        this(accommodation, hostelDAO, userDAO, enquiryDAO, new haven.db.ReviewDAO(), currentUser, onStateChanged, false, null, null);
    }

    public ListingCardPanel(Accommodation accommodation, HostelDAO hostelDAO, UserDAO userDAO,
                            EnquiryDAO enquiryDAO, User currentUser, Runnable onStateChanged,
                            boolean isCompared, java.util.function.Predicate<Accommodation> onCompareToggled) {
        this(accommodation, hostelDAO, userDAO, enquiryDAO, new haven.db.ReviewDAO(), currentUser, onStateChanged, isCompared, onCompareToggled, null);
    }

    public ListingCardPanel(Accommodation accommodation, HostelDAO hostelDAO, UserDAO userDAO,
                            EnquiryDAO enquiryDAO, haven.db.ReviewDAO reviewDAO, User currentUser, Runnable onStateChanged,
                            boolean isCompared, java.util.function.Predicate<Accommodation> onCompareToggled) {
        this(accommodation, hostelDAO, userDAO, enquiryDAO, reviewDAO, currentUser, onStateChanged, isCompared, onCompareToggled, null);
    }

    public ListingCardPanel(Accommodation accommodation, HostelDAO hostelDAO, UserDAO userDAO,
                            EnquiryDAO enquiryDAO, haven.db.ReviewDAO reviewDAO, User currentUser, Runnable onStateChanged,
                            boolean isCompared, java.util.function.Predicate<Accommodation> onCompareToggled,
                            Consumer<Accommodation> onViewMapCallback) {
        this.accommodation = accommodation;
        this.hostelDAO = hostelDAO;
        this.userDAO = userDAO;
        this.enquiryDAO = enquiryDAO;
        this.reviewDAO = (reviewDAO != null) ? reviewDAO : new haven.db.ReviewDAO();
        this.currentUser = currentUser;
        this.onStateChanged = onStateChanged;
        this.onViewMapCallback = onViewMapCallback;

        setLayout(new BorderLayout(14, 0));
        setBackground(HavenTheme.SURFACE_WHITE);

        Color borderColor = accommodation.isFeatured() ? new Color(245, 158, 11) : HavenTheme.BORDER_LIGHT;
        defaultBorder = BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, accommodation.isFeatured() ? 2 : 1, true),
                new EmptyBorder(12, 14, 12, 16)
        );
        hoverBorder = BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_FOCUS, 2, true),
                new EmptyBorder(12, 14, 12, 16)
        );
        setBorder(defaultBorder);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(hoverBorder);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(defaultBorder);
                repaint();
            }
        });

        try {
            PropertyImageStorage.ensureDefaultImagesForAccommodation(accommodation);
        } catch (RuntimeException ignored) {
            LOGGER.log(Level.FINE, "Failed to ensure default images for accommodation " + accommodation.getId(), ignored);
        }
        List<String> imgPaths = accommodation.getImagePaths();
        String primaryImg = imgPaths.isEmpty() ? null : imgPaths.get(0);

        ImageIcon loadedIcon = null;
        if (primaryImg != null && new java.io.File(primaryImg).exists()) {
            loadedIcon = PropertyImageStorage.loadScaledIcon(primaryImg, 142, 114);
        }
        final ImageIcon finalIcon = loadedIcon;

        JPanel thumbPanel = new JPanel(new BorderLayout());
        thumbPanel.setOpaque(false);
        thumbPanel.setPreferredSize(new Dimension(142, 114));
        thumbPanel.setMinimumSize(new Dimension(142, 114));
        thumbPanel.setMaximumSize(new Dimension(142, 114));
        thumbPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        thumbPanel.setToolTipText("Click to open full photo gallery (" + imgPaths.size() + " photos)");
        thumbPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPhotoGallery();
            }
        });

        JComponent imageThumbComponent = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();
                Shape origClip = g2.getClip();
                Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 8, 8);
                g2.clip(clip);

                if (finalIcon != null) {
                    g2.drawImage(finalIcon.getImage(), 0, 0, w, h, null);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, new Color(241, 245, 249), 0, h, new Color(226, 232, 240)));
                    g2.fillRect(0, 0, w, h);
                    g2.setColor(new Color(100, 116, 139));
                    g2.setFont(HavenTheme.FONT_CAPTION_BOLD);
                    FontMetrics fm = g2.getFontMetrics();
                    String t = "HAVEN STAY";
                    g2.drawString(t, (w - fm.stringWidth(t)) / 2, h / 2 - 2);
                    g2.setFont(HavenTheme.FONT_BADGE);
                    fm = g2.getFontMetrics();
                    String sub = "Click for photos";
                    g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, h / 2 + 13);
                }

                // Overlay Photo Count Badge in bottom-right corner
                if (!imgPaths.isEmpty()) {
                    String countText = "📷 " + imgPaths.size();
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    FontMetrics bfm = g2.getFontMetrics();
                    int bw = bfm.stringWidth(countText) + 10;
                    int bh = 18;
                    int bx = w - bw - 6;
                    int by = h - bh - 6;

                    g2.setColor(new Color(15, 23, 42, 195));
                    g2.fillRoundRect(bx, by, bw, bh, 6, 6);
                    g2.setColor(Color.WHITE);
                    g2.drawString(countText, bx + 5, by + bfm.getAscent() + 1);
                }

                g2.setClip(origClip);
                g2.setColor(HavenTheme.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                g2.dispose();
            }
        };
        thumbPanel.add(imageThumbComponent, BorderLayout.CENTER);
        add(thumbPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // 1. Meta / Category Row + Rating + Compare Checkbox
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean isHostel = (accommodation instanceof Hostel);
        String categoryText = (isHostel ? "HOSTEL" : "PG / STAY") + " • " + accommodation.getGenderAllowed().toUpperCase();
        JLabel catLabel = new JLabel(categoryText);
        catLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        catLabel.setForeground(isHostel ? HavenTheme.PRIMARY : new Color(13, 148, 136));
        metaRow.add(catLabel);

        Accommodation effectiveStay = accommodation;
        FeaturedAccommodationDecorator featuredDecorator = null;
        if (accommodation instanceof FeaturedAccommodationDecorator) {
            featuredDecorator = (FeaturedAccommodationDecorator) accommodation;
        } else if (accommodation.isFeatured()) {
            featuredDecorator = new FeaturedAccommodationDecorator(accommodation, "FEATURED", 10.0);
            effectiveStay = featuredDecorator;
        }

        if (featuredDecorator != null) {
            JLabel featTag = HavenTheme.createBadge(
                    featuredDecorator.getHighlightBadgeText(),
                    HavenTheme.WARNING,
                    HavenTheme.WARNING_BG,
                    HavenTheme.WARNING_BORDER
            );
            featTag.setFont(new Font("SansSerif", Font.BOLD, 10));
            metaRow.add(featTag);
        }

        // Verified Review Rating Badge (Clean inline style)
        double avgRating = this.reviewDAO.getAverageRating(accommodation.getId());
        int reviewCount = this.reviewDAO.getReviewCount(accommodation.getId());
        JButton ratingBtn = new JButton();
        ratingBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        ratingBtn.setFocusPainted(false);
        ratingBtn.setContentAreaFilled(false);
        ratingBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(253, 230, 138), 1, true),
                new EmptyBorder(1, 6, 1, 6)
        ));
        ratingBtn.setBackground(new Color(254, 252, 232));
        ratingBtn.setOpaque(true);
        ratingBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (reviewCount > 0) {
            ratingBtn.setText(String.format("★ %.1f (%d)", avgRating, reviewCount));
            ratingBtn.setForeground(new Color(180, 83, 9));
        } else {
            ratingBtn.setText("★ New");
            ratingBtn.setForeground(new Color(180, 83, 9));
        }
        ratingBtn.setToolTipText("View verified student reviews or write your experience");
        ratingBtn.addActionListener(e -> {
            ReviewsDialog dialog = new ReviewsDialog(SwingUtilities.getWindowAncestor(this), accommodation, this.reviewDAO, currentUser, onStateChanged);
            dialog.setVisible(true);
        });
        metaRow.add(ratingBtn);

        if (onCompareToggled != null) {
            metaRow.add(Box.createHorizontalStrut(6));
            JCheckBox compareCheck = new JCheckBox("Compare") {
                @Override
                protected void paintComponent(Graphics g) {
                    // Ensure pristine white card surface beneath text to avoid subpixel ghosting/cutting
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(HavenTheme.SURFACE_WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            compareCheck.setFont(HavenTheme.FONT_CAPTION);
            compareCheck.setForeground(HavenTheme.TEXT_SECONDARY);
            compareCheck.setOpaque(false);
            compareCheck.setFocusPainted(false);
            compareCheck.setCursor(new Cursor(Cursor.HAND_CURSOR));
            compareCheck.setSelected(isCompared);
            compareCheck.setToolTipText("Add or remove from side-by-side comparison dock");
            compareCheck.addActionListener(e -> {
                boolean accepted = onCompareToggled.test(accommodation);
                if (!accepted) {
                    compareCheck.setSelected(!compareCheck.isSelected());
                }
                ListingCardPanel.this.repaint();
            });
            metaRow.add(compareCheck);
        }
        infoPanel.add(metaRow);

        // 2. Title Row (Prominent, High-Contrast)
        JLabel titleLabel = new JLabel(accommodation.getName());
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(titleLabel);

        // 3. Location & Area (Clean, Scannable with full details in Tooltip)
        String shortLoc = extractLocality(accommodation.getAddress());
        String locDisplay = String.format("📍 %.1f km from %s", accommodation.getDistanceFromCollege(), accommodation.getNearbyCollege());
        if (!shortLoc.isEmpty()) {
            locDisplay += " • " + shortLoc;
        }
        JLabel locLabel = new JLabel(locDisplay);
        locLabel.setFont(HavenTheme.FONT_BODY);
        locLabel.setForeground(HavenTheme.TEXT_SECONDARY);
        locLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String fullTooltip = String.format("<html><body style='padding: 4px; font-family: sans-serif;'>" +
                "<b style='font-size: 12px; color: #0F172A;'>%s</b><br/>" +
                "<span style='color: #475569;'>Distance: %.1f km from %s</span><br/>" +
                "<span style='color: #475569;'>Address: %s</span><br/>" +
                "<span style='color: #475569;'>Contact Phone: %s</span>" +
                "</body></html>",
                accommodation.getName(), accommodation.getDistanceFromCollege(), accommodation.getNearbyCollege(),
                accommodation.getAddress() != null ? accommodation.getAddress() : "N/A",
                accommodation.getContactPhone() != null ? accommodation.getContactPhone() : "N/A");
        locLabel.setToolTipText(fullTooltip);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(locLabel);

        // 4. Availability & Specs Row (Replaces chaotic badge cluster with clean typography)
        JPanel termsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        termsRow.setOpaque(false);
        termsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        int vacancies = accommodation.getVacancies();
        JLabel vacancyBadge;
        if (vacancies > 0) {
            vacancyBadge = HavenTheme.createBadge(
                    "● " + vacancies + " Bed" + (vacancies == 1 ? "" : "s") + " Available",
                    HavenTheme.SUCCESS, HavenTheme.SUCCESS_BG, HavenTheme.SUCCESS_BORDER
            );
        } else {
            vacancyBadge = HavenTheme.createBadge(
                    "● Fully Booked",
                    HavenTheme.DANGER, HavenTheme.DANGER_BG, HavenTheme.DANGER_BORDER
            );
        }
        vacancyBadge.setFont(HavenTheme.FONT_CAPTION_BOLD);
        termsRow.add(vacancyBadge);

        String specsText;
        if (isHostel) {
            Hostel h = (Hostel) accommodation;
            specsText = String.format("Rent: ₹%,.0f/mo  •  Mess: ₹%,.0f  •  Curfew: %s",
                    accommodation.getBaseRent(), h.getMessFee(), h.getCurfewTime());
        } else {
            PG pg = (PG) accommodation;
            specsText = String.format("Rent: ₹%,.0f/mo  •  %s  •  Utilities: ₹%,.0f",
                    accommodation.getBaseRent(), pg.isFoodIncluded() ? "Food Included" : "Self-Cook", pg.getUtilityCharges());
        }
        JLabel specsLabel = new JLabel(specsText);
        specsLabel.setFont(HavenTheme.FONT_BODY);
        specsLabel.setForeground(HavenTheme.TEXT_BODY);
        termsRow.add(specsLabel);

        infoPanel.add(Box.createVerticalStrut(7));
        infoPanel.add(termsRow);

        // 5. Amenities Tags Row (Subtle, modern chips)
        List<String> amenities = accommodation.getAmenities();
        if (amenities != null && !amenities.isEmpty()) {
            JPanel amenitiesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            amenitiesRow.setOpaque(false);
            amenitiesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            int maxShow = Math.min(amenities.size(), 4);
            for (int i = 0; i < maxShow; i++) {
                String am = amenities.get(i);
                JLabel tag = new JLabel(am);
                tag.setFont(HavenTheme.FONT_CAPTION);
                tag.setForeground(HavenTheme.TEXT_BODY);
                tag.setBackground(new Color(241, 245, 249));
                tag.setOpaque(true);
                tag.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                        new EmptyBorder(2, 7, 2, 7)
                ));
                amenitiesRow.add(tag);
            }
            if (amenities.size() > 4) {
                int moreCount = amenities.size() - 4;
                JLabel moreTag = new JLabel("+" + moreCount + " more");
                moreTag.setFont(HavenTheme.FONT_CAPTION_BOLD);
                moreTag.setForeground(HavenTheme.PRIMARY);
                moreTag.setCursor(new Cursor(Cursor.HAND_CURSOR));
                moreTag.setToolTipText("All Amenities: " + String.join(", ", amenities));
                amenitiesRow.add(moreTag);
            }
            infoPanel.add(Box.createVerticalStrut(6));
            infoPanel.add(amenitiesRow);
        }

        infoPanel.add(Box.createVerticalGlue());
        add(infoPanel, BorderLayout.CENTER);

        JPanel actionCol = new JPanel();
        actionCol.setLayout(new BoxLayout(actionCol, BoxLayout.Y_AXIS));
        actionCol.setOpaque(false);
        actionCol.setPreferredSize(new Dimension(205, 126));
        actionCol.setMinimumSize(new Dimension(205, 126));

        double totalEst = effectiveStay.calculateTotalMonthlyCost(1);

        JLabel priceVal = new JLabel(String.format("₹%,.0f", totalEst));
        priceVal.setFont(new Font("SansSerif", Font.BOLD, 22));
        priceVal.setForeground(HavenTheme.PRIMARY);
        priceVal.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel priceSub;
        if (featuredDecorator != null && featuredDecorator.getDiscountPercentage() > 0) {
            double origTotal = accommodation.calculateTotalMonthlyCost(1);
            priceSub = new JLabel(String.format("<html><span style='text-decoration: line-through; color: #94a3b8;'>₹%,.0f</span> / mo (%.0f%% off)</html>", origTotal, featuredDecorator.getDiscountPercentage()));
        } else {
            priceSub = new JLabel("/ month (total est.)");
        }
        priceSub.setFont(HavenTheme.FONT_CAPTION);
        priceSub.setForeground(HavenTheme.TEXT_SECONDARY);
        priceSub.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel priceBlock = new JPanel();
        priceBlock.setLayout(new BoxLayout(priceBlock, BoxLayout.Y_AXIS));
        priceBlock.setOpaque(false);
        priceBlock.setAlignmentX(Component.RIGHT_ALIGNMENT);
        priceBlock.add(priceVal);
        priceBlock.add(priceSub);
        actionCol.add(priceBlock);
        actionCol.add(Box.createVerticalStrut(8));

        // Primary Action: Enquire & Book
        JButton enquireBtn = ModernButton.primary("Enquire & Book");
        enquireBtn.setPreferredSize(new Dimension(198, 34));
        enquireBtn.setMaximumSize(new Dimension(198, 34));
        enquireBtn.setFont(HavenTheme.FONT_BODY_BOLD);
        enquireBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        enquireBtn.setEnabled(accommodation.getVacancies() > 0);
        enquireBtn.addActionListener(e -> openEnquiryDialog());
        actionCol.add(enquireBtn);
        actionCol.add(Box.createVerticalStrut(8));

        // Utility Row: Chat, Map & Wishlist
        JPanel utilRow = new JPanel(new GridLayout(1, 3, 5, 0));
        utilRow.setOpaque(false);
        utilRow.setPreferredSize(new Dimension(198, 28));
        utilRow.setMaximumSize(new Dimension(198, 28));
        utilRow.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton chatBtn = ModernButton.subtle("Chat");
        chatBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        chatBtn.setBorder(new EmptyBorder(2, 2, 2, 2));
        chatBtn.setToolTipText("Open direct chat with the property owner");
        chatBtn.addActionListener(e -> {
            new ChatDialog(SwingUtilities.getWindowAncestor(this), currentUser, accommodation).setVisible(true);
        });

        JButton mapBtn = ModernButton.subtle("Map");
        mapBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        mapBtn.setBorder(new EmptyBorder(2, 2, 2, 2));
        mapBtn.setToolTipText("View pin on HAVEN interactive map or open Google Maps");
        mapBtn.addActionListener(e -> {
            if (onViewMapCallback != null) {
                onViewMapCallback.accept(accommodation);
            } else {
                GoogleMapsAdapter adapter = new GoogleMapsAdapter();
                adapter.openLocation(accommodation.getLocationLink());
            }
        });

        wishlistBtn = ModernButton.subtle("Save");
        wishlistBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        wishlistBtn.setBorder(new EmptyBorder(2, 2, 2, 2));
        updateWishlistButtonLabel();
        wishlistBtn.addActionListener(e -> {
            userDAO.toggleWishlist(currentUser.getUsername(), accommodation.getId());
            updateWishlistButtonLabel();
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof JFrame) {
                boolean saved = userDAO.getWishlist(currentUser.getUsername()).contains(accommodation.getId());
                ToastNotification.show((JFrame) win,
                        saved ? "Saved to wishlist: " + accommodation.getName() : "Removed from wishlist",
                        saved ? ToastNotification.Type.SUCCESS : ToastNotification.Type.INFO);
            }
            if (onStateChanged != null) onStateChanged.run();
        });

        utilRow.add(chatBtn);
        utilRow.add(mapBtn);
        utilRow.add(wishlistBtn);
        actionCol.add(utilRow);

        add(actionCol, BorderLayout.EAST);
    }

    private String extractLocality(String fullAddress) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) return "";
        String[] parts = fullAddress.split(",");
        for (int i = parts.length - 1; i >= 0; i--) {
            String p = parts[i].trim();
            if (!p.isEmpty() && !p.toLowerCase().startsWith("ph:") && !p.toLowerCase().startsWith("ph ")
                    && !p.toLowerCase().startsWith("tc") && !p.toLowerCase().startsWith("pin")) {
                return p;
            }
        }
        return parts[parts.length - 1].trim();
    }

    private void openPhotoGallery() {
        new ImageGalleryDialog(SwingUtilities.getWindowAncestor(this), accommodation, currentUser, onStateChanged).setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 156));
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, d.height);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(520, 150);
    }

    private void updateWishlistButtonLabel() {
        List<String> wishlist = userDAO.getWishlist(currentUser.getUsername());
        if (wishlist.contains(accommodation.getId())) {
            wishlistBtn.setText("Saved");
            wishlistBtn.setForeground(HavenTheme.WARNING);
        } else {
            wishlistBtn.setText("Save");
            wishlistBtn.setForeground(HavenTheme.TEXT_BODY);
        }
    }


    public void openEnquiryDialog() {
        String defaultPhone = (currentUser != null && currentUser.getPhone() != null && !currentUser.getPhone().trim().isEmpty())
                ? currentUser.getPhone()
                : "9876543210";
        JTextField phoneField = new JTextField(defaultPhone, 15);
        phoneField.setFont(HavenTheme.FONT_BODY);
        phoneField.setBorder(HavenTheme.createInputPadding());

        JLabel phoneValidationLbl = new JLabel(" ");
        phoneValidationLbl.setFont(HavenTheme.FONT_CAPTION);
        ValidationUtils.attachValidator(phoneField, ValidationUtils::isValidPhone, "Enter a valid phone number (7-20 digits).", phoneValidationLbl);

        String[] sharingOptions = {"1-Person (Single)", "2-Sharing Room", "3-Sharing Room"};
        JComboBox<String> sharingBox = new JComboBox<>(sharingOptions);

        JTextArea messageArea = new JTextArea(3, 20);
        messageArea.setFont(HavenTheme.FONT_BODY);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        Runnable refreshMessage = () -> {
            int sharing = sharingBox.getSelectedIndex() + 1;
            double estimatedCost = accommodation.calculateTotalMonthlyCost(sharing);
            messageArea.setText("Hi, I am interested in visiting this accommodation for a " +
                    sharingBox.getSelectedItem() + " (Estimated: ₹" + String.format("%.0f", estimatedCost) + "/mo).");
        };
        sharingBox.addActionListener(e -> refreshMessage.run());
        refreshMessage.run();

        JPanel panel = new JPanel(new BorderLayout(8, 10));
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 1, 4, 2));
        if (accommodation.getContactPhone() != null && !accommodation.getContactPhone().isEmpty()) {
            JLabel wardenContact = new JLabel("Hostel Contact: " + accommodation.getContactPhone());
            wardenContact.setForeground(HavenTheme.TEXT_PRIMARY);
            wardenContact.setFont(HavenTheme.FONT_CAPTION_BOLD);
            fieldsPanel.add(wardenContact);
        }
        fieldsPanel.add(new JLabel("Room Sharing Preference:"));
        fieldsPanel.add(sharingBox);
        fieldsPanel.add(new JLabel("Your Contact Phone Number:"));
        fieldsPanel.add(phoneField);
        fieldsPanel.add(phoneValidationLbl);
        panel.add(fieldsPanel, BorderLayout.NORTH);

        JPanel msgPanel = new JPanel(new BorderLayout(4, 4));
        msgPanel.add(new JLabel("Message to Owner:"), BorderLayout.NORTH);
        msgPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        panel.add(msgPanel, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(this, panel, "Enquire for " + accommodation.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String phone = phoneField.getText().trim();
            String message = messageArea.getText().trim();

            if (phone.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Phone and message cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean decremented = hostelDAO.decrementVacancy(accommodation.getId());
                if (!decremented) {
                    throw new haven.exception.VacancyExhaustedException("Sorry, vacancies for " + accommodation.getName() + " are currently exhausted.");
                }
                Accommodation updated = hostelDAO.getAccommodationById(accommodation.getId());
                if (updated != null) {
                    accommodation.setVacancies(updated.getVacancies());
                }

                try {
                    String username = (currentUser != null && currentUser.isStudent()) ? currentUser.getUsername() : "student";
                    enquiryDAO.saveEnquiry(accommodation.getId(), accommodation.getName(), accommodation.getOwnerId(), username, phone, message);
                } catch (RuntimeException ex) {
                    LOGGER.log(Level.WARNING, "Failed to save enquiry, rolling back vacancy for " + accommodation.getId(), ex);
                    hostelDAO.incrementVacancy(accommodation.getId());
                    Accommodation rolledBack = hostelDAO.getAccommodationById(accommodation.getId());
                    if (rolledBack != null) {
                        accommodation.setVacancies(rolledBack.getVacancies());
                    }
                    throw ex;
                }

                JOptionPane.showMessageDialog(this, "Enquiry submitted successfully! Vacancy reserved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                Window win = SwingUtilities.getWindowAncestor(this);
                if (win instanceof JFrame) {
                    ToastNotification.show((JFrame) win, "Enquiry submitted & vacancy reserved!", ToastNotification.Type.SUCCESS);
                }
                if (onStateChanged != null) onStateChanged.run();
            } catch (haven.exception.VacancyExhaustedException ex) {
                LOGGER.log(Level.INFO, "Booking rejected: vacancy exhausted for {0}", accommodation.getId());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Vacancy Unavailable", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, "Failed to submit enquiry for " + accommodation.getId(), ex);
                JOptionPane.showMessageDialog(this, "Failed to submit enquiry: " + ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createFallbackImagePanel(String title, int width, int height) {
        JPanel fallback = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(241, 245, 249), 0, height, new Color(226, 232, 240)));
                g2.fillRect(0, 0, width, height);
                g2.dispose();
            }
        };
        fallback.setPreferredSize(new Dimension(width, height));
        fallback.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel noImgLbl = new JLabel("No Image", SwingConstants.CENTER);
        noImgLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        noImgLbl.setForeground(HavenTheme.TEXT_SECONDARY);
        noImgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Available", SwingConstants.CENTER);
        subLbl.setFont(HavenTheme.FONT_BADGE);
        subLbl.setForeground(HavenTheme.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(noImgLbl);
        inner.add(Box.createVerticalStrut(2));
        inner.add(subLbl);
        fallback.add(inner);
        return fallback;
    }
}
