package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.ReviewDAO;
import haven.db.UserDAO;
import haven.model.Accommodation;
import haven.model.Student;
import haven.model.User;
import haven.pattern.event.*;
import haven.pattern.iterator.AccommodationFilterCriteria;
import haven.pattern.iterator.FilteredAccommodationIterator;
import haven.pattern.mediator.PanelMediator;
import haven.repository.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main application window for HAVEN.
 * Follows Dependency Inversion Principle by depending on Repository interfaces,
 * Observer Pattern via HavenEventBus for reactive synchronization,
 * Iterator Pattern via FilteredAccommodationIterator for advanced multi-criteria search,
 * and Mediator Pattern via PanelMediator for decoupled cross-panel navigation.
 *
 * Styled cleanly with HavenTheme design system (No Emojis).
 */
public class MainFrame extends JFrame implements PanelMediator {
    private final AccommodationRepository accommodationRepo;
    private final UserRepository userRepo;
    private final EnquiryRepository enquiryRepo;
    private final ReviewRepository reviewRepo;
    private final NotificationRepository notificationRepo;
    private final User currentUser;

    private final PanelMediator mediator;
    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    private final JPanel cardsContainer;

    private final JTextField searchField;
    private final JComboBox<String> collegeBox;
    private final JComboBox<String> genderBox;
    private final JComboBox<String> typeFilterBox;
    private final JTextField minRentField;
    private final JTextField rentField;
    private final JComboBox<String> maxDistanceBox;
    private final JComboBox<String> minRatingBox;
    private final JCheckBox vacanciesOnlyCheck;
    private final JCheckBox wishlistFilterCheck;
    private final JComboBox<String> sortBox;
    private final JLabel resultsCountLabel;

    private final JCheckBox wifiFilterCheck;
    private final JCheckBox foodFilterCheck;
    private final JCheckBox bathFilterCheck;
    private final JCheckBox cctvFilterCheck;
    private final JCheckBox powerFilterCheck;

    private final Map<String, Accommodation> comparedStays = new LinkedHashMap<>();
    private JPanel comparisonDock;
    private JLabel comparisonLabel;
    private JButton compareNowBtn;

    private final JTabbedPane studentTabbedPane;
    private final MapPanel mapPanel;
    private final OwnerDashboardPanel ownerPanel;
    private final AdminDashboardPanel adminPanel;
    private StudentEnquiriesPanel studentEnquiriesPanel;

    private final JButton notificationBellBtn;
    private final JButton switchModeBtn;
    private String activeMode = "STUDENT";

    private final HavenEventListener<AccommodationEvent> accommodationEventListener;
    private final HavenEventListener<EnquiryEvent> enquiryEventListener;
    private final HavenEventListener<ReviewEvent> reviewEventListener;
    private final HavenEventListener<NotificationEvent> notificationEventListener;
    private final HavenEventListener<ChatEvent> chatEventListener;

    public MainFrame(User currentUser) {
        this(currentUser,
             RepositoryProvider.getAccommodationRepository(),
             RepositoryProvider.getUserRepository(),
             RepositoryProvider.getEnquiryRepository(),
             RepositoryProvider.getReviewRepository());
    }

    public MainFrame(User currentUser, AccommodationRepository accommodationRepo, UserRepository userRepo,
                     EnquiryRepository enquiryRepo, ReviewRepository reviewRepo) {
        super("HAVEN - Student Accommodation Discovery & Governance Platform");
        this.currentUser = currentUser;
        this.accommodationRepo = accommodationRepo;
        this.userRepo = userRepo;
        this.enquiryRepo = enquiryRepo;
        this.reviewRepo = reviewRepo;
        this.notificationRepo = RepositoryProvider.getNotificationRepository();
        this.mediator = this;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1160, 820);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // =====================================================================
        // HEADER BAR WITH NOTIFICATION BELL, PROFILE & ROLE MODE SWITCHER
        // =====================================================================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(HavenTheme.BG_CANVAS);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel titleLabel = new JLabel("HAVEN : Student Accommodation Discovery");
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        JPanel userControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        userControlsPanel.setOpaque(false);

        if (RepositoryProvider.isOffline()) {
            JLabel demoBadge = HavenTheme.createBadge(
                    "Offline Store (File-Backed)",
                    HavenTheme.WARNING,
                    HavenTheme.WARNING_BG,
                    HavenTheme.WARNING_BORDER
            );
            userControlsPanel.add(demoBadge);
        }

