package haven.view;

import haven.db.UserDAO;
import haven.model.User;
import haven.event.ChatEvent;
import haven.event.HavenEventBus;
import haven.event.HavenEventListener;
import haven.event.NotificationEvent;
import haven.view.PanelMediator;
import haven.repository.NotificationRepository;
import haven.repository.RepositoryProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Top navigation header panel for the HAVEN application window.
 * Displays application branding, user badge, offline mode indicator,
 * persistent notification counter badge, profile trigger,
 * role/mode switcher (for Admin/Owner), and logout action.
 */
public class NavigationHeaderPanel extends JPanel {
    private final JFrame parentFrame;
    private final User currentUser;
    private final PanelMediator mediator;
    private final UserDAO userDAO;
    private final NotificationRepository notificationRepo;
    private final Runnable onProfileUpdated;
    private final Runnable onToggleMode;
    private final Runnable onLogout;

    private final JLabel userBadge;
    private final JButton notificationBellBtn;
    private final JButton switchModeBtn;

    private final HavenEventListener<NotificationEvent> notificationEventListener;
    private final HavenEventListener<ChatEvent> chatEventListener;

    public NavigationHeaderPanel(JFrame parentFrame, User currentUser, PanelMediator mediator,
                                 UserDAO userDAO, NotificationRepository notificationRepo,
                                 Runnable onProfileUpdated, Runnable onToggleMode, Runnable onLogout) {
        super(new BorderLayout());
        this.parentFrame = parentFrame;
        this.currentUser = currentUser;
        this.mediator = mediator;
        this.userDAO = userDAO;
        this.notificationRepo = notificationRepo != null ? notificationRepo : RepositoryProvider.getNotificationRepository();
        this.onProfileUpdated = onProfileUpdated;
        this.onToggleMode = onToggleMode;
        this.onLogout = onLogout;

        setBackground(HavenTheme.SURFACE_WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Brand Panel (Left)
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel brandLogo = new JLabel("HAVEN");
        brandLogo.setFont(new Font("SansSerif", Font.BOLD, 19));
        brandLogo.setForeground(HavenTheme.PRIMARY);

        JLabel brandSep = new JLabel("│");
        brandSep.setFont(new Font("SansSerif", Font.PLAIN, 15));
        brandSep.setForeground(HavenTheme.BORDER_SUBTLE);

        JLabel titleLabel = new JLabel("Student Accommodation Discovery");
        titleLabel.setFont(HavenTheme.FONT_SUBTITLE);
        titleLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        brandPanel.add(brandLogo);
        brandPanel.add(brandSep);
        brandPanel.add(titleLabel);

        // User Controls Panel (Right)
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

        userBadge = HavenTheme.createBadge(
                currentUser.getFullName() + " (" + currentUser.getRole() + ")",
                HavenTheme.TEXT_BODY,
                HavenTheme.BG_MUTED,
                HavenTheme.BORDER_LIGHT
        );

        notificationBellBtn = ModernButton.secondary("Notifications (0)");
        notificationBellBtn.setToolTipText("Open your persistent Notification Inbox");
        notificationBellBtn.addActionListener(e -> {
            NotificationInboxDialog dialog = new NotificationInboxDialog(parentFrame, currentUser, mediator, this::updateNotificationBadge);
            dialog.setVisible(true);
        });

        JButton profileBtn = ModernButton.secondary("Profile");
        profileBtn.setToolTipText("View or edit your profile and preferences");
        profileBtn.addActionListener(e -> {
            UserProfileDialog dialog = new UserProfileDialog(parentFrame, currentUser, this.userDAO, () -> {
                updateUserBadge();
                if (this.onProfileUpdated != null) {
                    this.onProfileUpdated.run();
                }
            });
            dialog.setVisible(true);
        });

        if (currentUser.isAdmin() || currentUser.isOwner()) {
            switchModeBtn = ModernButton.primary(currentUser.isAdmin() ? "Switch to Admin Portal" : "Switch to Student View");
            switchModeBtn.setToolTipText(currentUser.isAdmin() ? "Toggle between Admin Portal and Student View" : "Toggle between Owner Management and Student View");
            switchModeBtn.addActionListener(e -> {
                if (this.onToggleMode != null) {
                    this.onToggleMode.run();
                }
            });
        } else {
            switchModeBtn = null;
        }

        JButton logoutBtn = ModernButton.secondary("Logout");
        logoutBtn.addActionListener(e -> {
            if (this.onLogout != null) {
                this.onLogout.run();
            }
        });

        userControlsPanel.add(userBadge);
        userControlsPanel.add(notificationBellBtn);
        userControlsPanel.add(profileBtn);
        if (switchModeBtn != null) {
            userControlsPanel.add(switchModeBtn);
        }
        userControlsPanel.add(logoutBtn);

        add(brandPanel, BorderLayout.WEST);
        add(userControlsPanel, BorderLayout.EAST);

        // Register reactive notification and chat event listeners
        notificationEventListener = event -> SwingUtilities.invokeLater(this::updateNotificationBadge);
        chatEventListener = event -> SwingUtilities.invokeLater(this::updateNotificationBadge);
        HavenEventBus.getInstance().subscribe(NotificationEvent.class, notificationEventListener);
        HavenEventBus.getInstance().subscribe(ChatEvent.class, chatEventListener);

        updateNotificationBadge();
    }

    public void updateUserBadge() {
        if (userBadge != null && currentUser != null) {
            userBadge.setText(currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        }
    }

    public void updateNotificationBadge() {
        if (currentUser == null || notificationBellBtn == null) return;
        int unread = notificationRepo.countUnread(currentUser.getUsername());
        notificationBellBtn.setText("Notifications (" + unread + ")");
        if (unread > 0) {
            notificationBellBtn.setForeground(HavenTheme.WARNING);
        } else {
            notificationBellBtn.setForeground(HavenTheme.TEXT_BODY);
        }
    }

    public void updateModeButton(String activeMode) {
        if (switchModeBtn == null) return;
        if ("ADMIN".equals(activeMode) || "OWNER".equals(activeMode)) {
            switchModeBtn.setText("Switch to Student View");
        } else {
            switchModeBtn.setText(currentUser.isAdmin() ? "Switch to Admin Portal" : "Switch to Owner Portal");
        }
    }

    public void dispose() {
        if (notificationEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(NotificationEvent.class, notificationEventListener);
        }
        if (chatEventListener != null) {
            HavenEventBus.getInstance().unsubscribe(ChatEvent.class, chatEventListener);
        }
    }
}
