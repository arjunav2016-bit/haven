package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.UserDAO;
import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.PG;
import haven.model.User;
import haven.pattern.GoogleMapsAdapter;
import haven.pattern.PropertyImageStorage;
import haven.pattern.decorator.FeaturedAccommodationDecorator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * ListingCardPanel - Modern Three-Column Card Component for Accommodations.
 * Features:
 * - Left Property Photo Thumbnail (clickable to launch ImageGalleryDialog carousel)
 * - Center details column with Decorator Pattern Featured badge, verified ratings, metrics, and amenities
 * - Right pricing callout and action column (Enquire & Book, Cost Calculator, Direct Chat, Map, Wishlist)
 */
public class ListingCardPanel extends JPanel {
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

        // =========================================================================
        // LEFT COLUMN: PROPERTY PHOTO THUMBNAIL (Clickable for ImageGalleryDialog)
        // =========================================================================
        PropertyImageStorage.ensureDefaultImagesForAccommodation(accommodation);
        List<String> imgPaths = accommodation.getImagePaths();
        String primaryImg = imgPaths.isEmpty() ? null : imgPaths.get(0);

        JPanel thumbPanel = new JPanel(new BorderLayout(0, 4));
        thumbPanel.setOpaque(false);
        thumbPanel.setPreferredSize(new Dimension(136, 132));

