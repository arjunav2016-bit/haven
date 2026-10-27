package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.model.User;
import haven.pattern.GoogleMapsAdapter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StudentEnquiriesPanel - Student's tracker for sent enquiries, booking history, and message threads.
 * Features:
 * - Status filtering (All, CONFIRMED, PENDING, CONTACTED, DECLINED, CANCELLED)
 * - Date sorting (Newest First / Oldest First)
 * - Direct access to Enquiry Message Thread (ChatDialog)
 * - Booking slip download via Template Method Pattern (BookingReceiptReport)
 */
public class StudentEnquiriesPanel extends JPanel {
    private final User currentUser;
    private final EnquiryDAO enquiryDAO;
    private final HostelDAO hostelDAO;
    private final Runnable onStateChanged;

    private DefaultTableModel tableModel;
    private JTable enquiriesTable;
    private List<Enquiry> currentEnquiries = new ArrayList<>();
    private JLabel countLabel;
    private JScrollPane scrollPane;
    private JComboBox<String> statusFilterBox;
    private JComboBox<String> dateSortBox;

    public StudentEnquiriesPanel(User currentUser, EnquiryDAO enquiryDAO, HostelDAO hostelDAO) {
        this(currentUser, enquiryDAO, hostelDAO, null);
    }

    public StudentEnquiriesPanel(User currentUser, EnquiryDAO enquiryDAO, HostelDAO hostelDAO, Runnable onStateChanged) {
        this.currentUser = currentUser;
        this.enquiryDAO = enquiryDAO;
        this.hostelDAO = hostelDAO;
        this.onStateChanged = onStateChanged;

        setLayout(new BorderLayout());
        setBackground(HavenTheme.BG_CANVAS);

        // Header Panel with Status Filter & Date Sort
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HavenTheme.SURFACE_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("My Sent Enquiries & Booking History");
        title.setFont(HavenTheme.FONT_TITLE);
        title.setForeground(HavenTheme.TEXT_PRIMARY);

        countLabel = new JLabel("Track the status, message threads, and booking slips of your accommodation requests.");
        countLabel.setFont(HavenTheme.FONT_CAPTION);
        countLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(countLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);

        JLabel filterLbl = new JLabel("Status:");
        filterLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        filterLbl.setForeground(HavenTheme.TEXT_BODY);
        headerActions.add(filterLbl);

        statusFilterBox = new JComboBox<>(new String[]{"All Statuses", "CONFIRMED", "PENDING", "CONTACTED", "DECLINED", "CANCELLED"});
        statusFilterBox.setFont(HavenTheme.FONT_BODY);
        statusFilterBox.addActionListener(e -> refreshEnquiries());
        headerActions.add(statusFilterBox);

        JLabel sortLbl = new JLabel("Sort:");
        sortLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        sortLbl.setForeground(HavenTheme.TEXT_BODY);
        headerActions.add(sortLbl);

        dateSortBox = new JComboBox<>(new String[]{"Newest First", "Oldest First"});
        dateSortBox.setFont(HavenTheme.FONT_BODY);
        dateSortBox.addActionListener(e -> refreshEnquiries());
        headerActions.add(dateSortBox);

        JButton refreshBtn = ModernButton.secondary("Refresh");
        refreshBtn.addActionListener(e -> refreshEnquiries());
        headerActions.add(refreshBtn);
        headerPanel.add(headerActions, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Panel with Table & Empty State
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        centerPanel.setBackground(HavenTheme.BG_CANVAS);

        String[] columns = {"Date", "Enquiry ID", "Accommodation", "Warden Contact", "My Phone", "Status", "Message / Warden Reply"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        enquiriesTable = new JTable(tableModel);
        HavenTheme.styleTable(enquiriesTable);
        enquiriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        enquiriesTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        enquiriesTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        enquiriesTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        enquiriesTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        enquiriesTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        enquiriesTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        enquiriesTable.getColumnModel().getColumn(6).setPreferredWidth(230);

        enquiriesTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        scrollPane = new JScrollPane(enquiriesTable);
        scrollPane.getViewport().setBackground(HavenTheme.SURFACE_WHITE);
        scrollPane.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Footer Action Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(HavenTheme.BG_CANVAS);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(8, 20, 12, 20)
        ));

        JButton withdrawBtn = ModernButton.danger("Withdraw Enquiry");
        withdrawBtn.setToolTipText("Withdraw your enquiry request and restore the vacancy");
        withdrawBtn.addActionListener(e -> handleWithdrawEnquiry());

        JButton chatThreadBtn = ModernButton.secondary("Message Thread");
        chatThreadBtn.setToolTipText("Open the direct chat thread with the property owner for this enquiry");
        chatThreadBtn.addActionListener(e -> handleOpenMessageThread());

        JButton mapBtn = ModernButton.subtle("View Property on Map");
        mapBtn.setToolTipText("Open the selected accommodation on Google Maps");
        mapBtn.addActionListener(e -> handleViewOnMap());

        JButton detailsBtn = ModernButton.secondary("View Details");
        detailsBtn.setToolTipText("View complete accommodation and enquiry information");
        detailsBtn.addActionListener(e -> handleViewDetails());

        JButton receiptBtn = ModernButton.primary("Download Booking Slip");
        receiptBtn.setToolTipText("Export an official confirmation slip for this enquiry / stay");
        receiptBtn.addActionListener(e -> handleDownloadSlip());

        footerPanel.add(withdrawBtn);
        footerPanel.add(chatThreadBtn);
        footerPanel.add(mapBtn);
        footerPanel.add(detailsBtn);
        footerPanel.add(receiptBtn);
        add(footerPanel, BorderLayout.SOUTH);

        refreshEnquiries();
    }

