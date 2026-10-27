package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.ReviewDAO;
import haven.db.UserDAO;
import haven.model.Accommodation;
import haven.model.User;
import haven.event.*;
import haven.view.PanelMediator;
import haven.repository.*;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * Main application window for HAVEN.
 * Integrates the discovery view, map panel, enquiries, and dashboard.
 * Uses PanelMediator for panel switching and HavenEventBus for live updates.
 */
public class MainFrame extends JFrame implements PanelMediator {
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
    private final AccommodationRepository accommodationRepo;
    private final UserRepository userRepo;
    private final EnquiryRepository enquiryRepo;
    private final ReviewRepository reviewRepo;
    private final NotificationRepository notificationRepo;
    private final User currentUser;

    private final PanelMediator mediator;
    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    private final StudentDiscoverPanel discoverPanel;

    private final JTabbedPane studentTabbedPane;
    private final MapPanel mapPanel;
    private final OwnerDashboardPanel ownerPanel;
    private final AdminDashboardPanel adminPanel;
    private StudentEnquiriesPanel studentEnquiriesPanel;

    private final NavigationHeaderPanel headerPanel;
    private String activeMode = "STUDENT";

    private final HavenEventListener<AccommodationEvent> accommodationEventListener;
    private final HavenEventListener<EnquiryEvent> enquiryEventListener;
    private final HavenEventListener<ReviewEvent> reviewEventListener;

    public MainFrame(User currentUser) {
        this(currentUser,
             RepositoryProvider.getAccommodationRepository(),
             RepositoryProvider.getUserRepository(),
             RepositoryProvider.getEnquiryRepository(),
             RepositoryProvider.getReviewRepository());
    }

    public MainFrame(User currentUser, AccommodationRepository accommodationRepo, UserRepository userRepo,
                     EnquiryRepository enquiryRepo, ReviewRepository reviewRepo) {
        super("HAVEN - Student Accommodation Discovery");
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

        headerPanel = new NavigationHeaderPanel(
                this,
                currentUser,
                mediator,
                getUserDAO(),
                notificationRepo,
                this::applyFilters,
                this::handleToggleMode,
                this::handleLogout
        );
        add(headerPanel, BorderLayout.NORTH);

        // Central Container
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        discoverPanel = new StudentDiscoverPanel(
                this,
                currentUser,
                mediator,
                () -> {
                    if (studentEnquiriesPanel != null) {
                        studentEnquiriesPanel.refreshEnquiries();
                    }
                }
        );

        // Student Tabs: Discover Stays, Interactive Map & My Enquiries
        studentTabbedPane = new JTabbedPane();
        studentTabbedPane.setFont(HavenTheme.FONT_SUBTITLE);
        studentTabbedPane.addTab("Discover Stays", discoverPanel);

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





        // Register EventBus listeners for reactive UI sync
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

        HavenEventBus.getInstance().subscribe(AccommodationEvent.class, accommodationEventListener);
        HavenEventBus.getInstance().subscribe(EnquiryEvent.class, enquiryEventListener);
        HavenEventBus.getInstance().subscribe(ReviewEvent.class, reviewEventListener);

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

    // PanelMediator implementation
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
        if (headerPanel != null) {
            headerPanel.updateModeButton(mode);
        }
        if ("ADMIN".equals(mode)) {
            adminPanel.refreshAllTabs();
        } else if ("OWNER".equals(mode)) {
            ownerPanel.refreshTable();
            ownerPanel.refreshEnquiriesTable();
        } else {
            applyFilters();
        }
    }

    private void handleToggleMode() {
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
    }

    private void updateNotificationBellBadge() {
        if (headerPanel != null) {
            headerPanel.updateNotificationBadge();
        }
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
        if (headerPanel != null) {
            headerPanel.dispose();
        }
        if (accommodationEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(AccommodationEvent.class, accommodationEventListener);
        }
        if (enquiryEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(EnquiryEvent.class, enquiryEventListener);
        }
        if (reviewEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(ReviewEvent.class, reviewEventListener);
        }
        super.dispose();
    }

    private void loadAllListings() {
        applyFilters();
    }

    private void applyFilters() {
        if (discoverPanel != null) {
            discoverPanel.applyFilters();
        }
    }

    public void resetAllFilters() {
        if (discoverPanel != null) {
            discoverPanel.resetAllFilters();
        }
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
