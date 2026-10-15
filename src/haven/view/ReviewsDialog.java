package haven.view;

import haven.db.ReviewDAO;
import haven.model.Accommodation;
import haven.model.Review;
import haven.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * ReviewsDialog - Verified student ratings and reviews modal.
 * Styled with HavenTheme design tokens and non-emoji badges.
 */
public class ReviewsDialog extends JDialog {
    private final Accommodation accommodation;
    private final ReviewDAO reviewDAO;
    private final User currentUser;
    private final Runnable onReviewsUpdated;

    private JPanel reviewsListPanel;
    private JLabel summaryLabel;

    public ReviewsDialog(Window parent, Accommodation accommodation, ReviewDAO reviewDAO, User currentUser, Runnable onReviewsUpdated) {
        super(parent, "HAVEN - Ratings & Reviews: " + accommodation.getName(), ModalityType.APPLICATION_MODAL);
        this.accommodation = accommodation;
        this.reviewDAO = reviewDAO;
        this.currentUser = currentUser;
        this.onReviewsUpdated = onReviewsUpdated;

        setSize(560, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HavenTheme.BG_CANVAS);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLabel = new JLabel(accommodation.getName());
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        summaryLabel = new JLabel();
        summaryLabel.setFont(HavenTheme.FONT_CAPTION);
        summaryLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(summaryLabel);
        headerPanel.add(titleBlock, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Center Reviews List
        reviewsListPanel = new JPanel();
        reviewsListPanel.setLayout(new BoxLayout(reviewsListPanel, BoxLayout.Y_AXIS));
        reviewsListPanel.setBackground(HavenTheme.BG_MUTED);
        reviewsListPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JScrollPane scrollPane = new JScrollPane(reviewsListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

        // Footer: Add Review Panel (available to Students or demo users)
        if (currentUser == null || currentUser.isStudent()) {
            JPanel addReviewPanel = createAddReviewPanel();
            add(addReviewPanel, BorderLayout.SOUTH);
        }

        refreshReviews();
    }

    private JPanel createAddReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(HavenTheme.SURFACE_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 16, 14, 16)
        ));

        JLabel writeLabel = new JLabel("Leave a Review & Rating:");
        writeLabel.setFont(HavenTheme.FONT_BODY_BOLD);
        writeLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.add(writeLabel);
        controls.add(new JLabel("Rating:"));

        String[] ratingOptions = {"5 Stars (Excellent)", "4 Stars (Good)", "3 Stars (Average)", "2 Stars (Poor)", "1 Star (Terrible)"};
        JComboBox<String> ratingBox = new JComboBox<>(ratingOptions);
        ratingBox.setFont(HavenTheme.FONT_BODY);
        controls.add(ratingBox);
        panel.add(controls, BorderLayout.NORTH);

        JTextArea commentArea = new JTextArea(2, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setFont(HavenTheme.FONT_BODY);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        panel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

        JButton postBtn = ModernButton.primary("Post Review");
        postBtn.addActionListener(e -> {
            String comment = commentArea.getText().trim();
            if (comment.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your review feedback before posting.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int ratingVal = 5 - ratingBox.getSelectedIndex();
            String studentUser = (currentUser != null) ? currentUser.getUsername() : "student";
            String studentName = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Student";

            Review newReview = new Review(accommodation.getId(), studentUser, studentName, ratingVal, comment);
            reviewDAO.addReview(newReview);

            commentArea.setText("");
            ratingBox.setSelectedIndex(0);
            refreshReviews();

            if (onReviewsUpdated != null) {
                onReviewsUpdated.run();
            }
            JOptionPane.showMessageDialog(this, "Thank you! Your review has been added.", "Review Submitted", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(postBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshReviews() {
        reviewsListPanel.removeAll();
        List<Review> reviews = reviewDAO.getReviewsForAccommodation(accommodation.getId());
        double avg = reviewDAO.getAverageRating(accommodation.getId());

        if (reviews.isEmpty()) {
            summaryLabel.setText("No reviews yet. Be the first to share your experience!");
            JLabel emptyLabel = new JLabel("<html><center style='color:#64748B;'><br><br><b>No reviews written yet.</b><br>Share your stay feedback below!</center></html>", SwingConstants.CENTER);
            reviewsListPanel.add(emptyLabel);
        } else {
            summaryLabel.setText(String.format("Average Rating: ★ %.1f / 5.0 (%d verified review%s)", avg, reviews.size(), reviews.size() == 1 ? "" : "s"));

            for (Review r : reviews) {
                JPanel card = new JPanel(new BorderLayout(6, 6));
                card.setBackground(HavenTheme.SURFACE_WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                ));

                JPanel topRow = new JPanel(new BorderLayout());
                topRow.setOpaque(false);

                JLabel nameBadge = new JLabel(r.getStudentFullName() + " (" + r.getStudentUsername() + ")");
                nameBadge.setFont(HavenTheme.FONT_BODY_BOLD);
                nameBadge.setForeground(HavenTheme.TEXT_PRIMARY);

                JLabel dateBadge = new JLabel(r.getFormattedDate());
                dateBadge.setFont(HavenTheme.FONT_CAPTION);
                dateBadge.setForeground(HavenTheme.TEXT_MUTED);

                topRow.add(nameBadge, BorderLayout.WEST);
                topRow.add(dateBadge, BorderLayout.EAST);

                JLabel stars = new JLabel(r.getStarsDisplay() + "  (" + r.getRating() + "/5)");
                stars.setFont(HavenTheme.FONT_CAPTION_BOLD);
                stars.setForeground(HavenTheme.WARNING);

                JLabel commentLabel = new JLabel("<html><body style='width: 380px; color:#334155; font-size:11px;'>" + r.getComment() + "</body></html>");

                JPanel content = new JPanel();
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setOpaque(false);
                content.add(topRow);
                content.add(Box.createVerticalStrut(3));
                content.add(stars);
                content.add(Box.createVerticalStrut(4));
                content.add(commentLabel);

                card.add(content, BorderLayout.CENTER);
                reviewsListPanel.add(card);
                reviewsListPanel.add(Box.createVerticalStrut(8));
            }
        }

        reviewsListPanel.revalidate();
        reviewsListPanel.repaint();
    }
}
