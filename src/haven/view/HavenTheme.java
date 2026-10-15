package haven.view;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * HavenTheme - Central Design System and Aesthetic Standard for HAVEN.
 * Encapsulates the Modern Indigo & Slate color palette, typography hierarchy,
 * card elevation styles, and enterprise data table styling.
 * 
 * Adheres strictly to clean, academic/enterprise desktop standards (No Emojis).
 */
public final class HavenTheme {
    private HavenTheme() {}

    // --- Color Palette: Modern Indigo & Slate ---
    public static final Color PRIMARY = new Color(79, 70, 229);        // Indigo 600
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);  // Indigo 700
    public static final Color PRIMARY_PRESSED = new Color(55, 48, 163);// Indigo 800
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255);// Indigo 50
    public static final Color PRIMARY_BORDER = new Color(199, 210, 254);// Indigo 200

    public static final Color BG_CANVAS = new Color(248, 250, 252);    // Slate 50
    public static final Color BG_MUTED = new Color(241, 245, 249);     // Slate 100
    public static final Color SURFACE_WHITE = Color.WHITE;
    public static final Color SURFACE_DARK = new Color(15, 23, 42);    // Slate 900

    public static final Color BORDER_LIGHT = new Color(226, 232, 240); // Slate 200
    public static final Color BORDER_SUBTLE = new Color(203, 213, 225);// Slate 300
    public static final Color BORDER_FOCUS = new Color(99, 102, 241);  // Indigo 500

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // Slate 900
    public static final Color TEXT_BODY = new Color(51, 65, 85);       // Slate 700
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);// Slate 500
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // Slate 400

    // Semantic Accents
    public static final Color SUCCESS = new Color(5, 150, 105);        // Emerald 600
    public static final Color SUCCESS_BG = new Color(236, 253, 245);   // Emerald 50
    public static final Color SUCCESS_BORDER = new Color(167, 243, 208);// Emerald 200

    public static final Color WARNING = new Color(217, 119, 6);        // Amber 600
    public static final Color WARNING_BG = new Color(254, 252, 232);   // Amber 50
    public static final Color WARNING_BORDER = new Color(253, 230, 138);// Amber 200

    public static final Color DANGER = new Color(220, 38, 38);         // Rose 600
    public static final Color DANGER_BG = new Color(254, 242, 242);    // Rose 50
    public static final Color DANGER_BORDER = new Color(254, 202, 202); // Rose 200

    public static final Color INFO = new Color(37, 99, 235);           // Blue 600
    public static final Color INFO_BG = new Color(239, 246, 255);      // Blue 50
    public static final Color INFO_BORDER = new Color(191, 219, 254);  // Blue 200

    // --- Typography Hierarchy ---
    public static final Font FONT_HERO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_CAPTION = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_CAPTION_BOLD = new Font("SansSerif", Font.BOLD, 11);
    public static final Font FONT_BADGE = new Font("SansSerif", Font.BOLD, 10);

    // --- Borders & Containers ---
    public static CompoundBorder createCardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        );
    }

    public static CompoundBorder createHeaderBorder() {
        return BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                new EmptyBorder(14, 20, 14, 20)
        );
    }

    public static CompoundBorder createInputPadding() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    // --- Enterprise Table Styling ---
    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(FONT_BODY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_CAPTION_BOLD);
        header.setBackground(BG_MUTED);
        header.setForeground(TEXT_BODY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_LIGHT));
        header.setReorderingAllowed(false);

        TableCellRenderer defaultHeaderRenderer = header.getDefaultRenderer();
        header.setDefaultRenderer((t, val, isSel, hasFocus, row, col) -> {
            JLabel lbl = (JLabel) defaultHeaderRenderer.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
            lbl.setFont(FONT_CAPTION_BOLD);
            lbl.setForeground(TEXT_BODY);
            lbl.setBackground(BG_MUTED);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                    new EmptyBorder(0, 10, 0, 10)
            ));
            return lbl;
        });

        // Zebra Striping Default Cell Renderer
        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSel) {
                    c.setBackground((row % 2 == 0) ? SURFACE_WHITE : BG_CANVAS);
                    c.setForeground(TEXT_BODY);
                } else {
                    c.setBackground(PRIMARY_LIGHT);
                    c.setForeground(PRIMARY_HOVER);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
        }
    }

    // --- Badge & Chip UI Helper ---
    public static JLabel createBadge(String text, Color fgColor, Color bgColor, Color borderColor) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(FONT_CAPTION_BOLD);
        badge.setForeground(fgColor);
        badge.setBackground(bgColor);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }

    public static JLabel createPill(String text, Color fgColor, Color bgColor) {
        return createBadge(text, fgColor, bgColor, bgColor);
    }

    // --- Header Banner Factory ---
    public static JPanel createHeader(String titleText, String subtitleText) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CANVAS);
        header.setBorder(createHeaderBorder());

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel titleLbl = new JLabel(titleText);
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitleText);
        subLbl.setFont(FONT_CAPTION);
        subLbl.setForeground(TEXT_SECONDARY);

        titles.add(titleLbl);
        titles.add(Box.createVerticalStrut(3));
        titles.add(subLbl);

        header.add(titles, BorderLayout.WEST);
        return header;
    }
}
