package haven.view;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Reusable modern empty-state card for lists with zero results.
 * Perfectly centered horizontally and vertically in viewports and scrollpanes.
 * Styled with HavenTheme design tokens without emojis.
 */
public class EmptyStatePanel extends JPanel implements Scrollable {

    public EmptyStatePanel(String tag, String title, String subtitle, String actionText, Runnable onAction) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(null);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(HavenTheme.SURFACE_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(32, 48, 32, 48)
        ));

        if (tag != null && !tag.isEmpty()) {
            JLabel tagLabel = HavenTheme.createBadge(tag, HavenTheme.TEXT_MUTED, HavenTheme.BG_MUTED, HavenTheme.BORDER_LIGHT);
            tagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(tagLabel);
            card.add(Box.createVerticalStrut(12));
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));

        JLabel subtitleLabel = new JLabel("<html><center style='color:#64748B; font-size:12px;'>" + subtitle + "</center></html>");
        subtitleLabel.setFont(HavenTheme.FONT_BODY);
        subtitleLabel.setForeground(HavenTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);

        if (actionText != null && onAction != null) {
            card.add(Box.createVerticalStrut(18));
            JButton btn = ModernButton.secondary(actionText);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> onAction.run());
            card.add(btn);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(card, gbc);
    }

    // Scrollable implementation to guarantee exact viewport tracking and centering
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            return parent.getWidth() >= getPreferredSize().width;
        }
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            return parent.getHeight() >= getPreferredSize().height;
        }
        return true;
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

    public static EmptyStatePanel forOwnerEnquiries(Runnable onRefresh) {
        return new EmptyStatePanel("NO ACTIVE ENQUIRIES", "No Pending Student Enquiries",
                "All student applications have been addressed or no active enquiries match the current filter.",
                "Refresh Enquiries", onRefresh);
    }
}
