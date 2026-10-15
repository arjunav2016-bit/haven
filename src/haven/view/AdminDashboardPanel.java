package haven.view;

import haven.db.InMemoryStore;
import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.model.Hostel;
import haven.model.Review;
import haven.model.User;
import haven.pattern.ReceiptGenerator;
import haven.pattern.command.*;
import haven.pattern.report.SystemAnalyticsReport;
import haven.repository.*;
import haven.repository.inmemory.FilePersistenceManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminDashboardPanel - Enterprise Platform Governance & Moderation Portal (ADMIN Role).
 * Features:
 * - Command Pattern Undo / Redo Toolbar for reversible administrative actions
 * - Tab 1: User Account Governance (Activate / Deactivate / Delete / Register)
 * - Tab 2: Property Moderation (Approve / Flag / Reject / Toggle Featured Badge / Photo Audit)
 * - Tab 3: Student Review Moderation (Delete / Flag Reviews with Command Undo)
 * - Tab 4: System-Wide Analytics & Custom Java2D Charts + Template Method Report Export
 * - Tab 5: Persistence Engine & Seed Data Management
 */
public class AdminDashboardPanel extends JPanel {
    private final User currentUser;
    private final AccommodationRepository accRepo;
    private final UserRepository userRepo;
    private final EnquiryRepository enquiryRepo;
    private final ReviewRepository reviewRepo;
    private final Runnable onDataChanged;

    private final CommandHistory commandHistory = new CommandHistory();
    private final JButton undoBtn;
    private final JButton redoBtn;
    private final JLabel lastActionLabel;

    // Tab 1: Users
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private List<User> userList = new ArrayList<>();

    // Tab 2: Properties
    private DefaultTableModel propTableModel;
    private JTable propTable;
    private List<Accommodation> propList = new ArrayList<>();

    // Tab 3: Reviews
    private DefaultTableModel reviewTableModel;
    private JTable reviewTable;
    private List<Review> reviewList = new ArrayList<>();

    // Tab 4 & 5: Analytics & Data
    private JPanel analyticsChartContainer;
    private JLabel dbStatusSummaryLabel;

    public AdminDashboardPanel(User currentUser, Runnable onDataChanged) {
        this.currentUser = currentUser;
        this.accRepo = RepositoryProvider.getAccommodationRepository();
        this.userRepo = RepositoryProvider.getUserRepository();
        this.enquiryRepo = RepositoryProvider.getEnquiryRepository();
        this.reviewRepo = RepositoryProvider.getReviewRepository();
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout());
        setBackground(HavenTheme.BG_CANVAS);

        // Top Command History Undo / Redo Governance Bar
        JPanel governanceBar = new JPanel(new BorderLayout(12, 0));
        governanceBar.setBackground(HavenTheme.SURFACE_WHITE);
        governanceBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTitle.setOpaque(false);
        JLabel adminBadge = HavenTheme.createBadge("SUPER ADMIN PORTAL", HavenTheme.PRIMARY, HavenTheme.PRIMARY_LIGHT, HavenTheme.PRIMARY_BORDER);
        leftTitle.add(adminBadge);

        lastActionLabel = new JLabel("Command History Ready — All moderation actions support Undo / Redo");
        lastActionLabel.setFont(HavenTheme.FONT_CAPTION);
        lastActionLabel.setForeground(HavenTheme.TEXT_SECONDARY);
        leftTitle.add(lastActionLabel);
        governanceBar.add(leftTitle, BorderLayout.WEST);

        JPanel rightUndoRedo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightUndoRedo.setOpaque(false);

        undoBtn = ModernButton.secondary("Undo Action");
        undoBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        undoBtn.setEnabled(false);
        undoBtn.addActionListener(e -> {
            AdminCommand undone = commandHistory.undo();
            if (undone != null) {
                lastActionLabel.setText("Undid: " + undone.getDescription());
                refreshAllTabs();
                if (onDataChanged != null) onDataChanged.run();
            }
        });

        redoBtn = ModernButton.secondary("Redo Action");
        redoBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        redoBtn.setEnabled(false);
        redoBtn.addActionListener(e -> {
            AdminCommand redone = commandHistory.redo();
            if (redone != null) {
                lastActionLabel.setText("Redid: " + redone.getDescription());
                refreshAllTabs();
                if (onDataChanged != null) onDataChanged.run();
            }
        });

        JButton refreshAllBtn = ModernButton.subtle("Refresh All");
        refreshAllBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        refreshAllBtn.addActionListener(e -> refreshAllTabs());