        JLabel userBadge = HavenTheme.createBadge(
                currentUser.getFullName() + " (" + currentUser.getRole() + ")",
                HavenTheme.TEXT_BODY,
                HavenTheme.BG_MUTED,
                HavenTheme.BORDER_LIGHT
        );

        notificationBellBtn = ModernButton.secondary("Notifications (0)");
        notificationBellBtn.setToolTipText("Open your persistent Notification Inbox");
        notificationBellBtn.addActionListener(e -> {
            NotificationInboxDialog dialog = new NotificationInboxDialog(this, currentUser, mediator, this::updateNotificationBellBadge);
            dialog.setVisible(true);
        });
        updateNotificationBellBadge();
        HavenEventBus.getInstance().subscribe(NotificationEvent.class, e -> SwingUtilities.invokeLater(this::updateNotificationBellBadge));

        JButton profileBtn = ModernButton.secondary("Profile");
        profileBtn.setToolTipText("View or edit your profile and preferences");
        profileBtn.addActionListener(e -> {
            UserProfileDialog dialog = new UserProfileDialog(this, currentUser, getUserDAO(), () -> {
                userBadge.setText(currentUser.getFullName() + " (" + currentUser.getRole() + ")");
                applyFilters();
            });
            dialog.setVisible(true);
        });

        // Mode Switcher: Strictly reserved for Owner and Admin roles (Students cannot access Owner/Admin portals)
        if (currentUser.isAdmin() || currentUser.isOwner()) {
            switchModeBtn = ModernButton.primary(currentUser.isAdmin() ? "Switch to Admin Portal" : "Switch to Student View");
            switchModeBtn.setToolTipText(currentUser.isAdmin() ? "Toggle between Admin Portal and Student View" : "Toggle between Owner Management and Student View");
        } else {
            switchModeBtn = null;
        }

        JButton logoutBtn = ModernButton.secondary("Logout");
        logoutBtn.addActionListener(e -> handleLogout());

