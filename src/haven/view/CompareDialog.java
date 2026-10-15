package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.ReviewDAO;
import haven.db.UserDAO;
import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.PG;
import haven.model.User;
import haven.pattern.GoogleMapsAdapter;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * CompareDialog - Side-by-side accommodation comparison modal.
 * Styled cleanly with HavenTheme design tokens and non-emoji badges.
 */
public class CompareDialog extends JDialog {
    private final List<Accommodation> accommodations;
    private final HostelDAO hostelDAO;
    private final UserDAO userDAO;
    private final EnquiryDAO enquiryDAO;
    private final ReviewDAO reviewDAO;
    private final User currentUser;
    private final Runnable onStateChanged;

    public CompareDialog(Window parent, List<Accommodation> accommodations, HostelDAO hostelDAO,
                         UserDAO userDAO, EnquiryDAO enquiryDAO, ReviewDAO reviewDAO,
                         User currentUser, Runnable onStateChanged) {
        super(parent, "HAVEN - Side-by-Side Property Comparison", ModalityType.APPLICATION_MODAL);
        this.accommodations = accommodations;
        this.hostelDAO = hostelDAO;
        this.userDAO = userDAO;
        this.enquiryDAO = enquiryDAO;
        this.reviewDAO = reviewDAO;
        this.currentUser = currentUser;
        this.onStateChanged = onStateChanged;

        int width = Math.max(780, accommodations.size() * 320);
        setSize(width, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HavenTheme.BG_CANVAS);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel titleLabel = new JLabel("Side-by-Side Accommodation Comparison");
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Comparing " + accommodations.size() + " selected stays across pricing, distance, policies, and amenities.");
        subtitleLabel.setFont(HavenTheme.FONT_CAPTION);
        subtitleLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subtitleLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JButton closeBtn = ModernButton.subtle("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setOpaque(false);
        closePanel.add(closeBtn);
        headerPanel.add(closePanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Columns Grid
        JPanel columnsPanel = new JPanel(new GridLayout(1, accommodations.size(), 14, 0));
        columnsPanel.setBackground(HavenTheme.BG_MUTED);
        columnsPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        for (Accommodation acc : accommodations) {
            columnsPanel.add(createPropertyColumn(acc));
        }

        JScrollPane scrollPane = new JScrollPane(columnsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createPropertyColumn(Accommodation acc) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBackground(HavenTheme.SURFACE_WHITE);
        column.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // 1. Property Name & Type
        boolean isHostel = (acc instanceof Hostel);
        JLabel typeBadge = HavenTheme.createBadge(
                isHostel ? "HOSTEL" : "PG / STAY",
                isHostel ? HavenTheme.PRIMARY : new Color(13, 148, 136),
                isHostel ? HavenTheme.PRIMARY_LIGHT : new Color(240, 253, 250),
                isHostel ? HavenTheme.PRIMARY_BORDER : new Color(153, 246, 228)
        );

        JLabel nameLabel = new JLabel("<html><body style='width: 200px;'><b>" + acc.getName() + "</b></body></html>");
        nameLabel.setFont(HavenTheme.FONT_TITLE);
        nameLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        JLabel collegeLabel = new JLabel("Near " + acc.getNearbyCollege());
        collegeLabel.setFont(HavenTheme.FONT_CAPTION);
        collegeLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        double avgRating = (reviewDAO != null) ? reviewDAO.getAverageRating(acc.getId()) : 0.0;
        int reviewCount = (reviewDAO != null) ? reviewDAO.getReviewCount(acc.getId()) : 0;
        String starText = (reviewCount > 0) ? String.format("★ %.1f / 5.0 (%d reviews)", avgRating, reviewCount) : "★ No reviews yet";
        JLabel ratingLabel = new JLabel(starText);
        ratingLabel.setFont(HavenTheme.FONT_CAPTION_BOLD);
        ratingLabel.setForeground(HavenTheme.WARNING);

        column.add(typeBadge);
        column.add(Box.createVerticalStrut(6));
        column.add(nameLabel);
        column.add(Box.createVerticalStrut(2));
        column.add(collegeLabel);
        column.add(Box.createVerticalStrut(4));
        column.add(ratingLabel);
        column.add(Box.createVerticalStrut(10));
        column.add(new JSeparator());
        column.add(Box.createVerticalStrut(10));

        // 2. Financial Metrics (Polymorphic Total Cost)
        double totalCost = acc.calculateTotalMonthlyCost(1);
        JLabel totalLabel = new JLabel(String.format("₹ %,.0f / mo", totalCost));
        totalLabel.setFont(HavenTheme.FONT_TITLE);
        totalLabel.setForeground(HavenTheme.PRIMARY);

        JLabel totalDesc = new JLabel("Total Estimated Monthly Cost");
        totalDesc.setFont(HavenTheme.FONT_CAPTION);
        totalDesc.setForeground(HavenTheme.TEXT_SECONDARY);

        column.add(totalLabel);
        column.add(totalDesc);
        column.add(Box.createVerticalStrut(8));

        // Details Table / Key-Value rows
        addComparisonRow(column, "Base Rent", String.format("₹ %,.0f / mo", acc.getBaseRent()));
        if (acc instanceof Hostel) {
            Hostel h = (Hostel) acc;
            addComparisonRow(column, "Mess Fee", String.format("₹ %,.0f / mo (Fixed)", h.getMessFee()));
            addComparisonRow(column, "Curfew Time", h.getCurfewTime());
        } else if (acc instanceof PG) {
            PG pg = (PG) acc;
            addComparisonRow(column, "Utilities Fee", String.format("₹ %,.0f / mo", pg.getUtilityCharges()));
            addComparisonRow(column, "Food Included", pg.isFoodIncluded() ? "Yes (Included)" : "No (Self)");
            addComparisonRow(column, "Curfew Time", "Flexible / None");
        }

        addComparisonRow(column, "Distance", String.format("%.1f km from campus", acc.getDistanceKm()));
        addComparisonRow(column, "Allowed Gender", acc.getGenderAllowed());
        addComparisonRow(column, "Vacancies Left", acc.getVacancies() + " room" + (acc.getVacancies() == 1 ? "" : "s"));

        column.add(Box.createVerticalStrut(8));
        column.add(new JSeparator());
        column.add(Box.createVerticalStrut(8));

        // 3. Amenities Check (Clean text indicators, no emojis)
        JLabel amenitiesHeader = new JLabel("Amenities Included:");
        amenitiesHeader.setFont(HavenTheme.FONT_BODY_BOLD);
        amenitiesHeader.setForeground(HavenTheme.TEXT_PRIMARY);
        column.add(amenitiesHeader);
        column.add(Box.createVerticalStrut(4));

        String[] standardAmenities = {"WiFi", "Food / Mess", "AC", "Attached Washroom", "CCTV Security", "Power Backup"};
        for (String am : standardAmenities) {
            boolean hasIt = acc.getAmenities().stream().anyMatch(a -> a.toLowerCase().contains(am.toLowerCase().split("/")[0].trim().toLowerCase()));
            String icon = hasIt ? "✓ " : "- ";
            JLabel amLabel = new JLabel(icon + am);
            amLabel.setFont(hasIt ? HavenTheme.FONT_CAPTION_BOLD : HavenTheme.FONT_CAPTION);
            amLabel.setForeground(hasIt ? HavenTheme.SUCCESS : HavenTheme.TEXT_MUTED);
            column.add(amLabel);
            column.add(Box.createVerticalStrut(2));
        }

        column.add(Box.createVerticalGlue());
        column.add(Box.createVerticalStrut(10));
        column.add(new JSeparator());
        column.add(Box.createVerticalStrut(10));

        // 4. Action Buttons
        JButton mapBtn = ModernButton.subtle("View on Map");
        mapBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        mapBtn.addActionListener(e -> {
            GoogleMapsAdapter adapter = new GoogleMapsAdapter();
            adapter.openLocation(acc.getLocationLink());
        });

        JButton bookBtn = ModernButton.primary(acc.getVacancies() > 0 ? "Book / Enquire" : "Exhausted");
        bookBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        bookBtn.setEnabled(acc.getVacancies() > 0);
        bookBtn.addActionListener(e -> {
            dispose();
            ListingCardPanel dummy = new ListingCardPanel(acc, hostelDAO, userDAO, enquiryDAO, currentUser, onStateChanged);
            SwingUtilities.invokeLater(() -> dummy.openEnquiryDialog());
        });

        column.add(mapBtn);
        column.add(Box.createVerticalStrut(4));
        column.add(bookBtn);

        return column;
    }

    private void addComparisonRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 2));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(HavenTheme.FONT_CAPTION);
        lbl.setForeground(HavenTheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(HavenTheme.FONT_CAPTION_BOLD);
        val.setForeground(HavenTheme.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        parent.add(row);
        parent.add(Box.createVerticalStrut(2));
    }
}