    private void handleOpenMessageThread() {
        int selectedRow = enquiriesTable.getSelectedRow();
        if (selectedRow == -1 || currentEnquiries == null || selectedRow >= currentEnquiries.size()) {
            JOptionPane.showMessageDialog(this, "Please select an enquiry from the table to open its message thread.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Enquiry eq = currentEnquiries.get(selectedRow);
        new ChatDialog(SwingUtilities.getWindowAncestor(this), currentUser, eq).setVisible(true);
    }

    private void handleDownloadSlip() {
        int selectedRow = enquiriesTable.getSelectedRow();
        if (selectedRow == -1 || currentEnquiries == null || selectedRow >= currentEnquiries.size()) {
            JOptionPane.showMessageDialog(this, "Please select an enquiry from the table to download its slip.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Enquiry eq = currentEnquiries.get(selectedRow);
        Accommodation acc = hostelDAO.getAccommodationById(eq.getAccommodationId());
        String slipContent = haven.pattern.ReceiptGenerator.generateEnquirySlipText(eq, acc, currentUser);
        String defaultName = "HAVEN_Slip_" + eq.getEnquiryId() + ".txt";

        haven.pattern.ReceiptGenerator.exportToFileWithDialog(this, defaultName, slipContent, "Export Student Booking Slip");
    }

    public void refreshEnquiries() {
        tableModel.setRowCount(0);
        String username = (currentUser != null && currentUser.getUsername() != null) ? currentUser.getUsername() : "student";
        List<Enquiry> all = enquiryDAO.getEnquiriesForStudent(username);

        String selectedStatus = statusFilterBox != null ? (String) statusFilterBox.getSelectedItem() : "All Statuses";
        if (selectedStatus != null && !selectedStatus.startsWith("All")) {
            all = all.stream()
                    .filter(e -> selectedStatus.equalsIgnoreCase(e.getStatus()))
                    .collect(Collectors.toList());
        }

        String sortOrder = dateSortBox != null ? (String) dateSortBox.getSelectedItem() : "Newest First";
        if ("Oldest First".equals(sortOrder)) {
            all.sort(Comparator.comparing(Enquiry::getCreatedAt));
        } else {
            all.sort(Comparator.comparing(Enquiry::getCreatedAt).reversed());
        }

        currentEnquiries = all;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");

        for (Enquiry eq : currentEnquiries) {
            String wardenContact = "-";
            Accommodation acc = hostelDAO.getAccommodationById(eq.getAccommodationId());
            if (acc != null && acc.getContactPhone() != null && !acc.getContactPhone().trim().isEmpty()) {
                wardenContact = acc.getContactPhone();
            }

            String summaryMsg = eq.getMessage();
            if (eq.getOwnerResponse() != null && !eq.getOwnerResponse().trim().isEmpty()) {
                summaryMsg += " [Warden: " + eq.getOwnerResponse() + "]";
            }

            tableModel.addRow(new Object[]{
                    sdf.format(eq.getCreatedAt()),
                    eq.getEnquiryId(),
                    eq.getAccommodationName(),
                    wardenContact,
                    eq.getStudentPhone(),
                    eq.getStatus(),
                    summaryMsg
            });
        }

        int count = currentEnquiries.size();
        countLabel.setText("Showing " + count + " enquiry / booking record" + (count == 1 ? "" : "s") + ".");

        if (count == 0) {
            scrollPane.setColumnHeaderView(null);
            scrollPane.setViewportView(EmptyStatePanel.forEnquiries(() -> {
                if (statusFilterBox != null && statusFilterBox.getSelectedIndex() != 0) {
                    statusFilterBox.setSelectedIndex(0);
                } else if (onStateChanged != null) {
                    onStateChanged.run();
                }
            }));
        } else {
            scrollPane.setColumnHeaderView(enquiriesTable.getTableHeader());
            scrollPane.setViewportView(enquiriesTable);
        }
        scrollPane.revalidate();
        scrollPane.repaint();
        if (enquiriesTable != null) {
            enquiriesTable.revalidate();
            enquiriesTable.repaint();
        }
    }

    private void handleViewOnMap() {
        int selectedRow = enquiriesTable.getSelectedRow();
        if (selectedRow == -1 || selectedRow >= currentEnquiries.size()) {
            JOptionPane.showMessageDialog(this, "Please select an enquiry from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Enquiry eq = currentEnquiries.get(selectedRow);
        Accommodation acc = hostelDAO.getAccommodationById(eq.getAccommodationId());
        GoogleMapsAdapter adapter = new GoogleMapsAdapter();

        if (acc != null && acc.getLocationLink() != null && !acc.getLocationLink().trim().isEmpty()) {
            adapter.openLocation(acc.getLocationLink());
        } else {
            adapter.openLocation("https://www.google.com/maps/search/?api=1&query=" + eq.getAccommodationName().replace(" ", "+"));
        }
    }

    private void handleViewDetails() {
        int selectedRow = enquiriesTable.getSelectedRow();
        if (selectedRow == -1 || selectedRow >= currentEnquiries.size()) {
            JOptionPane.showMessageDialog(this, "Please select an enquiry from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Enquiry eq = currentEnquiries.get(selectedRow);
        Accommodation acc = hostelDAO.getAccommodationById(eq.getAccommodationId());

        StringBuilder sb = new StringBuilder();
        sb.append("Enquiry ID: ").append(eq.getEnquiryId()).append("\n");
        sb.append("Date Sent: ").append(new SimpleDateFormat("dd MMM yyyy, HH:mm").format(eq.getCreatedAt())).append("\n");
        sb.append("Status: [").append(eq.getStatus()).append("]\n");
        sb.append("State Description: ").append(eq.getState().getDescription()).append("\n\n");
        sb.append("Accommodation: ").append(eq.getAccommodationName()).append("\n");

        if (acc != null) {
            sb.append("College: ").append(acc.getNearbyCollege()).append("\n");
            sb.append("Address: ").append(acc.getAddress().isEmpty() ? "N/A" : acc.getAddress()).append("\n");
            sb.append("Warden / Contact: ").append(acc.getContactPhone().isEmpty() ? "N/A" : acc.getContactPhone()).append("\n");
            sb.append("Base Rent: ₹").append(String.format("%.0f", acc.getBaseRent())).append(" / month\n");
            sb.append("Remaining Vacancies: ").append(acc.getVacancies()).append("\n\n");
        }

        sb.append("Student Contact Phone: ").append(eq.getStudentPhone()).append("\n");
        sb.append("Message Sent:\n\"").append(eq.getMessage()).append("\"\n");
        if (eq.getOwnerResponse() != null && !eq.getOwnerResponse().isEmpty()) {
            sb.append("\nWarden Response:\n\"").append(eq.getOwnerResponse()).append("\"\n");
        }

        JTextArea area = new JTextArea(sb.toString(), 15, 40);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Enquiry Details - " + eq.getEnquiryId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleWithdrawEnquiry() {
        int selectedRow = enquiriesTable.getSelectedRow();
        if (selectedRow == -1 || currentEnquiries == null || selectedRow >= currentEnquiries.size()) {
            JOptionPane.showMessageDialog(this, "Please select an enquiry from the table to withdraw.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Enquiry eq = currentEnquiries.get(selectedRow);
        String status = eq.getStatus().toUpperCase();

        if ("CANCELLED".equals(status) || "DECLINED".equals(status)) {
            int delChoice = JOptionPane.showConfirmDialog(this,
                    "This enquiry has already been " + status + ".\nWould you like to remove this record from your enquiry history?",
                    "Remove From History",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (delChoice == JOptionPane.YES_OPTION) {
                enquiryDAO.deleteEnquiry(eq.getEnquiryId());
                JOptionPane.showMessageDialog(this, "Enquiry record removed from history.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                refreshEnquiries();
            }
            return;
        }

        String confirmMsg;
        if ("CONFIRMED".equals(status)) {
            confirmMsg = "Your booking for \"" + eq.getAccommodationName() + "\" has already been CONFIRMED by the owner.\n" +
                    "Are you sure you want to cancel your confirmed booking?\n" +
                    "This will cancel your reservation and restore 1 room vacancy.";
        } else {
            confirmMsg = "Are you sure you want to withdraw your enquiry for \"" + eq.getAccommodationName() + "\"?\n" +
                    "This will cancel your application and restore 1 room vacancy.";
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                confirmMsg,
                "Confirm Enquiry Withdrawal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = enquiryDAO.updateStatus(eq.getEnquiryId(), "CANCELLED");
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Cannot withdraw enquiry from its current status (" + eq.getStatus() + ").",
                        "Invalid Transition",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Enquiry withdrawn successfully! The room vacancy has been restored.",
                    "Enquiry Withdrawn",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshEnquiries();
            if (onStateChanged != null) {
                onStateChanged.run();
            }
        }
    }
}
