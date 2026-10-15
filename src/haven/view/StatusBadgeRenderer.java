package haven.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Reusable table cell renderer for enquiry status badges with color-coded backgrounds.
 * Uses HavenTheme semantic colors and typography.
 *
 * Demonstrates the DRY (Don't Repeat Yourself) principle and component reusability.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(HavenTheme.FONT_CAPTION_BOLD);
        setBorder(new EmptyBorder(4, 8, 4, 8));

        if (value != null && !isSelected) {
            String status = value.toString().toUpperCase();
            switch (status) {
                case "CONFIRMED":
                case "APPROVED":
                case "ACTIVE":
                    setForeground(HavenTheme.SUCCESS);
                    setBackground(HavenTheme.SUCCESS_BG);
                    break;
                case "PENDING":
                case "FLAGGED":
                    setForeground(HavenTheme.WARNING);
                    setBackground(HavenTheme.WARNING_BG);
                    break;
                case "CONTACTED":
                    setForeground(HavenTheme.INFO);
                    setBackground(HavenTheme.INFO_BG);
                    break;
                case "DECLINED":
                case "REJECTED":
                case "DEACTIVATED":
                    setForeground(HavenTheme.DANGER);
                    setBackground(HavenTheme.DANGER_BG);
                    break;
                case "CANCELLED":
                    setForeground(HavenTheme.TEXT_MUTED);
                    setBackground(HavenTheme.BG_MUTED);
                    break;
                default:
                    setForeground(HavenTheme.TEXT_BODY);
                    setBackground(HavenTheme.SURFACE_WHITE);
                    break;
            }
        }
        return c;
    }
}
