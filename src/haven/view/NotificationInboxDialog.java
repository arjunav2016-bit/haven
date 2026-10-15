package haven.view;

import haven.model.Notification;
import haven.model.User;
import haven.pattern.event.HavenEventBus;
import haven.pattern.event.HavenEventListener;
import haven.pattern.event.NotificationEvent;
import haven.pattern.mediator.PanelMediator;
import haven.repository.NotificationRepository;
import haven.repository.RepositoryProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * NotificationInboxDialog - Persistent Notification Inbox dialog.
 * Displays unread and historical notifications with timestamps, semantic type badges,
 * Mark Read / Mark All as Read actions, and Mediator-driven navigation.
 */
public class NotificationInboxDialog extends JDialog {
    private final User currentUser;
    private final PanelMediator mediator;
    private final NotificationRepository notificationRepo;
    private final Runnable onBadgeChanged;

    private final JPanel listContainer;
    private final JLabel unreadCountBadge;
    private final HavenEventListener<NotificationEvent> notifListener;

    public NotificationInboxDialog(Window parent, User currentUser, PanelMediator mediator, Runnable onBadgeChanged) {
        super(parent, "Notification Inbox — @" + (currentUser != null ? currentUser.getUsername() : "user"), ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.mediator = mediator;
        this.notificationRepo = RepositoryProvider.getNotificationRepository();
        this.onBadgeChanged = onBadgeChanged;

        setSize(580, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(HavenTheme.BG_CANVAS);

        // Header Bar
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(HavenTheme.SURFACE_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JPanel titleCol = new JPanel(new GridLayout(2, 1, 0, 2));
        titleCol.setOpaque(false);
        JLabel titleLbl = new JLabel("Notifications & Activity Inbox");
        titleLbl.setFont(HavenTheme.FONT_TITLE);
        titleLbl.setForeground(HavenTheme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel("Real-time alerts for enquiries, messages, reviews, and platform updates");
        subLbl.setFont(HavenTheme.FONT_CAPTION);
        subLbl.setForeground(HavenTheme.TEXT_SECONDARY);
        titleCol.add(titleLbl);
        titleCol.add(subLbl);

        header.add(titleCol, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        headerRight.setOpaque(false);
        unreadCountBadge = HavenTheme.createBadge("0 Unread", HavenTheme.PRIMARY, HavenTheme.PRIMARY_LIGHT, HavenTheme.PRIMARY_BORDER);
        headerRight.add(unreadCountBadge);

        JButton markAllBtn = ModernButton.secondary("Mark All Read");
        markAllBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        markAllBtn.addActionListener(e -> {
            if (currentUser != null) {
                notificationRepo.markAllAsRead(currentUser.getUsername());
                refreshInbox();
                if (onBadgeChanged != null) onBadgeChanged.run();
            }
        });
        headerRight.add(markAllBtn);

        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Scrollable Notifications List
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(HavenTheme.BG_CANVAS);
        listContainer.setBorder(new EmptyBorder(14, 18, 14, 18));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(HavenTheme.SURFACE_WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, HavenTheme.BORDER_LIGHT));
        JButton closeBtn = ModernButton.subtle("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);

        notifListener = event -> SwingUtilities.invokeLater(this::refreshInbox);
        HavenEventBus.getInstance().subscribe(NotificationEvent.class, notifListener);

        refreshInbox();
    }

    public void refreshInbox() {
        listContainer.removeAll();
        String username = (currentUser != null) ? currentUser.getUsername() : "student";
        List<Notification> list = notificationRepo.findByRecipient(username);
        int unread = notificationRepo.countUnread(username);

        unreadCountBadge.setText(unread + " Unread");
        if (unread > 0) {
            unreadCountBadge.setForeground(HavenTheme.WARNING);
            unreadCountBadge.setBackground(HavenTheme.WARNING_BG);
        } else {
            unreadCountBadge.setForeground(HavenTheme.SUCCESS);
            unreadCountBadge.setBackground(HavenTheme.SUCCESS_BG);
        }

        if (list.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(70, 20, 70, 20));
            JLabel lbl = new JLabel("Your notification inbox is empty.", SwingConstants.CENTER);
            lbl.setFont(HavenTheme.FONT_BODY);
            lbl.setForeground(HavenTheme.TEXT_SECONDARY);
            empty.add(lbl, BorderLayout.CENTER);
            listContainer.add(empty);
        } else {
            for (Notification n : list) {
                listContainer.add(createNotificationCard(n));
                listContainer.add(Box.createVerticalStrut(8));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createNotificationCard(Notification n) {
        JPanel card = new JPanel(new BorderLayout(12, 6));
        card.setBackground(n.isRead() ? HavenTheme.SURFACE_WHITE : HavenTheme.PRIMARY_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(n.isRead() ? HavenTheme.BORDER_LIGHT : HavenTheme.PRIMARY_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Top row: Type Badge + Title + Timestamp
        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftTitle.setOpaque(false);

        JLabel typeBadge = HavenTheme.createBadge(
                n.getType().name(),
                n.isRead() ? HavenTheme.TEXT_SECONDARY : HavenTheme.PRIMARY,
                n.isRead() ? HavenTheme.BG_MUTED : HavenTheme.SURFACE_WHITE,
                HavenTheme.BORDER_LIGHT
        );
        leftTitle.add(typeBadge);

        JLabel titleLbl = new JLabel((n.isRead() ? "" : "● ") + n.getTitle());
        titleLbl.setFont(n.isRead() ? HavenTheme.FONT_BODY : HavenTheme.FONT_BODY_BOLD);
        titleLbl.setForeground(HavenTheme.TEXT_PRIMARY);
        leftTitle.add(titleLbl);

        JLabel timeLbl = new JLabel(n.getFormattedTime());
        timeLbl.setFont(HavenTheme.FONT_CAPTION);
        timeLbl.setForeground(HavenTheme.TEXT_SECONDARY);

        topRow.add(leftTitle, BorderLayout.WEST);
        topRow.add(timeLbl, BorderLayout.EAST);

        // Message body + Action buttons
        JPanel bottomRow = new JPanel(new BorderLayout(8, 0));
        bottomRow.setOpaque(false);

        JLabel msgLbl = new JLabel(n.getMessage());
        msgLbl.setFont(HavenTheme.FONT_CAPTION);
        msgLbl.setForeground(HavenTheme.TEXT_BODY);
        bottomRow.add(msgLbl, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        if (!n.isRead()) {
            JButton readBtn = ModernButton.subtle("Mark Read");
            readBtn.setFont(HavenTheme.FONT_BADGE);
            readBtn.addActionListener(e -> {
                notificationRepo.markAsRead(n.getId());
                refreshInbox();
                if (onBadgeChanged != null) onBadgeChanged.run();
            });
            actions.add(readBtn);
        }

        if (n.getType() == Notification.Type.ENQUIRY && mediator != null) {
            JButton viewBtn = ModernButton.secondary("Open Enquiries");
            viewBtn.setFont(HavenTheme.FONT_BADGE);
            viewBtn.addActionListener(e -> {
                notificationRepo.markAsRead(n.getId());
                if (onBadgeChanged != null) onBadgeChanged.run();
                dispose();
                mediator.showStudentEnquiriesView();
            });
            actions.add(viewBtn);
        } else if (n.getType() == Notification.Type.CHAT && !n.getLinkedEntityId().isEmpty()) {
            JButton openChatBtn = ModernButton.secondary("Reply");
            openChatBtn.setFont(HavenTheme.FONT_BADGE);
            openChatBtn.addActionListener(e -> {
                notificationRepo.markAsRead(n.getId());
                if (onBadgeChanged != null) onBadgeChanged.run();
                RepositoryProvider.getAccommodationRepository().findById(n.getLinkedEntityId()).ifPresent(acc -> {
                    dispose();
                    new ChatDialog(getOwner(), currentUser, acc).setVisible(true);
                });
            });
            actions.add(openChatBtn);
        }

        bottomRow.add(actions, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);
        card.add(bottomRow, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void dispose() {
        if (notifListener != null) {
            HavenEventBus.getInstance().unsubscribe(NotificationEvent.class, notifListener);
        }
        super.dispose();
    }
}
