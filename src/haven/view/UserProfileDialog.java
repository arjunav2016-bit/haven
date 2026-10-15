package haven.view;

import haven.db.UserDAO;
import haven.model.Student;
import haven.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * UserProfileDialog - View and edit account profile and student stay preferences.
 * Styled cleanly with HavenTheme design tokens (No Emojis).
 */
public class UserProfileDialog extends JDialog {
    private final User currentUser;
    private final UserDAO userDAO;
    private final Runnable onProfileUpdated;

    private JTextField nameField, phoneField, budgetField;
    private JComboBox<String> collegeBox, genderBox;

    public UserProfileDialog(Window parent, User currentUser, UserDAO userDAO, Runnable onProfileUpdated) {
        super(parent, "HAVEN - Account Profile & Settings", ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.userDAO = userDAO;
        this.onProfileUpdated = onProfileUpdated;

        setSize(500, 530);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HavenTheme.BG_CANVAS);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel titleLabel = new JLabel("Account Profile & Settings");
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Signed in as @" + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        subtitleLabel.setFont(HavenTheme.FONT_CAPTION);
        subtitleLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subtitleLabel);
        headerPanel.add(titleBlock, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Tabs for Profile Info & Change Password
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HavenTheme.FONT_SUBTITLE);
        tabbedPane.addTab("Profile & Preferences", createProfilePanel());
        tabbedPane.addTab("Change Password", createPasswordPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(HavenTheme.SURFACE_WHITE);
        panel.setBorder(new EmptyBorder(18, 24, 18, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(currentUser.getFullName(), 15);
        nameField.setFont(HavenTheme.FONT_BODY);
        nameField.setBorder(HavenTheme.createInputPadding());

        phoneField = new JTextField(currentUser.getPhone(), 15);
        phoneField.setFont(HavenTheme.FONT_BODY);
        phoneField.setBorder(HavenTheme.createInputPadding());

        int row = 0;
        addFormRow(panel, gbc, row++, "Full Name:", nameField);
        addFormRow(panel, gbc, row++, "Phone Number:", phoneField);

        if (currentUser instanceof Student) {
            Student s = (Student) currentUser;
            String[] colleges = {"SCTCE", "CET", "GEC Barton Hill", "Mar Baselios (MBCET)", "LBS Institute"};
            collegeBox = new JComboBox<>(colleges);
            collegeBox.setFont(HavenTheme.FONT_BODY);
            for (int i = 0; i < colleges.length; i++) {
                if (s.getCollegeName() != null && s.getCollegeName().toLowerCase().contains(colleges[i].toLowerCase())) {
                    collegeBox.setSelectedIndex(i);
                    break;
                }
            }

            genderBox = new JComboBox<>(new String[]{"Any", "Boys", "Girls"});
            genderBox.setFont(HavenTheme.FONT_BODY);
            genderBox.setSelectedItem(s.getGenderPreference() != null ? s.getGenderPreference() : "Any");

            budgetField = new JTextField(String.format("%.0f", s.getMaxBudget()), 10);
            budgetField.setFont(HavenTheme.FONT_BODY);
            budgetField.setBorder(HavenTheme.createInputPadding());

            addFormRow(panel, gbc, row++, "Enrolled College:", collegeBox);
            addFormRow(panel, gbc, row++, "Gender Pref:", genderBox);
            addFormRow(panel, gbc, row++, "Max Budget (₹):", budgetField);
        }

        JButton saveBtn = ModernButton.primary("Save Profile Changes");
        saveBtn.addActionListener(e -> handleSaveProfile());

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panel.add(saveBtn, gbc);

        return panel;
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(HavenTheme.SURFACE_WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPasswordField oldPass = new JPasswordField(15);
        oldPass.setFont(HavenTheme.FONT_BODY);
        oldPass.setBorder(HavenTheme.createInputPadding());

        JPasswordField newPass = new JPasswordField(15);
        newPass.setFont(HavenTheme.FONT_BODY);
        newPass.setBorder(HavenTheme.createInputPadding());

        JPasswordField confirmPass = new JPasswordField(15);
        confirmPass.setFont(HavenTheme.FONT_BODY);
        confirmPass.setBorder(HavenTheme.createInputPadding());

        int row = 0;
        addFormRow(panel, gbc, row++, "Current Password:", oldPass);
        addFormRow(panel, gbc, row++, "New Password:", newPass);
        addFormRow(panel, gbc, row++, "Confirm Password:", confirmPass);

        JButton changeBtn = ModernButton.primary("Update Password");
        changeBtn.addActionListener(e -> {
            String oldP = new String(oldPass.getPassword()).trim();
            String newP = new String(newPass.getPassword()).trim();
            String confP = new String(confirmPass.getPassword()).trim();

            if (oldP.isEmpty() || newP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your current and new password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newP.equals(confP)) {
                JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Mismatch", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newP.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password should be at least 4 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = userDAO.changePassword(currentUser.getUsername(), oldP, newP);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                oldPass.setText(""); newPass.setText(""); confirmPass.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect current password. Please try again.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panel.add(changeBtn, gbc);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(HavenTheme.FONT_BODY_BOLD);
        lbl.setForeground(HavenTheme.TEXT_BODY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void handleSaveProfile() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String college = (collegeBox != null) ? (String) collegeBox.getSelectedItem() : "SCTCE";
        String gender = (genderBox != null) ? (String) genderBox.getSelectedItem() : "Any";
        double budget = 8000.0;

        if (budgetField != null && !budgetField.getText().trim().isEmpty()) {
            try {
                budget = Double.parseDouble(budgetField.getText().trim());
            } catch (NumberFormatException ignored) {}
        }

        userDAO.updateProfile(currentUser.getUsername(), name, phone, college, gender, budget);

        currentUser.setFullName(name);
        currentUser.setPhone(phone);
        if (currentUser instanceof Student) {
            Student s = (Student) currentUser;
            s.setCollegeName(college);
            s.setGenderPreference(gender);
            s.setMaxBudget(budget);
        }

        JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        if (onProfileUpdated != null) {
            onProfileUpdated.run();
        }
        dispose();
    }
}
