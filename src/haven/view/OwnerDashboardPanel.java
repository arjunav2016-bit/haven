package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.model.Hostel;
import haven.model.PG;
import haven.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OwnerDashboardPanel - Property Owner Portal.
 * Features:
 * - Enterprise Data Tables for listings and enquiries styled via HavenTheme
 * - Clean tab navigation without emojis
 * - Quick vacancy updates, editing, deletion, and enquiry management
 */
public class OwnerDashboardPanel extends JPanel {
    private final User currentUser;
    private final HostelDAO dao;
    private final EnquiryDAO enquiryDAO;
    private final Runnable onDataChanged;

    private DefaultTableModel tableModel;
    private JTable listingsTable;
    private List<Accommodation> currentListings;

    private DefaultTableModel enquiryTableModel;
    private JTable enquiryTable;
    private List<Enquiry> currentEnquiries;
    private JScrollPane enquiryScrollPane;
    private JComboBox<String> enquiryStatusFilter;
    private JLabel enquiryCountLabel;

    private JTextField nameField, collegeField, rentField, distanceField, vacancyField, locationLinkField;
    private JTextField addressField, contactPhoneField;
    private JComboBox<String> typeBox, genderBox;
    private JTextField messFeeField, curfewField, utilityField;
    private JCheckBox foodCheckBox;
    private JCheckBox regWifiCheck, regFoodCheck, regBathCheck, regCctvCheck, regPowerCheck;

    private AnalyticsPanel analyticsPanel;

