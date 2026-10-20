package haven.view;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * ModernButton - Clean, styled button with subtle hover, uniform height,
 * and high-contrast professional styling conforming to HavenTheme.
 */
public class ModernButton extends JButton {
    private final Color defaultBg;
    private final Color hoverBg;
    private final Color pressedBg;
    private final Color disabledBg;
    private final Color borderColor;

    public ModernButton(String text, Color bgColor, Color fgColor, Color borderColor) {
        super(text);
        this.defaultBg = bgColor;
        this.hoverBg = isLight(bgColor) ? blend(bgColor, Color.BLACK, 0.05f) : blend(bgColor, Color.BLACK, 0.12f);
        this.pressedBg = isLight(bgColor) ? blend(bgColor, Color.BLACK, 0.10f) : blend(bgColor, Color.BLACK, 0.22f);
        this.disabledBg = HavenTheme.BG_MUTED;
        this.borderColor = borderColor;

        setUI(new ModernButtonUI());
        setForeground(fgColor);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(HavenTheme.FONT_BODY_BOLD);
        setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
    }

    public ModernButton(String text, Color bgColor, Color fgColor) {
        this(text, bgColor, fgColor, isLight(bgColor) ? HavenTheme.BORDER_SUBTLE : null);
    }

    public ModernButton(String text, Color bgColor) {
        this(text, bgColor, isLight(bgColor) ? HavenTheme.TEXT_PRIMARY : Color.WHITE);
    }

    public static ModernButton primary(String text) {
        return new ModernButton(text, HavenTheme.PRIMARY, Color.WHITE, null);
    }

    public static ModernButton secondary(String text) {
        return new ModernButton(text, HavenTheme.SURFACE_WHITE, HavenTheme.TEXT_BODY, HavenTheme.BORDER_SUBTLE);
    }

    public static ModernButton subtle(String text) {
        return new ModernButton(text, HavenTheme.BG_MUTED, HavenTheme.TEXT_BODY, HavenTheme.BORDER_LIGHT);
    }

    public static ModernButton danger(String text) {
        return new ModernButton(text, HavenTheme.DANGER_BG, HavenTheme.DANGER, HavenTheme.DANGER_BORDER);
    }

    public static ModernButton success(String text) {
        return new ModernButton(text, HavenTheme.SUCCESS_BG, HavenTheme.SUCCESS, HavenTheme.SUCCESS_BORDER);
    }

    private static boolean isLight(Color c) {
        if (c == null) return true;
        double luma = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
        return luma > 0.65;
    }

    private static Color blend(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, b)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (!isEnabled()) {
            g2.setColor(disabledBg);
            g2.fillRoundRect(0, 0, width, height, 8, 8);
            g2.setColor(HavenTheme.BORDER_LIGHT);
            g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
        } else {
            if (getModel().isPressed()) {
                g2.setColor(pressedBg);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(defaultBg);
            }
            g2.fillRoundRect(0, 0, width, height, 8, 8);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
            }
        }
        g2.dispose();
        super.paintComponent(g);
    }

    private static class ModernButtonUI extends BasicButtonUI {
        @Override
        protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
            AbstractButton b = (AbstractButton) c;
            FontMetrics fm = g.getFontMetrics();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (b.isEnabled()) {
                g2.setColor(b.getForeground());
            } else {
                g2.setColor(HavenTheme.TEXT_MUTED);
            }
            g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
            g2.dispose();
        }
    }
}