        userControlsPanel.add(userBadge);
        userControlsPanel.add(notificationBellBtn);
        userControlsPanel.add(profileBtn);
        if (switchModeBtn != null) {
            userControlsPanel.add(switchModeBtn);
        }
        userControlsPanel.add(logoutBtn);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(userControlsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Central Container
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // =====================================================================
        // STUDENT VIEW WITH 3-TIER ADVANCED SEARCH FILTER BAR
        // =====================================================================
        JPanel studentViewPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(HavenTheme.BG_MUTED);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(8, 16, 8, 16)
        ));

        // Tier 1: Search, College, Type, Gender, Price Range (Min - Max), Sort & Live Count Badge
        JPanel primaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        primaryRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        searchLbl.setForeground(HavenTheme.TEXT_BODY);
        primaryRow.add(searchLbl);

        searchField = new JTextField("", 12);
        searchField.setFont(HavenTheme.FONT_BODY);
        searchField.setPreferredSize(new Dimension(135, 30));
        searchField.setBorder(HavenTheme.createInputPadding());
        searchField.setToolTipText("Search by property name, road, or area (e.g. Pappanamcode, Sreekaryam)");
        primaryRow.add(searchField);

        JLabel collegeLbl = new JLabel("College:");
        collegeLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        collegeLbl.setForeground(HavenTheme.TEXT_BODY);
        primaryRow.add(collegeLbl);

        String[] collegeOptions = {"All Colleges", "SCTCE", "CET", "GEC Barton Hill", "Mar Baselios (MBCET)", "LBS Institute"};
        collegeBox = new JComboBox<>(collegeOptions);
        collegeBox.setFont(HavenTheme.FONT_BODY);
        collegeBox.setPreferredSize(new Dimension(145, 30));
        if (currentUser instanceof Student) {
            String studentCol = ((Student) currentUser).getCollegeName();
            if (studentCol != null) {
                for (int i = 0; i < collegeOptions.length; i++) {
                    if (studentCol.toLowerCase().contains(collegeOptions[i].toLowerCase())) {
                        collegeBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
        primaryRow.add(collegeBox);

        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        typeLbl.setForeground(HavenTheme.TEXT_BODY);
        primaryRow.add(typeLbl);

        typeFilterBox = new JComboBox<>(new String[]{"All Types", "Hostel", "PG"});
        typeFilterBox.setFont(HavenTheme.FONT_BODY);
        typeFilterBox.setPreferredSize(new Dimension(95, 30));
        primaryRow.add(typeFilterBox);

        JLabel genderLbl = new JLabel("Gender:");
        genderLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        genderLbl.setForeground(HavenTheme.TEXT_BODY);
        primaryRow.add(genderLbl);

        genderBox = new JComboBox<>(new String[]{"Any", "Boys", "Girls"});
        genderBox.setFont(HavenTheme.FONT_BODY);
        genderBox.setPreferredSize(new Dimension(78, 30));
        primaryRow.add(genderBox);

        JLabel rentLbl = new JLabel("Rent (₹):");
        rentLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        rentLbl.setForeground(HavenTheme.TEXT_BODY);
        primaryRow.add(rentLbl);

        minRentField = new JTextField("", 4);
        minRentField.setFont(HavenTheme.FONT_BODY);
        minRentField.setPreferredSize(new Dimension(56, 30));
        minRentField.setBorder(HavenTheme.createInputPadding());
        minRentField.setToolTipText("Minimum Monthly Rent (₹)");
        primaryRow.add(minRentField);

        primaryRow.add(new JLabel("-"));

        rentField = new JTextField("8000", 5);
        rentField.setFont(HavenTheme.FONT_BODY);
        rentField.setPreferredSize(new Dimension(64, 30));
        rentField.setBorder(HavenTheme.createInputPadding());
        rentField.setToolTipText("Maximum Monthly Rent (₹)");
        primaryRow.add(rentField);

        resultsCountLabel = HavenTheme.createBadge(
                "Showing all stays",
                HavenTheme.PRIMARY,
                HavenTheme.PRIMARY_LIGHT,
                HavenTheme.PRIMARY_BORDER
        );
        primaryRow.add(resultsCountLabel);

        filterPanel.add(primaryRow);

        // Tier 2: Sort By, Distance Filter, Min Rating Filter, Vacancies Toggle, Amenities Toggles, and Reset
        JPanel tier2Row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        tier2Row.setOpaque(false);

        JLabel sortLbl = new JLabel("Sort By:");
        sortLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        sortLbl.setForeground(HavenTheme.TEXT_PRIMARY);
        tier2Row.add(sortLbl);

        sortBox = new JComboBox<>(new String[]{
                "Default (Featured First)",
                "Rent: Low to High",
                "Rent: High to Low",
                "Distance: Nearest",
                "Vacancies: High to Low"
        });
        sortBox.setFont(HavenTheme.FONT_BODY);
        sortBox.setPreferredSize(new Dimension(175, 28));
        tier2Row.add(sortBox);

        tier2Row.add(Box.createHorizontalStrut(6));

        JLabel distLbl = new JLabel("Distance:");
        distLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        distLbl.setForeground(HavenTheme.TEXT_BODY);
        tier2Row.add(distLbl);

        maxDistanceBox = new JComboBox<>(new String[]{"Any Distance", "<= 0.5 km", "<= 1.0 km", "<= 2.0 km"});
        maxDistanceBox.setFont(HavenTheme.FONT_CAPTION);
        maxDistanceBox.setPreferredSize(new Dimension(108, 26));
        tier2Row.add(maxDistanceBox);

        JLabel ratingLbl = new JLabel("Min Rating:");
        ratingLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        ratingLbl.setForeground(HavenTheme.TEXT_BODY);
        tier2Row.add(ratingLbl);

        minRatingBox = new JComboBox<>(new String[]{"Any Rating", "★ 4.0+", "★ 4.5+"});
        minRatingBox.setFont(HavenTheme.FONT_CAPTION);
        minRatingBox.setPreferredSize(new Dimension(95, 26));
        tier2Row.add(minRatingBox);

        vacanciesOnlyCheck = new JCheckBox("Has Vacancies");
        vacanciesOnlyCheck.setFont(HavenTheme.FONT_CAPTION_BOLD);
        vacanciesOnlyCheck.setForeground(HavenTheme.SUCCESS);
        vacanciesOnlyCheck.setOpaque(false);
        tier2Row.add(vacanciesOnlyCheck);

        tier2Row.add(Box.createHorizontalStrut(6));

        JLabel amenityLabel = new JLabel("Amenities:");
        amenityLabel.setFont(HavenTheme.FONT_CAPTION_BOLD);
        amenityLabel.setForeground(HavenTheme.TEXT_BODY);
        tier2Row.add(amenityLabel);

        wifiFilterCheck = new JCheckBox("WiFi");
        wifiFilterCheck.setFont(HavenTheme.FONT_CAPTION);
        wifiFilterCheck.setForeground(HavenTheme.TEXT_BODY);
        wifiFilterCheck.setOpaque(false);
        tier2Row.add(wifiFilterCheck);

        foodFilterCheck = new JCheckBox("Food/Mess");
        foodFilterCheck.setFont(HavenTheme.FONT_CAPTION);
        foodFilterCheck.setForeground(HavenTheme.TEXT_BODY);
        foodFilterCheck.setOpaque(false);
        tier2Row.add(foodFilterCheck);

        bathFilterCheck = new JCheckBox("Attached Bath");
        bathFilterCheck.setFont(HavenTheme.FONT_CAPTION);
        bathFilterCheck.setForeground(HavenTheme.TEXT_BODY);
        bathFilterCheck.setOpaque(false);
        tier2Row.add(bathFilterCheck);

        cctvFilterCheck = new JCheckBox("CCTV");
        cctvFilterCheck.setFont(HavenTheme.FONT_CAPTION);
        cctvFilterCheck.setForeground(HavenTheme.TEXT_BODY);
        cctvFilterCheck.setOpaque(false);
        tier2Row.add(cctvFilterCheck);

        powerFilterCheck = new JCheckBox("Power Backup");
        powerFilterCheck.setFont(HavenTheme.FONT_CAPTION);
        powerFilterCheck.setForeground(HavenTheme.TEXT_BODY);
        powerFilterCheck.setOpaque(false);
        tier2Row.add(powerFilterCheck);

        wishlistFilterCheck = new JCheckBox("Saved Only");
        wishlistFilterCheck.setFont(HavenTheme.FONT_CAPTION_BOLD);
        wishlistFilterCheck.setForeground(HavenTheme.WARNING);
        wishlistFilterCheck.setOpaque(false);
        tier2Row.add(wishlistFilterCheck);

        JButton clearFiltersBtn = ModernButton.danger("Clear All");
        clearFiltersBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        clearFiltersBtn.addActionListener(e -> resetAllFilters());

        tier2Row.add(Box.createHorizontalStrut(4));
        tier2Row.add(clearFiltersBtn);

        filterPanel.add(tier2Row);
        studentViewPanel.add(filterPanel, BorderLayout.NORTH);

        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBorder(new EmptyBorder(12, 18, 12, 18));
        cardsContainer.setBackground(HavenTheme.BG_CANVAS);

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setBorder(null);
        studentViewPanel.add(scrollPane, BorderLayout.CENTER);

        // Comparison Dock (Bottom Bar)
        comparisonDock = new JPanel(new BorderLayout(12, 0));
        comparisonDock.setBackground(HavenTheme.SURFACE_DARK);
        comparisonDock.setBorder(new EmptyBorder(10, 20, 10, 20));
        comparisonDock.setVisible(false);

        comparisonLabel = new JLabel("0 of 3 stays selected for comparison");
        comparisonLabel.setFont(HavenTheme.FONT_BODY_BOLD);
        comparisonLabel.setForeground(Color.WHITE);

        JPanel dockButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dockButtons.setOpaque(false);

        JButton clearCompareBtn = ModernButton.secondary("Clear Selection");
        clearCompareBtn.addActionListener(e -> {
            comparedStays.clear();
            updateComparisonDock();
            applyFilters();
        });

        compareNowBtn = ModernButton.primary("Compare Now");
        compareNowBtn.setEnabled(false);
        compareNowBtn.addActionListener(e -> {
            if (comparedStays.size() < 2) {
                JOptionPane.showMessageDialog(this, "Select at least 2 accommodations to compare side-by-side.", "Comparison", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            CompareDialog dialog = new CompareDialog(this, new ArrayList<>(comparedStays.values()),
                    getHostelDAO(), getUserDAO(), getEnquiryDAO(), getReviewDAO(), currentUser, this::applyFilters);
            dialog.setVisible(true);
        });

        dockButtons.add(clearCompareBtn);
        dockButtons.add(compareNowBtn);
        comparisonDock.add(comparisonLabel, BorderLayout.CENTER);
        comparisonDock.add(dockButtons, BorderLayout.EAST);
        studentViewPanel.add(comparisonDock, BorderLayout.SOUTH);

        // Student Tabs: Discover Stays, Interactive Map & My Enquiries
        studentTabbedPane = new JTabbedPane();
        studentTabbedPane.setFont(HavenTheme.FONT_SUBTITLE);
        studentTabbedPane.addTab("Discover Stays", studentViewPanel);

        mapPanel = new MapPanel(getHostelDAO(), getUserDAO(), getEnquiryDAO(), getReviewDAO(), currentUser, this::applyFilters);
        studentTabbedPane.addTab("Interactive Campus Map", mapPanel);

        studentEnquiriesPanel = new StudentEnquiriesPanel(currentUser, getEnquiryDAO(), getHostelDAO(), this::applyFilters);
        studentTabbedPane.addTab("My Enquiries & Bookings", studentEnquiriesPanel);

        studentTabbedPane.addChangeListener(e -> {
            int idx = studentTabbedPane.getSelectedIndex();
            if (idx == 1 && mapPanel != null) {
                mapPanel.refreshMapData();
            } else if (idx == 2 && studentEnquiriesPanel != null) {
                studentEnquiriesPanel.refreshEnquiries();
            }
        });

        // Owner View & Admin View
        ownerPanel = new OwnerDashboardPanel(currentUser, getHostelDAO(), getEnquiryDAO(), this::applyFilters);
        adminPanel = new AdminDashboardPanel(currentUser, this::applyFilters);

        mainContainer.add(studentTabbedPane, "STUDENT");
        mainContainer.add(ownerPanel, "OWNER");
        mainContainer.add(adminPanel, "ADMIN");
        add(mainContainer, BorderLayout.CENTER);

        // Keystroke Debounce Timer
        javax.swing.Timer searchDebounceTimer = new javax.swing.Timer(250, e -> applyFilters());
        searchDebounceTimer.setRepeats(false);

        DocumentListener debouncedFilterListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
        };

        searchField.getDocument().addDocumentListener(debouncedFilterListener);
        minRentField.getDocument().addDocumentListener(debouncedFilterListener);
        rentField.getDocument().addDocumentListener(debouncedFilterListener);
        collegeBox.addActionListener(e -> applyFilters());
        typeFilterBox.addActionListener(e -> applyFilters());
        genderBox.addActionListener(e -> applyFilters());
        maxDistanceBox.addActionListener(e -> applyFilters());
        minRatingBox.addActionListener(e -> applyFilters());
        vacanciesOnlyCheck.addActionListener(e -> applyFilters());
        sortBox.addActionListener(e -> applyFilters());
        wishlistFilterCheck.addActionListener(e -> applyFilters());
        wifiFilterCheck.addActionListener(e -> applyFilters());
        foodFilterCheck.addActionListener(e -> applyFilters());
        bathFilterCheck.addActionListener(e -> applyFilters());
        cctvFilterCheck.addActionListener(e -> applyFilters());
        powerFilterCheck.addActionListener(e -> applyFilters());

        if (switchModeBtn != null) {
            switchModeBtn.addActionListener(e -> {
                if (currentUser.isAdmin()) {
                    if ("ADMIN".equals(activeMode)) {
                        showMode("STUDENT");
                    } else {
                        showMode("ADMIN");
                    }
                } else if (currentUser.isOwner()) {
                    if ("OWNER".equals(activeMode)) {
                        showMode("STUDENT");
                    } else {
                        showMode("OWNER");
                    }
                }
            });
        }

        // Observer Pattern: Register EventBus Subscribers for Reactive UI Sync
        accommodationEventListener = event -> SwingUtilities.invokeLater(() -> {
            applyFilters();
            if (mapPanel != null) mapPanel.refreshMapData();
            if (ownerPanel != null) ownerPanel.refreshTable();
            if (adminPanel != null) adminPanel.refreshAllTabs();
        });

        enquiryEventListener = event -> SwingUtilities.invokeLater(() -> {
            if (studentEnquiriesPanel != null) studentEnquiriesPanel.refreshEnquiries();
            if (ownerPanel != null) ownerPanel.refreshEnquiriesTable();
            if (adminPanel != null) adminPanel.refreshAllTabs();
            updateNotificationBellBadge();
        });

        reviewEventListener = event -> SwingUtilities.invokeLater(() -> {
            applyFilters();
            if (adminPanel != null) adminPanel.refreshAllTabs();
        });

        notificationEventListener = event -> SwingUtilities.invokeLater(this::updateNotificationBellBadge);
        chatEventListener = event -> SwingUtilities.invokeLater(this::updateNotificationBellBadge);

        HavenEventBus.getInstance().subscribe(AccommodationEvent.class, accommodationEventListener);
        HavenEventBus.getInstance().subscribe(EnquiryEvent.class, enquiryEventListener);
        HavenEventBus.getInstance().subscribe(ReviewEvent.class, reviewEventListener);
        HavenEventBus.getInstance().subscribe(NotificationEvent.class, notificationEventListener);
        HavenEventBus.getInstance().subscribe(ChatEvent.class, chatEventListener);

        // Initial setup based on user role
        if (currentUser.isAdmin()) {
            showMode("ADMIN");
            loadAllListings();
        } else if (currentUser.isOwner()) {
            showMode("OWNER");
            loadAllListings();
        } else {
            showMode("STUDENT");
            applyFilters();
        }
    }

    // =========================================================================
    // PanelMediator Interface Implementation (Mediator Pattern)
    // =========================================================================
    @Override
    public void showDiscoverView() {
        showMode("STUDENT");
        studentTabbedPane.setSelectedIndex(0);
        applyFilters();
    }

    @Override
    public void showStudentEnquiriesView() {
        if ("OWNER".equals(activeMode)) {
            ownerPanel.refreshEnquiriesTable();
        } else {
            showMode("STUDENT");
            studentTabbedPane.setSelectedIndex(2);
            studentEnquiriesPanel.refreshEnquiries();
        }
    }

    @Override
    public void showMapView(Accommodation focusAccommodation) {
        showMode("STUDENT");
        studentTabbedPane.setSelectedIndex(1);
        if (focusAccommodation != null) {
            mapPanel.focusOnAccommodation(focusAccommodation);
        } else {
            mapPanel.refreshMapData();
        }
    }

    @Override
    public void openChatDialog(Accommodation accommodation, String counterpartyUsername, String enquiryId) {
        new ChatDialog(this, currentUser, counterpartyUsername,
                accommodation != null ? accommodation.getId() : "",
                accommodation != null ? accommodation.getName() : "",
                enquiryId).setVisible(true);
    }

    @Override
    public void openImageGalleryDialog(Accommodation accommodation) {
        new ImageGalleryDialog(this, accommodation, currentUser, this::applyFilters).setVisible(true);
    }

    @Override
    public void openNotificationInbox() {
        new NotificationInboxDialog(this, currentUser, this, this::updateNotificationBellBadge).setVisible(true);
    }

    @Override
    public void refreshAllViews() {
        applyFilters();
        if (mapPanel != null) mapPanel.refreshMapData();
        if (ownerPanel != null) {
            ownerPanel.refreshTable();
            ownerPanel.refreshEnquiriesTable();
        }
        if (adminPanel != null) adminPanel.refreshAllTabs();
        updateNotificationBellBadge();
    }

    private void showMode(String mode) {
        // Strict RBAC Guard: Students are not authorized to view the Owner or Admin portals
        if (!"STUDENT".equals(mode) && currentUser.isStudent()) {
            mode = "STUDENT";
        }
        activeMode = mode;
        cardLayout.show(mainContainer, mode);
        if ("ADMIN".equals(mode)) {
            if (switchModeBtn != null) switchModeBtn.setText("Switch to Student View");
            adminPanel.refreshAllTabs();
        } else if ("OWNER".equals(mode)) {
            if (switchModeBtn != null) switchModeBtn.setText("Switch to Student View");
            ownerPanel.refreshTable();
            ownerPanel.refreshEnquiriesTable();
        } else {
            if (switchModeBtn != null) {
                switchModeBtn.setText(currentUser.isAdmin() ? "Switch to Admin Portal" : "Switch to Owner Portal");
            }
            applyFilters();
        }
    }

    private void updateNotificationBellBadge() {
        if (currentUser == null || notificationBellBtn == null) return;
        int unread = notificationRepo.countUnread(currentUser.getUsername());
        notificationBellBtn.setText("Notifications (" + unread + ")");
        if (unread > 0) {
            notificationBellBtn.setForeground(HavenTheme.WARNING);
        } else {
            notificationBellBtn.setForeground(HavenTheme.TEXT_BODY);
        }
    }

    public void resetAllFilters() {
        searchField.setText("");
        collegeBox.setSelectedIndex(0);
        typeFilterBox.setSelectedIndex(0);
        genderBox.setSelectedIndex(0);
        minRentField.setText("");
        rentField.setText("");
        maxDistanceBox.setSelectedIndex(0);
        minRatingBox.setSelectedIndex(0);
        vacanciesOnlyCheck.setSelected(false);
        wishlistFilterCheck.setSelected(false);
        wifiFilterCheck.setSelected(false);
        foodFilterCheck.setSelected(false);
        bathFilterCheck.setSelected(false);
        cctvFilterCheck.setSelected(false);
        powerFilterCheck.setSelected(false);
        sortBox.setSelectedIndex(0);
        applyFilters();
        ToastNotification.show(this, "Filters reset to show all stays", ToastNotification.Type.INFO);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginDialog login = new LoginDialog(null);
                login.setVisible(true);
                User authenticatedUser = login.getAuthenticatedUser();
                if (authenticatedUser != null) {
                    new MainFrame(authenticatedUser).setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        }
    }

    @Override
    public void dispose() {
        if (accommodationEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(AccommodationEvent.class, accommodationEventListener);
        }
        if (enquiryEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(EnquiryEvent.class, enquiryEventListener);
        }
        if (reviewEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(ReviewEvent.class, reviewEventListener);
        }
        if (notificationEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(NotificationEvent.class, notificationEventListener);
        }
        if (chatEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(ChatEvent.class, chatEventListener);
        }
        super.dispose();
    }

    private void loadAllListings() {
        cardsContainer.removeAll();
        List<Accommodation> list = accommodationRepo.findAll().stream()
                .filter(a -> !"REJECTED".equalsIgnoreCase(a.getModerationStatus()))
                .collect(Collectors.toList());
        populateCards(list);
    }

    private void applyFilters() {
        String selectedCollege = (String) collegeBox.getSelectedItem();
        String college = (selectedCollege == null || selectedCollege.startsWith("All")) ? "All" : selectedCollege;
        if (college.contains("MBCET")) college = "MBCET";
        else if (college.contains("Barton Hill")) college = "Barton Hill";
        else if (college.contains("LBS")) college = "LBS";
        else if (college.contains("CET")) college = "CET";
        else if (college.contains("SCTCE")) college = "SCTCE";

        String gender = (String) genderBox.getSelectedItem();
        String typeChoice = (String) typeFilterBox.getSelectedItem();
        String accType = "ALL";
        if ("Hostel".equalsIgnoreCase(typeChoice)) accType = "HOSTEL";
        else if ("PG".equalsIgnoreCase(typeChoice)) accType = "PG";

        double minRent = 0;
        String minRentText = minRentField.getText().trim();
        if (!minRentText.isEmpty()) {
            try { minRent = Double.parseDouble(minRentText); } catch (NumberFormatException ignored) {}
        }

        double maxRent = 0;
        String rentText = rentField.getText().trim();
        if (!rentText.isEmpty()) {
            try { maxRent = Double.parseDouble(rentText); } catch (NumberFormatException ignored) {}
        }

        double maxDistance = 0;
        String distChoice = (String) maxDistanceBox.getSelectedItem();
        if (distChoice != null && distChoice.contains("0.5")) maxDistance = 0.5;
        else if (distChoice != null && distChoice.contains("1.0")) maxDistance = 1.0;
        else if (distChoice != null && distChoice.contains("2.0")) maxDistance = 2.0;

        double minRating = 0;
        String ratingChoice = (String) minRatingBox.getSelectedItem();
        if (ratingChoice != null && ratingChoice.contains("4.0")) minRating = 4.0;
        else if (ratingChoice != null && ratingChoice.contains("4.5")) minRating = 4.5;

        List<String> requiredAmenities = new ArrayList<>();
        if (wifiFilterCheck.isSelected()) requiredAmenities.add("WiFi");
        if (foodFilterCheck.isSelected()) requiredAmenities.add("Food");
        if (bathFilterCheck.isSelected()) requiredAmenities.add("Washroom");
        if (cctvFilterCheck.isSelected()) requiredAmenities.add("CCTV");
        if (powerFilterCheck.isSelected()) requiredAmenities.add("Power Backup");

        // Build criteria and traverse using FilteredAccommodationIterator (Iterator Pattern)
        AccommodationFilterCriteria criteria = new AccommodationFilterCriteria()
                .setSearchQuery(searchField.getText().trim())
                .setCollege(college)
                .setGender(gender)
                .setAccommodationType(accType)
                .setMinRent(minRent)
                .setMaxRent(maxRent)
                .setMaxDistanceKm(maxDistance)
                .setMinRating(minRating)
                .setVacanciesOnly(vacanciesOnlyCheck.isSelected())
                .setApprovedOnly(true)
                .setRequiredAmenities(requiredAmenities);

        List<Accommodation> allStays = accommodationRepo.findAll();
        FilteredAccommodationIterator iterator = new FilteredAccommodationIterator(
                allStays, criteria, reviewRepo);
        List<Accommodation> list = iterator.toList();

        if (wishlistFilterCheck.isSelected()) {
            List<String> savedIds = userRepo.getWishlist(currentUser.getUsername());
            list = list.stream().filter(a -> savedIds.contains(a.getId())).collect(Collectors.toList());
        }

        String sortChoice = (String) sortBox.getSelectedItem();
        if ("Rent: Low to High".equals(sortChoice)) {
            list.sort(Comparator.comparingDouble(Accommodation::getBaseRent));
        } else if ("Rent: High to Low".equals(sortChoice)) {
            list.sort((a, b) -> Double.compare(b.getBaseRent(), a.getBaseRent()));
        } else if ("Distance: Nearest".equals(sortChoice)) {
            list.sort(Comparator.comparingDouble(Accommodation::getDistanceKm));
        } else if ("Vacancies: High to Low".equals(sortChoice)) {
            list.sort((a, b) -> Integer.compare(b.getVacancies(), a.getVacancies()));
        } else {
            // Default: Featured stays first, then by ID
            list.sort((a, b) -> Boolean.compare(b.isFeatured(), a.isFeatured()));
        }

        cardsContainer.removeAll();
        populateCards(list);
    }

    private void populateCards(List<Accommodation> list) {
        int totalStays = accommodationRepo.findAll().size();
        resultsCountLabel.setText("Showing " + list.size() + " of " + totalStays + " stays");

        if (list.isEmpty()) {
            if (wishlistFilterCheck.isSelected()) {
                cardsContainer.add(EmptyStatePanel.forWishlist(() -> {
                    wishlistFilterCheck.setSelected(false);
                    applyFilters();
                }));
            } else {
                cardsContainer.add(EmptyStatePanel.forSearch(this::resetAllFilters));
            }
        } else {
            HostelDAO hostelDaoAdapter = getHostelDAO();
            UserDAO userDaoAdapter = getUserDAO();
            EnquiryDAO enquiryDaoAdapter = getEnquiryDAO();
            ReviewDAO reviewDaoAdapter = getReviewDAO();
            for (Accommodation acc : list) {
                cardsContainer.add(new ListingCardPanel(
                        acc, hostelDaoAdapter, userDaoAdapter, enquiryDaoAdapter, reviewDaoAdapter, currentUser,
                        () -> {
                            applyFilters();
                            if (studentEnquiriesPanel != null) {
                                studentEnquiriesPanel.refreshEnquiries();
                            }
                        },
                        comparedStays.containsKey(acc.getId()),
                        this::handleToggleCompare,
                        this::showMapView
                ));
                cardsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private boolean handleToggleCompare(Accommodation acc) {
        if (comparedStays.containsKey(acc.getId())) {
            comparedStays.remove(acc.getId());
            updateComparisonDock();
            ToastNotification.show(this, "Removed from comparison: " + acc.getName(), ToastNotification.Type.INFO);
            return true;
        } else {
            if (comparedStays.size() >= 3) {
                JOptionPane.showMessageDialog(this, "You can compare up to 3 stays simultaneously.", "Comparison Limit", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            comparedStays.put(acc.getId(), acc);
            updateComparisonDock();
            ToastNotification.show(this, "Added to comparison dock: " + acc.getName(), ToastNotification.Type.SUCCESS);
            return true;
        }
    }

    private void updateComparisonDock() {
        int count = comparedStays.size();
        if (count == 0) {
            comparisonDock.setVisible(false);
        } else {
            comparisonDock.setVisible(true);
            comparisonLabel.setText(count + " of 3 stays selected for side-by-side comparison (" +
                    comparedStays.values().stream().map(Accommodation::getName).collect(Collectors.joining(", ")) + ")");
            compareNowBtn.setEnabled(count >= 2);
        }
        comparisonDock.revalidate();
        comparisonDock.repaint();
    }

    private HostelDAO getHostelDAO() {
        return new HostelDAO(accommodationRepo);
    }

    private UserDAO getUserDAO() {
        return new UserDAO(userRepo);
    }

    private EnquiryDAO getEnquiryDAO() {
        return new EnquiryDAO(enquiryRepo);
    }

    private ReviewDAO getReviewDAO() {
        return new ReviewDAO(reviewRepo);
    }
}