        JLabel thumbLabel = new JLabel("", SwingConstants.CENTER);
        ImageIcon icon = PropertyImageStorage.loadScaledIcon(primaryImg, 134, 104);
        if (icon != null) {
            thumbLabel.setIcon(icon);
        } else {
            thumbLabel.setIcon(new ImageIcon(PropertyImageStorage.createPlaceholderImage(accommodation.getName(), 134, 104)));
        }
        thumbLabel.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        thumbLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        thumbLabel.setToolTipText("Click to open full photo gallery (" + imgPaths.size() + " photos)");
        thumbLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPhotoGallery();
            }
        });

        JButton galleryChipBtn = ModernButton.subtle("Photos (" + imgPaths.size() + ")");
        galleryChipBtn.setFont(HavenTheme.FONT_BADGE);
        galleryChipBtn.setBorder(new EmptyBorder(2, 4, 2, 4));
        galleryChipBtn.addActionListener(e -> openPhotoGallery());

        thumbPanel.add(thumbLabel, BorderLayout.CENTER);
        thumbPanel.add(galleryChipBtn, BorderLayout.SOUTH);
        add(thumbPanel, BorderLayout.WEST);

        // =========================================================================
        // CENTER CONTENT COLUMN
        // =========================================================================
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // 1. Title Row: Decorator Featured Badge + Type Badge + Title + Rating Badge + Compare Checkbox
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (accommodation.isFeatured()) {
            FeaturedAccommodationDecorator decorated = new FeaturedAccommodationDecorator(accommodation, "FEATURED", 0.0);
            JLabel featuredBadge = HavenTheme.createBadge(
                    decorated.getHighlightBadgeText(),
                    HavenTheme.WARNING,
                    HavenTheme.WARNING_BG,
                    HavenTheme.WARNING_BORDER
            );
            titleRow.add(featuredBadge);
        }

        boolean isHostel = (accommodation instanceof Hostel);
        JLabel typeBadge = HavenTheme.createBadge(
                isHostel ? "HOSTEL" : "PG / STAY",
                isHostel ? HavenTheme.PRIMARY : new Color(13, 148, 136),
                isHostel ? HavenTheme.PRIMARY_LIGHT : new Color(240, 253, 250),
                isHostel ? HavenTheme.PRIMARY_BORDER : new Color(153, 246, 228)
        );
        titleRow.add(typeBadge);

        JLabel titleLabel = new JLabel(accommodation.getName());
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);
        titleRow.add(titleLabel);

        // Verified Review Rating Badge
        double avgRating = this.reviewDAO.getAverageRating(accommodation.getId());
        int reviewCount = this.reviewDAO.getReviewCount(accommodation.getId());
        String ratingBadgeText = reviewCount > 0
                ? String.format("★ %.1f (%d)", avgRating, reviewCount)
                : "★ New";
        JButton ratingBadge = new JButton(ratingBadgeText);
        ratingBadge.setFont(HavenTheme.FONT_CAPTION_BOLD);
        ratingBadge.setForeground(HavenTheme.WARNING);
        ratingBadge.setBackground(HavenTheme.WARNING_BG);
        ratingBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.WARNING_BORDER, 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        ratingBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ratingBadge.setFocusPainted(false);
        ratingBadge.setToolTipText("View verified student reviews or write your experience");
        ratingBadge.addActionListener(e -> {
            ReviewsDialog dialog = new ReviewsDialog(SwingUtilities.getWindowAncestor(this), accommodation, this.reviewDAO, currentUser, onStateChanged);
            dialog.setVisible(true);
        });
        titleRow.add(ratingBadge);

        if (onCompareToggled != null) {
            JCheckBox compareCheck = new JCheckBox("Compare");
            compareCheck.setFont(HavenTheme.FONT_CAPTION_BOLD);
            compareCheck.setForeground(HavenTheme.TEXT_BODY);
            compareCheck.setOpaque(false);
            compareCheck.setSelected(isCompared);
            compareCheck.setToolTipText("Add or remove from side-by-side comparison dock");
            compareCheck.addActionListener(e -> {
                boolean accepted = onCompareToggled.test(accommodation);
                if (!accepted) {
                    compareCheck.setSelected(!compareCheck.isSelected());
                }
            });
            titleRow.add(compareCheck);
        }
        infoPanel.add(titleRow);

        // 2. Location & College Subtitle
        String locText = String.format("%.1f km from %s", accommodation.getDistanceFromCollege(), accommodation.getNearbyCollege());
        if (accommodation.getAddress() != null && !accommodation.getAddress().isEmpty()) {
            locText += " • " + accommodation.getAddress();
        }
        if (accommodation.getContactPhone() != null && !accommodation.getContactPhone().isEmpty()) {
            locText += " • Ph: " + accommodation.getContactPhone();
        }
        JLabel locLabel = new JLabel(locText);
        locLabel.setFont(HavenTheme.FONT_CAPTION);
        locLabel.setForeground(HavenTheme.TEXT_SECONDARY);
        locLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(locLabel);

        // 3. Metric Badges Row (Base Rent, Mess/Food, Vacancies, Gender, Curfew)
        JPanel metricsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metricsRow.setOpaque(false);
        metricsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        metricsRow.add(HavenTheme.createBadge(
                String.format("Rent: ₹%,.0f/mo", accommodation.getBaseRent()),
                HavenTheme.TEXT_BODY, HavenTheme.BG_MUTED, HavenTheme.BORDER_LIGHT
        ));

        if (isHostel) {
            Hostel h = (Hostel) accommodation;
            metricsRow.add(HavenTheme.createBadge(
                    String.format("Mess: ₹%,.0f", h.getMessFee()),
                    HavenTheme.TEXT_BODY, HavenTheme.BG_MUTED, HavenTheme.BORDER_LIGHT
            ));
            metricsRow.add(HavenTheme.createBadge(
                    "Curfew: " + h.getCurfewTime(),
                    HavenTheme.TEXT_SECONDARY, HavenTheme.BG_CANVAS, HavenTheme.BORDER_LIGHT
            ));
        } else {
            PG pg = (PG) accommodation;
            metricsRow.add(HavenTheme.createBadge(
                    pg.isFoodIncluded() ? "Food Included" : "Self-Cook",
                    HavenTheme.TEXT_BODY, HavenTheme.BG_MUTED, HavenTheme.BORDER_LIGHT
            ));
            metricsRow.add(HavenTheme.createBadge(
                    String.format("Utilities: ₹%,.0f", pg.getUtilityCharges()),
                    HavenTheme.TEXT_SECONDARY, HavenTheme.BG_CANVAS, HavenTheme.BORDER_LIGHT
            ));
        }

        int vacancies = accommodation.getVacancies();
        if (vacancies > 0) {
            metricsRow.add(HavenTheme.createBadge(
                    "● " + vacancies + " Bed" + (vacancies == 1 ? "" : "s") + " Available",
                    HavenTheme.SUCCESS, HavenTheme.SUCCESS_BG, HavenTheme.SUCCESS_BORDER
            ));
        } else {
            metricsRow.add(HavenTheme.createBadge(
                    "● Fully Booked",
                    HavenTheme.DANGER, HavenTheme.DANGER_BG, HavenTheme.DANGER_BORDER
            ));
        }

        metricsRow.add(HavenTheme.createBadge(
                accommodation.getGenderAllowed(),
                HavenTheme.PRIMARY, HavenTheme.PRIMARY_LIGHT, HavenTheme.PRIMARY_BORDER
        ));

        infoPanel.add(Box.createVerticalStrut(7));
        infoPanel.add(metricsRow);

        // 4. Amenities Tags Row (No Emojis)
        List<String> amenities = accommodation.getAmenities();
        if (amenities != null && !amenities.isEmpty()) {
            JPanel amenitiesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            amenitiesRow.setOpaque(false);
            amenitiesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (String am : amenities) {
                JLabel tag = new JLabel("✓ " + am);
                tag.setFont(HavenTheme.FONT_BADGE);
                tag.setForeground(HavenTheme.TEXT_BODY);
                tag.setBackground(HavenTheme.BG_CANVAS);
                tag.setOpaque(true);
                tag.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                        new EmptyBorder(2, 7, 2, 7)
                ));
                amenitiesRow.add(tag);
            }
            infoPanel.add(Box.createVerticalStrut(7));
            infoPanel.add(amenitiesRow);
        }

        infoPanel.add(Box.createVerticalGlue());
        add(infoPanel, BorderLayout.CENTER);

        // =========================================================================
        // RIGHT ACTION & PRICING COLUMN
        // =========================================================================
        JPanel actionCol = new JPanel();
        actionCol.setLayout(new BoxLayout(actionCol, BoxLayout.Y_AXIS));
        actionCol.setOpaque(false);
        actionCol.setPreferredSize(new Dimension(212, 152));
        actionCol.setMinimumSize(new Dimension(212, 152));

        double totalEst = accommodation.calculateTotalMonthlyCost(1);

        JLabel priceVal = new JLabel(String.format("₹%,.0f", totalEst));
        priceVal.setFont(HavenTheme.FONT_HERO);
        priceVal.setForeground(HavenTheme.PRIMARY);
        priceVal.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel priceSub = new JLabel("/ month total est.");
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
        actionCol.add(Box.createVerticalStrut(6));

        // Primary Action: Enquire & Book
        JButton enquireBtn = ModernButton.primary("Enquire & Book");
        enquireBtn.setPreferredSize(new Dimension(206, 30));
        enquireBtn.setMaximumSize(new Dimension(206, 30));
        enquireBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        enquireBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        enquireBtn.setEnabled(accommodation.getVacancies() > 0);
        enquireBtn.addActionListener(e -> openEnquiryDialog());
        actionCol.add(enquireBtn);
        actionCol.add(Box.createVerticalStrut(4));

        // Secondary Action: Cost Calculator
        JButton calcBtn = ModernButton.secondary("Cost Calculator");
        calcBtn.setPreferredSize(new Dimension(206, 26));
        calcBtn.setMaximumSize(new Dimension(206, 26));
        calcBtn.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        calcBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        calcBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        calcBtn.setToolTipText("Calculate monthly cost based on room sharing");
        calcBtn.addActionListener(e -> openCostCalculatorDialog());
        actionCol.add(calcBtn);
        actionCol.add(Box.createVerticalStrut(4));

        // Utility Row: Chat, Map & Wishlist
        JPanel utilRow = new JPanel(new GridLayout(1, 3, 4, 0));
        utilRow.setOpaque(false);
        utilRow.setPreferredSize(new Dimension(206, 26));
        utilRow.setMaximumSize(new Dimension(206, 26));
        utilRow.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton chatBtn = ModernButton.subtle("Chat");
        chatBtn.setFont(HavenTheme.FONT_CAPTION);
        chatBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        chatBtn.setToolTipText("Open direct chat with the property owner");
        chatBtn.addActionListener(e -> {
            new ChatDialog(SwingUtilities.getWindowAncestor(this), currentUser, accommodation).setVisible(true);
        });

        JButton mapBtn = ModernButton.subtle("Map");
        mapBtn.setFont(HavenTheme.FONT_CAPTION);
        mapBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        mapBtn.setToolTipText("View pin on HAVEN interactive map or open Google Maps");
        mapBtn.addActionListener(e -> {
            if (onViewMapCallback != null) {
                onViewMapCallback.accept(accommodation);
            } else {
                GoogleMapsAdapter adapter = new GoogleMapsAdapter();
                adapter.openLocation(accommodation.getLocationLink());
            }
        });

        wishlistBtn = ModernButton.subtle("☆ Save");
        wishlistBtn.setFont(HavenTheme.FONT_CAPTION);
        wishlistBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
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

    private void openPhotoGallery() {
        new ImageGalleryDialog(SwingUtilities.getWindowAncestor(this), accommodation, currentUser, onStateChanged).setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 178));
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, d.height);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(520, 172);
    }

    private void updateWishlistButtonLabel() {
        List<String> wishlist = userDAO.getWishlist(currentUser.getUsername());
        if (wishlist.contains(accommodation.getId())) {
            wishlistBtn.setText("★ Saved");
            wishlistBtn.setForeground(HavenTheme.WARNING);
        } else {
            wishlistBtn.setText("☆ Save");
            wishlistBtn.setForeground(HavenTheme.TEXT_BODY);
        }
    }

    private void openCostCalculatorDialog() {
        String[] options = {"1-Person (Single Room)", "2-Sharing Room", "3-Sharing Room"};
        JComboBox<String> sharingBox = new JComboBox<>(options);
        JLabel resultLabel = new JLabel();
        resultLabel.setFont(HavenTheme.FONT_BODY_BOLD);
        resultLabel.setForeground(HavenTheme.PRIMARY);

        JLabel breakdownLabel = new JLabel();
        breakdownLabel.setFont(HavenTheme.FONT_CAPTION);
        breakdownLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        Runnable updateCost = () -> {
            int sharing = sharingBox.getSelectedIndex() + 1;
            double totalCost = accommodation.calculateTotalMonthlyCost(sharing);
            resultLabel.setText(String.format("Estimated Total: ₹%,.0f / month", totalCost));
            if (accommodation instanceof Hostel) {
                Hostel h = (Hostel) accommodation;
                breakdownLabel.setText(String.format("Calculation: Base Rent factored (₹%,.0f) + Fixed Mess (₹%,.0f)",
                        (totalCost - h.getMessFee()), h.getMessFee()));
            } else {
                PG pg = (PG) accommodation;
                breakdownLabel.setText(String.format("Calculation: Rent + Utilities (₹%,.0f) %s",
                        pg.getUtilityCharges(), pg.isFoodIncluded() ? "+ Food Option" : ""));
            }
        };

        sharingBox.addActionListener(e -> updateCost.run());
        updateCost.run();

        JPanel calcPanel = new JPanel(new GridLayout(4, 1, 6, 6));
        calcPanel.add(new JLabel("Select Room Sharing Tier:"));
        calcPanel.add(sharingBox);
        calcPanel.add(resultLabel);
        calcPanel.add(breakdownLabel);

        JOptionPane.showMessageDialog(this, calcPanel, "Monthly Cost Calculator - " + accommodation.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    public void openEnquiryDialog() {
        String defaultPhone = (currentUser != null && currentUser.getPhone() != null) ? currentUser.getPhone() : "";
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
                    String username = (currentUser != null && currentUser.getUsername() != null) ? currentUser.getUsername() : "student";
                    enquiryDAO.saveEnquiry(accommodation.getId(), accommodation.getName(), accommodation.getOwnerId(), username, phone, message);
                } catch (Exception ex) {
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
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Vacancy Unavailable", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to submit enquiry: " + ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