    public OwnerDashboardPanel(User currentUser, HostelDAO dao, EnquiryDAO enquiryDAO, Runnable onDataChanged) {
        this.currentUser = currentUser;
        this.dao = dao;
        this.enquiryDAO = enquiryDAO;
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout());
        setBackground(HavenTheme.BG_CANVAS);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HavenTheme.FONT_SUBTITLE);
        analyticsPanel = new AnalyticsPanel(currentUser, dao, enquiryDAO);

        tabbedPane.addTab("Manage Listings & Vacancies", createManageListingsPanel());
        tabbedPane.addTab("Register New Property", createRegisterPropertyPanel());
        tabbedPane.addTab("Student Enquiries", createEnquiriesPanel());
        tabbedPane.addTab("Analytics & Revenue", analyticsPanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) {
                refreshTable();
            } else if (idx == 2) {
                refreshEnquiriesTable();
            } else if (idx == 3 && analyticsPanel != null) {
                analyticsPanel.refreshData();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    public OwnerDashboardPanel(HostelDAO dao, EnquiryDAO enquiryDAO, Runnable onDataChanged) {
        this(null, dao, enquiryDAO, onDataChanged);
    }

    private JPanel createManageListingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(HavenTheme.BG_CANVAS);

        String[] columns = {"ID", "Name", "Type", "College", "Base Rent (₹)", "Gender", "Vacancies"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        listingsTable = new JTable(tableModel);
        HavenTheme.styleTable(listingsTable);

        listingsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        listingsTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        listingsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        listingsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        listingsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        listingsTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        listingsTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(listingsTable);
        scrollPane.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);

        JButton refreshBtn = ModernButton.subtle("Refresh");
        JButton photosBtn = ModernButton.secondary("Manage Photos");
        JButton updateVacancyBtn = ModernButton.secondary("Quick Vacancy");
        JButton editBtn = ModernButton.secondary("Edit Property");
        JButton deleteBtn = ModernButton.danger("Delete Property");

        photosBtn.setToolTipText("View or upload property photographs (PNG/JPG) for the selected listing");
        photosBtn.addActionListener(e -> {
            int selectedRow = listingsTable.getSelectedRow();
            if (selectedRow == -1 || currentListings == null || selectedRow >= currentListings.size()) {
                JOptionPane.showMessageDialog(this, "Select a property to manage its photos.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Accommodation selectedStay = currentListings.get(selectedRow);
            new ImageGalleryDialog(SwingUtilities.getWindowAncestor(this), selectedStay, currentUser, () -> {
                refreshTable();
                if (onDataChanged != null) onDataChanged.run();
            }).setVisible(true);
        });

        updateVacancyBtn.addActionListener(e -> {
            int selectedRow = listingsTable.getSelectedRow();
            if (selectedRow == -1 || currentListings == null || selectedRow >= currentListings.size()) {
                JOptionPane.showMessageDialog(this, "Select a property to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Accommodation selectedStay = currentListings.get(selectedRow);
            String input = JOptionPane.showInputDialog(this, "Enter new vacancy count for " + selectedStay.getName() + ":", selectedStay.getVacancies());
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int newVacancy = Integer.parseInt(input.trim());
                    if (newVacancy < 0) throw new NumberFormatException();
                    dao.updateVacancy(selectedStay.getId(), newVacancy);
                    refreshTable();
                    Window win = SwingUtilities.getWindowAncestor(this);
                    if (win instanceof JFrame) {
                        ToastNotification.show((JFrame) win, "Vacancy updated for " + selectedStay.getName(), ToastNotification.Type.SUCCESS);
                    }
                    if (onDataChanged != null) onDataChanged.run();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Enter a valid positive integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editBtn.addActionListener(e -> handleEditProperty());
        deleteBtn.addActionListener(e -> handleDeleteProperty());
        refreshBtn.addActionListener(e -> refreshTable());

        JButton chatBtn = ModernButton.secondary("Direct Chat");
        chatBtn.setToolTipText("Open direct message thread with prospective students for this property");
        chatBtn.addActionListener(e -> {
            int selectedRow = listingsTable.getSelectedRow();
            if (selectedRow == -1 || currentListings == null || selectedRow >= currentListings.size()) {
                JOptionPane.showMessageDialog(this, "Select a property from the table to view direct messages.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Accommodation selectedStay = currentListings.get(selectedRow);
            new ChatDialog(SwingUtilities.getWindowAncestor(this), currentUser, selectedStay).setVisible(true);
        });

        actionPanel.add(refreshBtn);
        actionPanel.add(chatBtn);
        actionPanel.add(photosBtn);
        actionPanel.add(updateVacancyBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        refreshTable();
        return panel;
    }

    private void handleEditProperty() {
        int selectedRow = listingsTable.getSelectedRow();
        if (selectedRow == -1 || currentListings == null || selectedRow >= currentListings.size()) {
            JOptionPane.showMessageDialog(this, "Select a property from the table to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Accommodation acc = currentListings.get(selectedRow);

        JTextField editNameField = new JTextField(acc.getName(), 18);
        JTextField editRentField = new JTextField(String.format("%.0f", acc.getBaseRent()), 10);
        JTextField editVacancyField = new JTextField(String.valueOf(acc.getVacancies()), 8);
        JTextField editAddressField = new JTextField(acc.getAddress() != null ? acc.getAddress() : "", 18);
        JTextField editPhoneField = new JTextField(acc.getContactPhone() != null ? acc.getContactPhone() : "", 12);
        JComboBox<String> editGenderBox = new JComboBox<>(new String[]{"Boys", "Girls", "Both"});
        editGenderBox.setSelectedItem(acc.getGenderAllowed());

        boolean isHostel = acc instanceof Hostel;
        JTextField editMessField = new JTextField(isHostel ? String.format("%.0f", ((Hostel) acc).getMessFee()) : "0", 8);
        JTextField editCurfewOrUtilField = new JTextField(isHostel ? ((Hostel) acc).getCurfewTime() : String.format("%.0f", ((PG) acc).getUtilityCharges()), 10);
        JCheckBox editFoodBox = new JCheckBox("Food Included", !isHostel && ((PG) acc).isFoodIncluded());

        JPanel editPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        editPanel.add(new JLabel("Property Name:"));
        editPanel.add(editNameField);
        editPanel.add(new JLabel("Base Rent (₹):"));
        editPanel.add(editRentField);
        editPanel.add(new JLabel("Vacancies:"));
        editPanel.add(editVacancyField);
        editPanel.add(new JLabel("Allowed Gender:"));
        editPanel.add(editGenderBox);
        editPanel.add(new JLabel("Full Address:"));
        editPanel.add(editAddressField);
        editPanel.add(new JLabel("Contact Phone:"));
        editPanel.add(editPhoneField);

        if (isHostel) {
            editPanel.add(new JLabel("Mess Fee (₹):"));
            editPanel.add(editMessField);
            editPanel.add(new JLabel("Curfew Time:"));
            editPanel.add(editCurfewOrUtilField);
        } else {
            editPanel.add(new JLabel("Food Option:"));
            editPanel.add(editFoodBox);
            editPanel.add(new JLabel("Utility Charges (₹):"));
            editPanel.add(editCurfewOrUtilField);
        }

        int result = JOptionPane.showConfirmDialog(this, editPanel, "Edit Property - " + acc.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String newName = editNameField.getText().trim();
                double newRent = Double.parseDouble(editRentField.getText().trim());
                int newVacancies = Integer.parseInt(editVacancyField.getText().trim());
                String newAddress = editAddressField.getText().trim();
                String newPhone = editPhoneField.getText().trim();
                String newGender = (String) editGenderBox.getSelectedItem();

                if (newName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Property name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newRent <= 0 || newVacancies < 0) {
                    JOptionPane.showMessageDialog(this, "Rent and vacancies must be valid positive values.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Accommodation updated;
                String locationLink = Accommodation.buildLocationSearchUrl(newName, newAddress, acc.getNearbyCollege());

                if (isHostel) {
                    double messFee = Double.parseDouble(editMessField.getText().trim());
                    String curfew = editCurfewOrUtilField.getText().trim();
                    updated = new Hostel(acc.getId(), acc.getOwnerId(), newName, acc.getNearbyCollege(),
                            newRent, acc.getDistanceKm(), newGender, newVacancies, locationLink,
                            acc.getLatitude(), acc.getLongitude(),
                            messFee, curfew, newAddress, newPhone);
                } else {
                    double utilities = Double.parseDouble(editCurfewOrUtilField.getText().trim());
                    boolean food = editFoodBox.isSelected();
                    updated = new PG(acc.getId(), acc.getOwnerId(), newName, acc.getNearbyCollege(),
                            newRent, acc.getDistanceKm(), newGender, newVacancies, locationLink,
                            acc.getLatitude(), acc.getLongitude(),
                            food, utilities, newAddress, newPhone);
                }

                updated.setAmenities(acc.getAmenities());
                dao.updateAccommodation(updated);
                JOptionPane.showMessageDialog(this, "Property updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                if (onDataChanged != null) onDataChanged.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteProperty() {
        int selectedRow = listingsTable.getSelectedRow();
        if (selectedRow == -1 || currentListings == null || selectedRow >= currentListings.size()) {
            JOptionPane.showMessageDialog(this, "Select a property from the table to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Accommodation selectedStay = currentListings.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete \"" + selectedStay.getName() + "\"?\n" +
                "This action cannot be undone.",
                "Delete Property Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dao.deleteAccommodation(selectedStay.getId());
            JOptionPane.showMessageDialog(this, "\"" + selectedStay.getName() + "\" has been removed.", "Property Deleted", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private JPanel createEnquiriesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(HavenTheme.BG_CANVAS);

        // Header toolbar with Status Filter & Count
        JPanel headerBar = new JPanel(new BorderLayout(10, 0));
        headerBar.setOpaque(false);
        headerBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        enquiryCountLabel = new JLabel("Manage student applicants and room booking enquiries.");
        enquiryCountLabel.setFont(HavenTheme.FONT_BODY);
        enquiryCountLabel.setForeground(HavenTheme.TEXT_SECONDARY);
        headerBar.add(enquiryCountLabel, BorderLayout.WEST);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBar.setOpaque(false);
        JLabel filterLbl = new JLabel("Status Filter:");
        filterLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        filterLbl.setForeground(HavenTheme.TEXT_PRIMARY);
        filterBar.add(filterLbl);

        enquiryStatusFilter = new JComboBox<>(new String[]{
                "Active Enquiries", "All Enquiries", "PENDING", "CONTACTED", "CONFIRMED", "DECLINED", "CANCELLED"
        });
        enquiryStatusFilter.setFont(HavenTheme.FONT_BODY);
        enquiryStatusFilter.addActionListener(e -> refreshEnquiriesTable());
        filterBar.add(enquiryStatusFilter);
        headerBar.add(filterBar, BorderLayout.EAST);
        panel.add(headerBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Date", "Property", "Student", "Phone", "Status", "Message"};
        enquiryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        enquiryTable = new JTable(enquiryTableModel);
        HavenTheme.styleTable(enquiryTable);
        enquiryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        enquiryTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        enquiryTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        enquiryTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        enquiryTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        enquiryTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        enquiryTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        enquiryTable.getColumnModel().getColumn(6).setPreferredWidth(210);

        // Status badge color renderer
        enquiryTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        enquiryScrollPane = new JScrollPane(enquiryTable);
        enquiryScrollPane.getViewport().setBackground(HavenTheme.SURFACE_WHITE);
        enquiryScrollPane.setBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1));
        panel.add(enquiryScrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnPanel.setOpaque(false);

        JButton refreshBtn = ModernButton.subtle("Refresh Enquiries");
        refreshBtn.addActionListener(e -> refreshEnquiriesTable());

        JButton contactBtn = ModernButton.secondary("Mark Contacted");
        contactBtn.setToolTipText("Mark selected enquiry as contacted");
        contactBtn.addActionListener(e -> {
            int row = enquiryTable.getSelectedRow();
            if (row == -1 || currentEnquiries == null || row >= currentEnquiries.size()) {
                JOptionPane.showMessageDialog(this, "Select an enquiry from the table to mark as contacted.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Enquiry eq = currentEnquiries.get(row);
            enquiryDAO.updateStatus(eq.getEnquiryId(), "CONTACTED");
            refreshEnquiriesTable();
            if (onDataChanged != null) onDataChanged.run();
        });

        JButton confirmBtn = ModernButton.primary("Confirm Booking");
        confirmBtn.setToolTipText("Confirm booking for this student");
        confirmBtn.addActionListener(e -> {
            int row = enquiryTable.getSelectedRow();
            if (row == -1 || currentEnquiries == null || row >= currentEnquiries.size()) {
                JOptionPane.showMessageDialog(this, "Select an enquiry from the table to confirm booking.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Enquiry eq = currentEnquiries.get(row);
            if ("CANCELLED".equalsIgnoreCase(eq.getStatus())) {
                JOptionPane.showMessageDialog(this, "Cannot confirm an enquiry that was cancelled by the student.", "Cancelled Enquiry", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("DECLINED".equalsIgnoreCase(eq.getStatus())) {
                JOptionPane.showMessageDialog(this, "Cannot confirm an enquiry that has already been declined.", "Declined Enquiry", JOptionPane.WARNING_MESSAGE);
                return;
            }
            enquiryDAO.updateStatus(eq.getEnquiryId(), "CONFIRMED");
            JOptionPane.showMessageDialog(this, "Booking confirmed for " + eq.getStudentUsername() + "!", "Confirmed", JOptionPane.INFORMATION_MESSAGE);
            refreshEnquiriesTable();
            if (onDataChanged != null) onDataChanged.run();
        });

        JButton declineBtn = ModernButton.danger("Decline & Restore Vacancy");
        declineBtn.setToolTipText("Decline enquiry and restore 1 room vacancy");
        declineBtn.addActionListener(e -> {
            int row = enquiryTable.getSelectedRow();
            if (row == -1 || currentEnquiries == null || row >= currentEnquiries.size()) {
                JOptionPane.showMessageDialog(this, "Select an enquiry from the table to decline.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Enquiry eq = currentEnquiries.get(row);
            if ("DECLINED".equalsIgnoreCase(eq.getStatus()) || "CANCELLED".equalsIgnoreCase(eq.getStatus())) {
                JOptionPane.showMessageDialog(this, "This enquiry has already been " + eq.getStatus().toLowerCase() + ".", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to decline this enquiry from " + eq.getStudentUsername() + "?\n" +
                    "This will restore 1 vacancy for " + eq.getAccommodationName() + ".",
                    "Decline Enquiry & Restore Room",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                enquiryDAO.updateStatus(eq.getEnquiryId(), "DECLINED");
                dao.incrementVacancy(eq.getAccommodationId());
                JOptionPane.showMessageDialog(this, "Enquiry marked as DECLINED and 1 vacancy restored.", "Vacancy Restored", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                refreshEnquiriesTable();
                if (onDataChanged != null) onDataChanged.run();
            }
        });

        JButton chatStudentBtn = ModernButton.secondary("Message Student");
        chatStudentBtn.setToolTipText("Open direct chat thread with the selected student applicant");
        chatStudentBtn.addActionListener(e -> {
            int row = enquiryTable.getSelectedRow();
            if (row == -1 || currentEnquiries == null || row >= currentEnquiries.size()) {
                JOptionPane.showMessageDialog(this, "Select an enquiry from the table to message the student.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Enquiry eq = currentEnquiries.get(row);
            new ChatDialog(SwingUtilities.getWindowAncestor(this), currentUser, eq).setVisible(true);
        });

        btnPanel.add(refreshBtn);
        btnPanel.add(chatStudentBtn);
        btnPanel.add(contactBtn);
        btnPanel.add(confirmBtn);
        btnPanel.add(declineBtn);

        JButton exportRosterBtn = ModernButton.secondary("Export Applicants Roster");
        exportRosterBtn.setToolTipText("Export a text roster report of all student applicants");
        exportRosterBtn.addActionListener(e -> {
            if (currentEnquiries == null || currentEnquiries.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No student enquiries to export.", "Empty Roster", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String ownerName = (currentUser != null) ? currentUser.getUsername() : "owner";
            String roster = haven.pattern.ReceiptGenerator.generateOwnerEnquiryRoster(ownerName, currentEnquiries);
            String filename = "HAVEN_Roster_" + ownerName + ".txt";
            haven.pattern.ReceiptGenerator.exportToFileWithDialog(this, filename, roster, "Export Applicants Roster");
        });
        btnPanel.add(exportRosterBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        refreshEnquiriesTable();
        return panel;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        String ownerUsername = (currentUser != null && currentUser.isOwner()) ? currentUser.getUsername() : "owner";
        currentListings = dao.getAccommodationsByOwner(ownerUsername, currentUser != null ? currentUser.getRoleEnum() : null);
        for (Accommodation a : currentListings) {
            String type = (a instanceof Hostel) ? "Hostel" : "PG";
            tableModel.addRow(new Object[]{
                    a.getId(), a.getName(), type, a.getNearbyCollege(),
                    String.format("%.0f", a.getBaseRent()), a.getGenderAllowed(), a.getVacancies()
            });
        }
    }

    public void refreshEnquiriesTable() {
        enquiryTableModel.setRowCount(0);
        String ownerUsername = (currentUser != null && currentUser.isOwner()) ? currentUser.getUsername() : "owner";
        List<Enquiry> all = enquiryDAO.getEnquiriesForOwner(ownerUsername, currentUser != null ? currentUser.getRoleEnum() : null);

        String filter = (enquiryStatusFilter != null && enquiryStatusFilter.getSelectedItem() != null)
                ? (String) enquiryStatusFilter.getSelectedItem()
                : "Active Enquiries";

        if ("Active Enquiries".equalsIgnoreCase(filter)) {
            currentEnquiries = all.stream()
                    .filter(eq -> !"DECLINED".equalsIgnoreCase(eq.getStatus()) && !"CANCELLED".equalsIgnoreCase(eq.getStatus()))
                    .collect(Collectors.toList());
        } else if ("All Enquiries".equalsIgnoreCase(filter)) {
            currentEnquiries = all;
        } else {
            currentEnquiries = all.stream()
                    .filter(eq -> filter.equalsIgnoreCase(eq.getStatus()))
                    .collect(Collectors.toList());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm");
        for (Enquiry eq : currentEnquiries) {
            enquiryTableModel.addRow(new Object[]{
                    eq.getEnquiryId(),
                    sdf.format(eq.getCreatedAt()),
                    eq.getAccommodationName(),
                    eq.getStudentUsername(),
                    eq.getStudentPhone(),
                    eq.getStatus(),
                    eq.getMessage()
            });
        }

        int count = currentEnquiries.size();
        if (enquiryCountLabel != null) {
            enquiryCountLabel.setText("Showing " + count + " enquiry record" + (count == 1 ? "" : "s") + " (" + filter + ").");
        }

        if (enquiryScrollPane != null) {
            if (count == 0) {
                enquiryScrollPane.setColumnHeaderView(null);
                enquiryScrollPane.setViewportView(EmptyStatePanel.forOwnerEnquiries(this::refreshEnquiriesTable));
            } else {
                enquiryScrollPane.setColumnHeaderView(enquiryTable.getTableHeader());
                enquiryScrollPane.setViewportView(enquiryTable);
            }
            enquiryScrollPane.revalidate();
            enquiryScrollPane.repaint();
            if (enquiryTable != null) {
                enquiryTable.revalidate();
                enquiryTable.repaint();
            }
        }
    }

    private JPanel createRegisterPropertyPanel() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(HavenTheme.BG_CANVAS);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        formPanel.setBackground(HavenTheme.SURFACE_WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(15);
        nameField.setBorder(HavenTheme.createInputPadding());
        collegeField = new JTextField("SCTCE", 15);
        collegeField.setBorder(HavenTheme.createInputPadding());
        rentField = new JTextField(10);
        rentField.setBorder(HavenTheme.createInputPadding());
        distanceField = new JTextField("0.8", 10);
        distanceField.setBorder(HavenTheme.createInputPadding());
        vacancyField = new JTextField(10);
        vacancyField.setBorder(HavenTheme.createInputPadding());
        addressField = new JTextField(20);
        addressField.setBorder(HavenTheme.createInputPadding());
        contactPhoneField = new JTextField(15);
        contactPhoneField.setBorder(HavenTheme.createInputPadding());
        locationLinkField = new JTextField("https://maps.google.com/?q=Pappanamcode+Trivandrum", 20);
        locationLinkField.setBorder(HavenTheme.createInputPadding());

        ValidationUtils.attachValidator(nameField, ValidationUtils::isNonEmpty, "Property name is required", null);
        ValidationUtils.attachValidator(rentField, ValidationUtils::isPositiveDouble, "Rent must be > 0", null);
        ValidationUtils.attachValidator(vacancyField, ValidationUtils::isNonNegativeInt, "Vacancies must be >= 0", null);

        typeBox = new JComboBox<>(new String[]{"Hostel", "PG"});
        genderBox = new JComboBox<>(new String[]{"Boys", "Girls", "Both"});

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Property Name:", nameField);
        addFormRow(formPanel, gbc, row++, "Property Type:", typeBox);
        addFormRow(formPanel, gbc, row++, "Nearby College:", collegeField);
        addFormRow(formPanel, gbc, row++, "Base Rent (₹/month):", rentField);
        addFormRow(formPanel, gbc, row++, "Distance from College (km):", distanceField);
        addFormRow(formPanel, gbc, row++, "Allowed Gender:", genderBox);
        addFormRow(formPanel, gbc, row++, "Initial Vacancies:", vacancyField);
        addFormRow(formPanel, gbc, row++, "Full Address:", addressField);
        addFormRow(formPanel, gbc, row++, "Contact Phone:", contactPhoneField);
        addFormRow(formPanel, gbc, row++, "Location Link (Google Maps):", locationLinkField);

        JPanel amenitiesCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        amenitiesCheckPanel.setOpaque(false);
        regWifiCheck = new JCheckBox("WiFi");
        regFoodCheck = new JCheckBox("Food / Mess");
        regBathCheck = new JCheckBox("Attached Washroom");
        regCctvCheck = new JCheckBox("CCTV Security");
        regPowerCheck = new JCheckBox("Power Backup");
        amenitiesCheckPanel.add(regWifiCheck);
        amenitiesCheckPanel.add(regFoodCheck);
        amenitiesCheckPanel.add(regBathCheck);
        amenitiesCheckPanel.add(regCctvCheck);
        amenitiesCheckPanel.add(regPowerCheck);
        addFormRow(formPanel, gbc, row++, "Amenities Offered:", amenitiesCheckPanel);

        CardLayout cardLayout = new CardLayout();
        JPanel dynamicCardPanel = new JPanel(cardLayout);

        JPanel hostelPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        hostelPanel.setBorder(new TitledBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1), "Hostel Specifics"));
        messFeeField = new JTextField("2500", 10);
        messFeeField.setBorder(HavenTheme.createInputPadding());
        curfewField = new JTextField("9:00 PM", 10);
        curfewField.setBorder(HavenTheme.createInputPadding());
        hostelPanel.add(new JLabel("Mess Fee (₹):"));
        hostelPanel.add(messFeeField);
        hostelPanel.add(new JLabel("Curfew Time:"));
        hostelPanel.add(curfewField);

        JPanel pgPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        pgPanel.setBorder(new TitledBorder(new LineBorder(HavenTheme.BORDER_LIGHT, 1), "PG Specifics"));
        foodCheckBox = new JCheckBox("Food Included");
        utilityField = new JTextField("500", 10);
        utilityField.setBorder(HavenTheme.createInputPadding());
        pgPanel.add(new JLabel("Food Option:"));
        pgPanel.add(foodCheckBox);
        pgPanel.add(new JLabel("Utility Charges (₹):"));
        pgPanel.add(utilityField);

        dynamicCardPanel.add(hostelPanel, "Hostel");
        dynamicCardPanel.add(pgPanel, "PG");

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(dynamicCardPanel, gbc);

        typeBox.addActionListener(e -> cardLayout.show(dynamicCardPanel, (String) typeBox.getSelectedItem()));

        JButton publishBtn = ModernButton.primary("Publish Listing");
        publishBtn.addActionListener(e -> handleFormSubmission());

        gbc.gridy = row;
        formPanel.add(publishBtn, gbc);

        rootPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        return rootPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(HavenTheme.FONT_BODY);
        lbl.setForeground(HavenTheme.TEXT_BODY);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void handleFormSubmission() {
        try {
            String name = nameField.getText().trim();
            String college = collegeField.getText().trim();
            if (name.isEmpty() || college.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and college cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double rent;
            double dist;
            int vacancies;
            try {
                rent = Double.parseDouble(rentField.getText().trim());
                dist = Double.parseDouble(distanceField.getText().trim());
                vacancies = Integer.parseInt(vacancyField.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Rent, distance, and vacancies must be valid numeric values.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (rent <= 0) {
                JOptionPane.showMessageDialog(this, "Monthly rent must be strictly greater than zero.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                rentField.requestFocus();
                return;
            }
            if (dist < 0) {
                JOptionPane.showMessageDialog(this, "Distance cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                distanceField.requestFocus();
                return;
            }
            if (vacancies < 0) {
                JOptionPane.showMessageDialog(this, "Vacancies cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                vacancyField.requestFocus();
                return;
            }

            String locationLink = locationLinkField.getText().trim();
            if (locationLink.isEmpty()) {
                locationLink = "https://www.google.com/maps/search/?api=1&query=" + (name + " " + college).replace(" ", "+");
            }
            String gender = (String) genderBox.getSelectedItem();
            String type = (String) typeBox.getSelectedItem();
            String address = addressField.getText().trim();
            String contact = contactPhoneField.getText().trim();

            if (!contact.isEmpty() && !contact.matches("^[0-9+ ,-]{7,30}$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid contact phone number (7 to 30 digits/characters).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                contactPhoneField.requestFocus();
                return;
            }

            String newId = "ACC-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String ownerId = (currentUser != null && currentUser.isOwner()) ? currentUser.getUsername() : "owner";
            Accommodation newStay;

            if ("Hostel".equals(type)) {
                double messFee = 0.0;
                try {
                    messFee = Double.parseDouble(messFeeField.getText().trim());
                    if (messFee < 0) throw new NumberFormatException();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Mess fee must be a non-negative number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    messFeeField.requestFocus();
                    return;
                }
                newStay = new Hostel(newId, ownerId, name, college, rent, dist, gender, vacancies, locationLink,
                        messFee, curfewField.getText().trim(), address, contact);
            } else {
                double utility = 0.0;
                try {
                    utility = Double.parseDouble(utilityField.getText().trim());
                    if (utility < 0) throw new NumberFormatException();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Utility charges must be a non-negative number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    utilityField.requestFocus();
                    return;
                }
                newStay = new PG(newId, ownerId, name, college, rent, dist, gender, vacancies, locationLink,
                        foodCheckBox.isSelected(), utility, address, contact);
            }

            if (regWifiCheck != null && regWifiCheck.isSelected()) newStay.addAmenity("WiFi");
            if (regFoodCheck != null && regFoodCheck.isSelected()) newStay.addAmenity("Food / Mess");
            if (regBathCheck != null && regBathCheck.isSelected()) newStay.addAmenity("Attached Washroom");
            if (regCctvCheck != null && regCctvCheck.isSelected()) newStay.addAmenity("CCTV Security");
            if (regPowerCheck != null && regPowerCheck.isSelected()) newStay.addAmenity("Power Backup");

            dao.insertAccommodation(newStay);
            JOptionPane.showMessageDialog(this, "Listing added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText(""); rentField.setText(""); distanceField.setText(""); vacancyField.setText("");
            addressField.setText(""); contactPhoneField.setText(""); locationLinkField.setText("");
            if (regWifiCheck != null) regWifiCheck.setSelected(false);
            if (regFoodCheck != null) regFoodCheck.setSelected(false);
            if (regBathCheck != null) regBathCheck.setSelected(false);
            if (regCctvCheck != null) regCctvCheck.setSelected(false);
            if (regPowerCheck != null) regPowerCheck.setSelected(false);
            refreshTable();
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Validation or DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
