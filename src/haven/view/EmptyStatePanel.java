package haven.view;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Reusable modern empty-state card for lists with zero results.
 * Styled with HavenTheme design tokens without emojis.
 */
public class EmptyStatePanel extends JPanel {

    public EmptyStatePanel(String tag, String title, String subtitle, String actionText, Runnable onAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(HavenTheme.SURFACE_WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(36, 24, 36, 24)
        ));

        if (tag != null && !tag.isEmpty()) {
            JLabel tagLabel = HavenTheme.createBadge(tag, HavenTheme.TEXT_MUTED, HavenTheme.BG_MUTED, HavenTheme.BORDER_LIGHT);
            tagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(tagLabel);
            add(Box.createVerticalStrut(12));
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("<html><center style='color:#64748B; font-size:12px;'>" + subtitle + "</center></html>");
        subtitleLabel.setFont(HavenTheme.FONT_BODY);
        subtitleLabel.setForeground(HavenTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(titleLabel);
        add(Box.createVerticalStrut(6));
        add(subtitleLabel);

        if (actionText != null && onAction != null) {
            add(Box.createVerticalStrut(16));
            JButton btn = ModernButton.secondary(actionText);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> onAction.run());
            add(btn);
        }
    }

    public static EmptyStatePanel forSearch(Runnable onResetFilters) {
        return new EmptyStatePanel("NO RESULTS", "No Stays Match Your Filters",
                "Try raising the rent threshold, clearing amenity filters, or selecting 'All Colleges'.",
                "Clear All Filters", onResetFilters);
    }

    public static EmptyStatePanel forWishlist(Runnable onViewAll) {
        return new EmptyStatePanel("WISHLIST EMPTY", "Your Saved Stays List is Empty",
                "Explore available hostels and PGs and click 'Save' to bookmark your favorites.",
                "Browse All Stays", onViewAll);
    }

    public static EmptyStatePanel forEnquiries(Runnable onBrowse) {
        return new EmptyStatePanel("NO ENQUIRIES", "No Enquiry Requests Sent Yet",
                "When you find a stay you like, click 'Enquire & Book' to reserve a vacancy and reach the owner.",
                "Discover Stays", onBrowse);
    }
}
