package haven.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Floating Toast notification banner for user feedback.
 * Styled with HavenTheme semantic colors and typography.
 */
public class ToastNotification extends JPanel {

    public enum Type {
        SUCCESS(HavenTheme.SUCCESS_BG, HavenTheme.SUCCESS, HavenTheme.SUCCESS_BORDER, "[OK]"),
        INFO(HavenTheme.INFO_BG, HavenTheme.INFO, HavenTheme.INFO_BORDER, "[INFO]"),
        WARNING(HavenTheme.WARNING_BG, HavenTheme.WARNING, HavenTheme.WARNING_BORDER, "[WARN]"),
        ERROR(HavenTheme.DANGER_BG, HavenTheme.DANGER, HavenTheme.DANGER_BORDER, "[ERROR]");

        final Color bg;
        final Color fg;
        final Color border;
        final String tag;

        Type(Color bg, Color fg, Color border, String tag) {
            this.bg = bg;
            this.fg = fg;
            this.border = border;
            this.tag = tag;
        }
    }

    private final Color bgColor;
    private final Color borderColor;

    public ToastNotification(String message, Type type) {
        this.bgColor = type.bg;
        this.borderColor = type.border;

        setLayout(new FlowLayout(FlowLayout.CENTER, 8, 7));
        setOpaque(false);
        setBorder(new EmptyBorder(3, 14, 3, 14));

        JLabel tagLabel = new JLabel(type.tag);
        tagLabel.setFont(HavenTheme.FONT_CAPTION_BOLD);
        tagLabel.setForeground(type.fg);

        JLabel textLabel = new JLabel(message);
        textLabel.setFont(HavenTheme.FONT_BODY_BOLD);
        textLabel.setForeground(type.fg);

        add(tagLabel);
        add(textLabel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }

    /**
     * Shows a toast notification on the glass pane or bottom of the target frame.
     */
    public static void show(JFrame frame, String message, Type type) {
        if (frame == null) return;

        JLayeredPane layeredPane = frame.getLayeredPane();
        ToastNotification toast = new ToastNotification(message, type);

        Dimension toastSize = toast.getPreferredSize();
        int x = (frame.getWidth() - toastSize.width) / 2;
        int y = 72; // Float cleanly beneath top navigation header, preventing collision with bottom docks

        toast.setBounds(x, y, toastSize.width, toastSize.height);
        layeredPane.add(toast, JLayeredPane.POPUP_LAYER);
        layeredPane.repaint();

        Timer timer = new Timer(2800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layeredPane.remove(toast);
                layeredPane.repaint();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