        rightUndoRedo.add(undoBtn);
        rightUndoRedo.add(redoBtn);
        rightUndoRedo.add(refreshAllBtn);
        governanceBar.add(rightUndoRedo, BorderLayout.EAST);

        add(governanceBar, BorderLayout.NORTH);

        // Main 5-Tab Admin Workspace
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(HavenTheme.FONT_SUBTITLE);
        tabs.addTab("User Management", createUsersTab());
        tabs.addTab("Property Moderation", createPropertiesTab());
        tabs.addTab("Review Moderation", createReviewsTab());
        tabs.addTab("Platform Analytics", createAnalyticsTab());
        tabs.addTab("Seed & Data Store", createSeedDataTab());

        tabs.addChangeListener(e -> refreshAllTabs());
        add(tabs, BorderLayout.CENTER);

        refreshAllTabs();
    }

    private void executeAdminCommand(AdminCommand cmd) {
        if (cmd != null) {
            commandHistory.executeCommand(cmd);
            lastActionLabel.setText("Executed: " + cmd.getDescription());
            refreshAllTabs();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void updateUndoRedoButtons() {
        undoBtn.setEnabled(commandHistory.canUndo());
        undoBtn.setToolTipText(commandHistory.canUndo() ? "Undo: " + commandHistory.getLastUndoDescription() : "Nothing to undo");
        redoBtn.setEnabled(commandHistory.canRedo());
        redoBtn.setToolTipText(commandHistory.canRedo() ? "Redo: " + commandHistory.getLastRedoDescription() : "Nothing to redo");
    }

    public void refreshAllTabs() {
        refreshUsersTable();
        refreshPropertiesTable();
        refreshReviewsTable();
        if (analyticsChartContainer != null) {
            analyticsChartContainer.repaint();
        }
        if (dbStatusSummaryLabel != null) {
            dbStatusSummaryLabel.setText(buildStoreMetricsSummary());
        }
        updateUndoRedoButtons();
    }

    // =========================================================================
    // TAB 1: USER MANAGEMENT
    // =========================================================================
    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(HavenTheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"Username", "Full Name", "Role", "Phone", "Saved Stays", "Status"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new JTable(userTableModel);
        HavenTheme.styleTable(userTable);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane sp = new JScrollPane(userTable);
        sp.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        panel.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actions.setOpaque(false);

        JButton toggleActiveBtn = ModernButton.secondary("Toggle Active / Deactivate");
        toggleActiveBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0 || row >= userList.size()) {
                JOptionPane.showMessageDialog(this, "Select a user account first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            User target = userList.get(row);
            if ("admin".equalsIgnoreCase(target.getUsername())) {
                JOptionPane.showMessageDialog(this, "The primary admin account cannot be deactivated.", "Protected Account", JOptionPane.WARNING_MESSAGE);
                return;
            }
            executeAdminCommand(new ToggleUserActiveCommand(userRepo, target.getUsername(), !target.isActive()));
        });

        JButton deleteUserBtn = ModernButton.danger("Delete User (Undoable)");
        deleteUserBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0 || row >= userList.size()) {
                JOptionPane.showMessageDialog(this, "Select a user account to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            User target = userList.get(row);
            if ("admin".equalsIgnoreCase(target.getUsername())) {
                JOptionPane.showMessageDialog(this, "The primary admin account cannot be deleted.", "Protected Account", JOptionPane.WARNING_MESSAGE);
                return;
            }
            executeAdminCommand(new DeleteUserCommand(userRepo, target.getUsername()));
        });

        actions.add(toggleActiveBtn);
        actions.add(deleteUserBtn);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshUsersTable() {
        if (userTableModel == null) return;
        userTableModel.setRowCount(0);
        userList = userRepo.findAll();
        for (User u : userList) {
            userTableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    u.getPhone(),
                    u.getWishlist() != null ? u.getWishlist().size() : 0,
                    u.isActive() ? "ACTIVE" : "DEACTIVATED"
            });
        }
    }

    // =========================================================================
    // TAB 2: PROPERTY MODERATION
    // =========================================================================
    private JPanel createPropertiesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(HavenTheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"ID", "Property Name", "Type", "College", "Rent (₹)", "Vacancies", "Featured", "Moderation Status"};
        propTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        propTable = new JTable(propTableModel);
        HavenTheme.styleTable(propTable);
        propTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane sp = new JScrollPane(propTable);
        sp.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        panel.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);

        JButton photosBtn = ModernButton.subtle("Inspect Photos");
        photosBtn.addActionListener(e -> {
            int row = propTable.getSelectedRow();
            if (row < 0 || row >= propList.size()) return;
            Accommodation acc = propList.get(row);
            new ImageGalleryDialog(SwingUtilities.getWindowAncestor(this), acc, currentUser, this::refreshAllTabs).setVisible(true);
        });

        JButton toggleFeaturedBtn = ModernButton.secondary("Toggle ★ Featured");
        toggleFeaturedBtn.addActionListener(e -> {
            int row = propTable.getSelectedRow();
            if (row < 0 || row >= propList.size()) {
                JOptionPane.showMessageDialog(this, "Select a property first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Accommodation acc = propList.get(row);
            executeAdminCommand(new ModerateListingCommand(accRepo, acc.getId(), acc.getModerationStatus(), !acc.isFeatured()));
        });

        JButton approveBtn = ModernButton.primary("Approve Listing");
        approveBtn.addActionListener(e -> moderateSelectedProperty("APPROVED"));

        JButton flagBtn = ModernButton.secondary("Flag for Audit");
        flagBtn.addActionListener(e -> moderateSelectedProperty("FLAGGED"));

        JButton rejectBtn = ModernButton.danger("Reject Listing");
        rejectBtn.addActionListener(e -> moderateSelectedProperty("REJECTED"));

        actions.add(photosBtn);
        actions.add(toggleFeaturedBtn);
        actions.add(approveBtn);
        actions.add(flagBtn);
        actions.add(rejectBtn);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void moderateSelectedProperty(String newStatus) {
        int row = propTable.getSelectedRow();
        if (row < 0 || row >= propList.size()) {
            JOptionPane.showMessageDialog(this, "Select a property from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Accommodation acc = propList.get(row);
        executeAdminCommand(new ModerateListingCommand(accRepo, acc.getId(), newStatus, acc.isFeatured()));
    }

    private void refreshPropertiesTable() {
        if (propTableModel == null) return;
        propTableModel.setRowCount(0);
        propList = accRepo.findAll();
        for (Accommodation a : propList) {
            propTableModel.addRow(new Object[]{
                    a.getId(),
                    a.getName(),
                    (a instanceof Hostel) ? "Hostel" : "PG",
                    a.getNearbyCollege(),
                    String.format("₹%,.0f", a.getBaseRent()),
                    a.getVacancies(),
                    a.isFeatured() ? "★ Featured" : "Standard",
                    a.getModerationStatus()
            });
        }
    }

    // =========================================================================
    // TAB 3: REVIEW MODERATION
    // =========================================================================
    private JPanel createReviewsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(HavenTheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"Review ID", "Property ID", "Student", "Author", "Rating", "Status", "Comment"};
        reviewTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        reviewTable = new JTable(reviewTableModel);
        HavenTheme.styleTable(reviewTable);
        reviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reviewTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane sp = new JScrollPane(reviewTable);
        sp.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        panel.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actions.setOpaque(false);

        JButton flagReviewBtn = ModernButton.secondary("Toggle Flagged");
        flagReviewBtn.addActionListener(e -> {
            int row = reviewTable.getSelectedRow();
            if (row < 0 || row >= reviewList.size()) {
                JOptionPane.showMessageDialog(this, "Select a review first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Review r = reviewList.get(row);
            r.setFlagged(!r.isFlagged());
            reviewRepo.save(r);
            refreshReviewsTable();
        });

        JButton deleteReviewBtn = ModernButton.danger("Delete Review (Undoable)");
        deleteReviewBtn.addActionListener(e -> {
            int row = reviewTable.getSelectedRow();
            if (row < 0 || row >= reviewList.size()) {
                JOptionPane.showMessageDialog(this, "Select a review to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Review r = reviewList.get(row);
            executeAdminCommand(new DeleteReviewCommand(reviewRepo, r));
        });

        actions.add(flagReviewBtn);
        actions.add(deleteReviewBtn);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshReviewsTable() {
        if (reviewTableModel == null) return;
        reviewTableModel.setRowCount(0);
        reviewList = reviewRepo.findAll();
        for (Review r : reviewList) {
            reviewTableModel.addRow(new Object[]{
                    r.getId(),
                    r.getAccommodationId(),
                    "@" + r.getUsername(),
                    r.getAuthorName(),
                    r.getStarsDisplay() + " (" + r.getRating() + ")",
                    r.isFlagged() ? "FLAGGED" : "APPROVED",
                    r.getComment()
            });
        }
    }

    // =========================================================================
    // TAB 4: SYSTEM-WIDE ANALYTICS & REPORT EXPORT
    // =========================================================================
    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(HavenTheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        analyticsChartContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintSystemAnalytics((Graphics2D) g);
            }
        };
        analyticsChartContainer.setBackground(HavenTheme.SURFACE_WHITE);
        analyticsChartContainer.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        panel.add(analyticsChartContainer, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        footer.setOpaque(false);

        JButton exportReportBtn = ModernButton.primary("Export System Analytics Report (.txt)");
        exportReportBtn.addActionListener(e -> {
            SystemAnalyticsReport generator = new SystemAnalyticsReport(
                    userRepo.findAll(),
                    accRepo.findAll(),
                    enquiryRepo.findAll(),
                    reviewRepo.findAll()
            );
            String reportContent = generator.generate();
            ReceiptGenerator.exportToFileWithDialog(this, "HAVEN_System_Analytics_Report.txt",
                    reportContent, "Export Platform Analytics Report");
        });
        footer.add(exportReportBtn);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private void paintSystemAnalytics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = analyticsChartContainer.getWidth();
        int h = analyticsChartContainer.getHeight();

        List<Accommodation> stays = accRepo.findAll();
        List<User> users = userRepo.findAll();
        List<Enquiry> enquiries = enquiryRepo.findAll();
        List<Review> reviews = reviewRepo.findAll();

        int totalBeds = stays.stream().mapToInt(Accommodation::getVacancies).sum();
        double avgRent = stays.isEmpty() ? 0 : stays.stream().mapToDouble(Accommodation::getBaseRent).average().orElse(0);

        // KPI Row at Top
        String[] kpiTitles = {"Total Properties", "Registered Users", "Available Beds", "Avg Monthly Rent", "Total Enquiries", "Verified Reviews"};
        String[] kpiVals = {
                String.valueOf(stays.size()),
                String.valueOf(users.size()),
                String.valueOf(totalBeds),
                String.format("₹%,.0f", avgRent),
                String.valueOf(enquiries.size()),
                String.valueOf(reviews.size())
        };

        int cardW = Math.max(120, (w - 90) / 6);
        for (int i = 0; i < 6; i++) {
            int x = 20 + i * (cardW + 10);
            int y = 20;
            g2.setColor(HavenTheme.BG_CANVAS);
            g2.fillRoundRect(x, y, cardW, 68, 10, 10);
            g2.setColor(HavenTheme.BORDER_LIGHT);
            g2.drawRoundRect(x, y, cardW, 68, 10, 10);

            g2.setFont(HavenTheme.FONT_CAPTION);
            g2.setColor(HavenTheme.TEXT_SECONDARY);
            g2.drawString(kpiTitles[i], x + 12, y + 24);

            g2.setFont(HavenTheme.FONT_TITLE);
            g2.setColor(HavenTheme.PRIMARY);
            g2.drawString(kpiVals[i], x + 12, y + 52);
        }

        // Left Chart: Properties by Engineering Campus
        int chartY = 115;
        int chartW = (w - 60) / 2;

        g2.setColor(HavenTheme.TEXT_PRIMARY);
        g2.setFont(HavenTheme.FONT_SUBTITLE);
        g2.drawString("Accommodations by Engineering Campus", 24, chartY);

        Map<String, Integer> byCollege = new LinkedHashMap<>();
        for (Accommodation a : stays) {
            byCollege.merge(a.getNearbyCollege(), 1, Integer::sum);
        }

        int barY = chartY + 24;
        int maxCount = byCollege.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        for (Map.Entry<String, Integer> entry : byCollege.entrySet()) {
            g2.setFont(HavenTheme.FONT_BODY);
            g2.setColor(HavenTheme.TEXT_BODY);
            g2.drawString(entry.getKey(), 24, barY + 16);

            int barMaxW = Math.max(80, chartW - 150);
            int bw = (int) ((entry.getValue() / (double) maxCount) * barMaxW);
            g2.setColor(HavenTheme.PRIMARY);
            g2.fillRoundRect(130, barY, Math.max(18, bw), 22, 6, 6);

            g2.setColor(HavenTheme.TEXT_PRIMARY);
            g2.setFont(HavenTheme.FONT_CAPTION_BOLD);
            g2.drawString(entry.getValue() + " stays", 138 + Math.max(18, bw), barY + 15);
            barY += 36;
        }

        // Right Chart: Enquiries by Lifecycle State (State Pattern)
        int rightX = 40 + chartW;
        g2.setColor(HavenTheme.TEXT_PRIMARY);
        g2.setFont(HavenTheme.FONT_SUBTITLE);
        g2.drawString("Enquiries by Lifecycle State (State Pattern)", rightX, chartY);

        String[] states = {"PENDING", "CONTACTED", "CONFIRMED", "DECLINED", "CANCELLED"};
        Color[] stateColors = {HavenTheme.WARNING, HavenTheme.INFO, HavenTheme.SUCCESS, HavenTheme.DANGER, HavenTheme.TEXT_MUTED};
        int stateY = chartY + 24;
        int maxState = Math.max(1, enquiries.size());

        for (int i = 0; i < states.length; i++) {
            final String st = states[i];
            long cnt = enquiries.stream().filter(e -> st.equalsIgnoreCase(e.getStatus())).count();
            g2.setFont(HavenTheme.FONT_BODY_BOLD);
            g2.setColor(HavenTheme.TEXT_BODY);
            g2.drawString(st, rightX, stateY + 16);

            int barMaxW = Math.max(80, chartW - 160);
            int bw = (int) ((cnt / (double) maxState) * barMaxW);
            g2.setColor(stateColors[i]);
            g2.fillRoundRect(rightX + 110, stateY, Math.max(14, bw), 22, 6, 6);

            g2.setColor(HavenTheme.TEXT_PRIMARY);
            g2.setFont(HavenTheme.FONT_CAPTION_BOLD);
            g2.drawString(cnt + " enquiries", rightX + 118 + Math.max(14, bw), stateY + 15);
            stateY += 36;
        }
    }

    // =========================================================================
    // TAB 5: SEED & DATA STORE MANAGEMENT
    // =========================================================================
    private JPanel createSeedDataTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(HavenTheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(HavenTheme.SURFACE_WHITE);
        card.setBorder(HavenTheme.createCardBorder());

        JLabel heading = new JLabel("Dual-Engine Persistence & Seed Data Governance");
        heading.setFont(HavenTheme.FONT_TITLE);
        heading.setForeground(HavenTheme.TEXT_PRIMARY);
        card.add(heading);
        card.add(Box.createVerticalStrut(8));

        dbStatusSummaryLabel = new JLabel(buildStoreMetricsSummary());
        dbStatusSummaryLabel.setFont(HavenTheme.FONT_BODY);
        dbStatusSummaryLabel.setForeground(HavenTheme.TEXT_BODY);
        card.add(dbStatusSummaryLabel);
        card.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton syncJsonBtn = ModernButton.primary("Force Save Offline JSON Snapshot");
        syncJsonBtn.addActionListener(e -> {
            InMemoryStore.getInstance().persist();
            java.io.File storeFile = new java.io.File("data/haven_store.json");
            JOptionPane.showMessageDialog(this,
                    "Offline JSON snapshot written to data/haven_store.json (" + storeFile.length() + " bytes).",
                    "Snapshot Saved",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshAllTabs();
        });

        JButton resetSeedBtn = ModernButton.danger("Reset Demo Store to Default Seed Data");
        resetSeedBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset the in-memory & file-backed store to the default 16 Trivandrum accommodations and demo accounts?",
                    "Confirm Seed Reset",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                InMemoryStore.getInstance().resetToDefaultSeed();
                refreshAllTabs();
                if (onDataChanged != null) onDataChanged.run();
                JOptionPane.showMessageDialog(this, "Default seed data restored successfully.", "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRow.add(syncJsonBtn);
        btnRow.add(resetSeedBtn);
        card.add(btnRow);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private String buildStoreMetricsSummary() {
        String engine = RepositoryProvider.isOffline()
                ? "InMemoryRepositoryFactory (File-Backed JSON: data/haven_store.json)"
                : "MongoRepositoryFactory (MongoDB Server @ localhost:27017)";
        return String.format("<html><b>Active Factory:</b> %s<br/><br/>"
                        + "<b>Entity Counts:</b> %d Accommodations • %d Users • %d Enquiries • %d Reviews • %d Images • %d Messages • %d Notifications</html>",
                engine,
                accRepo.count(),
                userRepo.count(),
                enquiryRepo.count(),
                reviewRepo.count(),
                RepositoryProvider.getImageRepository().count(),
                RepositoryProvider.getMessageRepository().count(),
                RepositoryProvider.getNotificationRepository().count());
    }
}
