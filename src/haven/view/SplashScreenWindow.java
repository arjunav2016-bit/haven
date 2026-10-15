package haven.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * SplashScreenWindow - Branded startup splash screen with smooth progress animation
 * and architectural initialization status messages.
 */
public class SplashScreenWindow extends JWindow {
    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public SplashScreenWindow() {
        setSize(520, 290);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(49, 46, 129)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle architectural grid lines
                g2.setColor(new Color(255, 255, 255, 14));
                for (int x = 0; x < getWidth(); x += 40) {
                    g2.drawLine(x, 0, x, getHeight());
                }
                for (int y = 0; y < getHeight(); y += 40) {
                    g2.drawLine(0, y, getWidth(), y);
                }
                g2.dispose();
            }
        };
        content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.PRIMARY_BORDER, 1),
                new EmptyBorder(32, 36, 26, 36)
        ));

        // Center Brand Block
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);

        JLabel pill = new JLabel("ENTERPRISE STUDENT HOUSING NETWORK");
        pill.setFont(HavenTheme.FONT_BADGE);
        pill.setForeground(new Color(165, 180, 252));
        pill.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("HAVEN");
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Hostel & Accommodation Verification & Enrollment Network");
        subtitle.setFont(HavenTheme.FONT_SUBTITLE);
        subtitle.setForeground(new Color(226, 232, 240));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel region = new JLabel("Thiruvananthapuram Engineering Campus Cluster (SCTCE • CET • GEC • MBCET • LBS)");
        region.setFont(HavenTheme.FONT_CAPTION);
        region.setForeground(new Color(148, 163, 184));
        region.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(pill);
        brandPanel.add(Box.createVerticalStrut(8));
        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(subtitle);
        brandPanel.add(Box.createVerticalStrut(6));
        brandPanel.add(region);

        content.add(brandPanel, BorderLayout.CENTER);

        // Bottom Progress Block
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);

        statusLabel = new JLabel("Initializing HAVEN Repository & EventBus Architecture...");
        statusLabel.setFont(HavenTheme.FONT_CAPTION);
        statusLabel.setForeground(new Color(199, 210, 254));

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(440, 6));
        progressBar.setForeground(new Color(99, 102, 241));
        progressBar.setBackground(new Color(30, 41, 59));
        progressBar.setBorderPainted(false);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        content.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(content);
    }

    public void setProgress(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(Math.max(0, Math.min(100, percent)));
            if (status != null) {
                statusLabel.setText(status);
            }
        });
    }
}
