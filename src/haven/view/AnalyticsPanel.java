package haven.view;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsPanel - Interactive Graphics2D dashboard displaying business intelligence
 * and pipeline metrics for property owners.
 * Styled with HavenTheme design tokens and non-emoji charts.
 */
public class AnalyticsPanel extends JPanel {
    private final User currentUser;
    private final HostelDAO hostelDAO;
    private final EnquiryDAO enquiryDAO;

    private JLabel propCountVal, bedsCountVal, revenueVal, enquiriesVal;
    private EnquiryBarChartPanel barChartPanel;
    private OccupancyGaugePanel gaugePanel;

    public AnalyticsPanel(User currentUser, HostelDAO hostelDAO, EnquiryDAO enquiryDAO) {
        this.currentUser = currentUser;
        this.hostelDAO = hostelDAO;
        this.enquiryDAO = enquiryDAO;

        setLayout(new BorderLayout(14, 14));
        setBackground(HavenTheme.BG_CANVAS);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Metric Cards Header
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 12, 12));
        metricsGrid.setOpaque(false);

        propCountVal = new JLabel("0");
        bedsCountVal = new JLabel("0");
        revenueVal = new JLabel("₹ 0");
        enquiriesVal = new JLabel("0");

        metricsGrid.add(createMetricCard("Total Properties", propCountVal, "Active listings in Haven", HavenTheme.PRIMARY));
        metricsGrid.add(createMetricCard("Bed Capacity", bedsCountVal, "Vacant vs filled capacity", HavenTheme.SUCCESS));
        metricsGrid.add(createMetricCard("Est. Monthly Revenue", revenueVal, "Projected full occupancy", HavenTheme.WARNING));
        metricsGrid.add(createMetricCard("Enquiry Pipeline", enquiriesVal, "Total student applications", HavenTheme.INFO));

        add(metricsGrid, BorderLayout.NORTH);

        // 2. Charts Center Panel (Bar Chart + Occupancy Gauge)
        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 14, 14));
        chartsContainer.setOpaque(false);

        barChartPanel = new EnquiryBarChartPanel();
        gaugePanel = new OccupancyGaugePanel();

        chartsContainer.add(barChartPanel);
        chartsContainer.add(gaugePanel);

        add(chartsContainer, BorderLayout.CENTER);

        // 3. Footer with Refresh button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footerPanel.setOpaque(false);
        JButton refreshBtn = ModernButton.subtle("Refresh Analytics");
        refreshBtn.addActionListener(e -> refreshData());
        footerPanel.add(refreshBtn);
        add(footerPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(HavenTheme.SURFACE_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HavenTheme.FONT_CAPTION_BOLD);
        titleLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        valueLabel.setFont(HavenTheme.FONT_HERO);
        valueLabel.setForeground(accentColor);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(HavenTheme.FONT_CAPTION);
        subLabel.setForeground(HavenTheme.TEXT_MUTED);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLabel);

        return card;
    }

    public void refreshData() {
        String ownerName = (currentUser != null && currentUser.getUsername() != null) ? currentUser.getUsername() : "owner";
        haven.model.Role ownerRole = (currentUser != null) ? currentUser.getRoleEnum() : null;
        List<Accommodation> stays = hostelDAO.getAccommodationsByOwner(ownerName, ownerRole);
        List<Enquiry> enquiries = enquiryDAO.getEnquiriesForOwner(ownerName, ownerRole);

        // Calculate properties & beds
        int totalProperties = stays.size();
        int totalVacancies = 0;
        double projectedRevenue = 0.0;

        for (Accommodation acc : stays) {
            totalVacancies += acc.getVacancies();
            double cost = acc.calculateTotalMonthlyCost(1);
            projectedRevenue += (cost * Math.max(1, acc.getVacancies()));
        }

        int estimatedTotalBeds = totalVacancies + (enquiries.size() > 0 ? (int)(enquiries.size() * 0.7) : 15);
        int occupiedBeds = Math.max(0, estimatedTotalBeds - totalVacancies);
        double occupancyRate = (estimatedTotalBeds > 0) ? ((double) occupiedBeds / estimatedTotalBeds) * 100.0 : 0.0;

        propCountVal.setText(String.valueOf(totalProperties));
        bedsCountVal.setText(String.format("%d Vacant / %d Total", totalVacancies, estimatedTotalBeds));
        revenueVal.setText(String.format("₹ %,.0f", projectedRevenue));
        enquiriesVal.setText(String.valueOf(enquiries.size()));

        // Update Charts
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("PENDING", 0);
        statusCounts.put("CONFIRMED", 0);
        statusCounts.put("CONTACTED", 0);
        statusCounts.put("DECLINED", 0);
        statusCounts.put("CANCELLED", 0);

        for (Enquiry eq : enquiries) {
            String st = eq.getStatus().toUpperCase();
            statusCounts.put(st, statusCounts.getOrDefault(st, 0) + 1);
        }

        barChartPanel.setData(statusCounts);
        gaugePanel.setData(occupiedBeds, totalVacancies, occupancyRate);
    }

    private static class EnquiryBarChartPanel extends JPanel {
        private Map<String, Integer> statusData = new HashMap<>();

        public EnquiryBarChartPanel() {
            setBackground(HavenTheme.SURFACE_WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                    new EmptyBorder(16, 16, 16, 16)
            ));
        }

        public void setData(Map<String, Integer> data) {
            this.statusData = new HashMap<>(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Header
            g2.setFont(HavenTheme.FONT_SUBTITLE);
            g2.setColor(HavenTheme.TEXT_PRIMARY);
            g2.drawString("Student Enquiry Status Distribution", 18, 28);

            g2.setFont(HavenTheme.FONT_CAPTION);
            g2.setColor(HavenTheme.TEXT_SECONDARY);
            g2.drawString("Breakdown of student enquiry pipeline states", 18, 44);

            String[] categories = {"PENDING", "CONFIRMED", "CONTACTED", "DECLINED", "CANCELLED"};
            Color[] colors = {
                    HavenTheme.WARNING,
                    HavenTheme.SUCCESS,
                    HavenTheme.INFO,
                    HavenTheme.DANGER,
                    HavenTheme.TEXT_MUTED
            };

            int maxVal = 1;
            for (String cat : categories) {
                maxVal = Math.max(maxVal, statusData.getOrDefault(cat, 0));
            }

            int topMargin = 70;
            int bottomMargin = 45;
            int leftMargin = 30;
            int rightMargin = 20;
            int plotHeight = height - topMargin - bottomMargin;
            int plotWidth = width - leftMargin - rightMargin;

            // Horizontal Grid line
            g2.setColor(HavenTheme.BG_MUTED);
            g2.drawLine(leftMargin, topMargin + plotHeight, leftMargin + plotWidth, topMargin + plotHeight);

            int barCount = categories.length;
            int slotWidth = plotWidth / barCount;
            int barWidth = Math.min(48, (int) (slotWidth * 0.55));

            for (int i = 0; i < barCount; i++) {
                String cat = categories[i];
                int val = statusData.getOrDefault(cat, 0);
                int barHeight = (int) (((double) val / maxVal) * (plotHeight - 20));

                int x = leftMargin + (i * slotWidth) + ((slotWidth - barWidth) / 2);
                int y = topMargin + plotHeight - barHeight;

                // Draw Bar
                g2.setColor(colors[i]);
                g2.fillRoundRect(x, y, barWidth, Math.max(4, barHeight), 8, 8);

                // Draw Count on top
                g2.setColor(HavenTheme.TEXT_PRIMARY);
                g2.setFont(HavenTheme.FONT_CAPTION_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                String valStr = String.valueOf(val);
                g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 5);

                // Draw Category Name below
                g2.setColor(HavenTheme.TEXT_SECONDARY);
                g2.setFont(HavenTheme.FONT_BADGE);
                FontMetrics cfm = g2.getFontMetrics();
                String label = cat.substring(0, 1) + cat.substring(1).toLowerCase();
                g2.drawString(label, x + (barWidth - cfm.stringWidth(label)) / 2, topMargin + plotHeight + 18);
            }

            g2.dispose();
        }
    }

    private static class OccupancyGaugePanel extends JPanel {
        private int occupied = 0;
        private int vacant = 0;
        private double percentage = 0.0;

        public OccupancyGaugePanel() {
            setBackground(HavenTheme.SURFACE_WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                    new EmptyBorder(16, 16, 16, 16)
            ));
        }

        public void setData(int occupied, int vacant, double percentage) {
            this.occupied = occupied;
            this.vacant = vacant;
            this.percentage = percentage;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Header
            g2.setFont(HavenTheme.FONT_SUBTITLE);
            g2.setColor(HavenTheme.TEXT_PRIMARY);
            g2.drawString("Real-Time Capacity & Occupancy Rate", 18, 28);

            g2.setFont(HavenTheme.FONT_CAPTION);
            g2.setColor(HavenTheme.TEXT_SECONDARY);
            g2.drawString("Ratio of reserved rooms to total capacity", 18, 44);

            int ringSize = Math.min(width - 60, height - 120);
            int cx = (width - ringSize) / 2;
            int cy = 65 + (height - 110 - ringSize) / 2;
            int thickness = 22;

            // Background Ring Track
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(HavenTheme.BG_MUTED);
            g2.drawOval(cx + thickness / 2, cy + thickness / 2, ringSize - thickness, ringSize - thickness);

            // Active Occupancy Arc
            g2.setColor(HavenTheme.SUCCESS);
            int arcAngle = (int) Math.round((percentage / 100.0) * 360.0);
            g2.drawArc(cx + thickness / 2, cy + thickness / 2, ringSize - thickness, ringSize - thickness, 90, -arcAngle);

            // Center Text
            String pctText = String.format("%.0f%%", percentage);
            g2.setFont(HavenTheme.FONT_HERO);
            g2.setColor(HavenTheme.TEXT_PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx + (ringSize - fm.stringWidth(pctText)) / 2;
            int ty = cy + (ringSize / 2) + (fm.getAscent() / 4);
            g2.drawString(pctText, tx, ty);

            g2.setFont(HavenTheme.FONT_CAPTION);
            g2.setColor(HavenTheme.TEXT_SECONDARY);
            String sub = "Occupied";
            FontMetrics sfm = g2.getFontMetrics();
            g2.drawString(sub, cx + (ringSize - sfm.stringWidth(sub)) / 2, ty + 16);

            // Bottom Legend
            int legendY = height - 20;
            g2.setFont(HavenTheme.FONT_CAPTION);
            g2.setColor(HavenTheme.SUCCESS);
            g2.fillOval(width / 2 - 90, legendY - 8, 8, 8);
            g2.setColor(HavenTheme.TEXT_BODY);
            g2.drawString("Occupied (" + occupied + ")", width / 2 - 76, legendY);

            g2.setColor(HavenTheme.BORDER_SUBTLE);
            g2.fillOval(width / 2 + 20, legendY - 8, 8, 8);
            g2.setColor(HavenTheme.TEXT_BODY);
            g2.drawString("Vacant (" + vacant + ")", width / 2 + 34, legendY);

            g2.dispose();
        }
    }
}
