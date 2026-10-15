package haven.view;

import haven.db.UserDAO;
import haven.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * RegisterDialog - Create Account portal styled with HavenTheme.
 */
public class RegisterDialog extends JDialog {
    private final UserDAO userDAO;
    private User registeredUser;

    private final JComboBox<String> roleCombo;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField userField;
    private final JPasswordField passField;
    private final JPasswordField confirmPassField;

    // Student-specific fields
    private final JPanel studentPanel;
    private final JTextField collegeField;
    private final JComboBox<String> genderPrefCombo;
    private final JTextField budgetField;

    public RegisterDialog(Dialog parent, UserDAO userDAO) {
        super(parent, "HAVEN - Create New Account", true);
        this.userDAO = userDAO;

        setSize(500, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HavenTheme.BG_CANVAS);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(16, 24, 14, 24)
        ));

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Register as a student or property owner");
        subtitleLabel.setFont(HavenTheme.FONT_CAPTION);
        subtitleLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form Panel
        JPanel formContainer = new JPanel();
        formContainer.setBackground(HavenTheme.SURFACE_WHITE);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Role Selector
        roleCombo = new JComboBox<>(new String[]{"Student (Hostel Seeker)", "Property Owner"});
        roleCombo.setFont(HavenTheme.FONT_BODY_BOLD);
        roleCombo.setBackground(HavenTheme.SURFACE_WHITE);

        nameField = createStyledTextField();
        phoneField = createStyledTextField();
        userField = createStyledTextField();
        passField = createStyledPasswordField();
        confirmPassField = createStyledPasswordField();

        collegeField = createStyledTextField();
        collegeField.setText("SCT College of Engineering, Pappanamcode");
        genderPrefCombo = new JComboBox<>(new String[]{"Any", "Boys", "Girls"});
        genderPrefCombo.setFont(HavenTheme.FONT_BODY);
        genderPrefCombo.setBackground(HavenTheme.SURFACE_WHITE);
        budgetField = createStyledTextField();
        budgetField.setText("8000");

        formContainer.add(createFieldRow("Account Type:", roleCombo));
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(createFieldRow("Full Name:", nameField));
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(createFieldRow("Phone Number:", phoneField));
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(createFieldRow("Desired Username:", userField));
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(createFieldRow("Password:", passField));
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(createFieldRow("Confirm Password:", confirmPassField));
        formContainer.add(Box.createVerticalStrut(10));

        // Student specifics panel
        studentPanel = new JPanel();
        studentPanel.setBackground(HavenTheme.SURFACE_WHITE);
        studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));
        studentPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(HavenTheme.BORDER_LIGHT, 1, true),
                "Student Preferences", 0, 0,
                HavenTheme.FONT_CAPTION_BOLD,
                HavenTheme.TEXT_SECONDARY
        ));

        JPanel studentContent = new JPanel();
        studentContent.setOpaque(false);
        studentContent.setLayout(new BoxLayout(studentContent, BoxLayout.Y_AXIS));
        studentContent.setBorder(new EmptyBorder(6, 10, 8, 10));
        studentContent.add(createFieldRow("College / Campus:", collegeField));
        studentContent.add(Box.createVerticalStrut(6));
        studentContent.add(createFieldRow("Stay Preference:", genderPrefCombo));
        studentContent.add(Box.createVerticalStrut(6));
        studentContent.add(createFieldRow("Max Budget (₹/mo):", budgetField));
        studentPanel.add(studentContent);

        formContainer.add(studentPanel);

        // Toggle student specifics when role changes
        roleCombo.addActionListener(e -> {
            boolean isStudent = roleCombo.getSelectedIndex() == 0;
            studentPanel.setVisible(isStudent);
            revalidate();
            repaint();
        });

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(HavenTheme.BG_CANVAS);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 24, 16, 24)
        ));

        ModernButton cancelBtn = ModernButton.subtle("Cancel");
        ModernButton submitBtn = ModernButton.primary("Create Account & Sign In");

        cancelBtn.addActionListener(e -> dispose());
        submitBtn.addActionListener(e -> handleRegister());

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(cancelBtn);
        rightBtns.add(submitBtn);

        btnPanel.add(rightBtns, BorderLayout.EAST);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createFieldRow(String labelText, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(140, 28));
        label.setFont(HavenTheme.FONT_BODY);
        label.setForeground(HavenTheme.TEXT_BODY);

        row.add(label, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        return row;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(HavenTheme.FONT_BODY);
        tf.setPreferredSize(new Dimension(200, 30));
        tf.setBorder(HavenTheme.createInputPadding());
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(HavenTheme.FONT_BODY);
        pf.setPreferredSize(new Dimension(200, 30));
        pf.setBorder(HavenTheme.createInputPadding());
        return pf;
    }

    private void handleRegister() {
        String role = roleCombo.getSelectedIndex() == 0 ? "STUDENT" : "OWNER";
        String fullName = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();
        String confirmPassword = new String(confirmPassField.getPassword()).trim();

        if (fullName.isEmpty()) {
            showError("Please enter your full name.");
            nameField.requestFocus();
            return;
        }

        if (phone.isEmpty() || !phone.matches("\\d{10}")) {
            showError("Please enter a valid 10-digit mobile number.");
            phoneField.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidUsername(username)) {
            showError("Username must be 3-30 characters and contain only letters, numbers, and underscores.");
            userField.requestFocus();
            return;
        }

        if (userDAO.userExists(username)) {
            showError("Username '" + username + "' is already taken. Please choose another.");
            userField.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showError("Password must be at least 4 characters long.");
            passField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match. Please re-enter.");
            confirmPassField.requestFocus();
            return;
        }

        String college = collegeField.getText().trim();
        String genderPref = (String) genderPrefCombo.getSelectedItem();
        double budget = 8000.0;
        if ("STUDENT".equals(role)) {
            try {
                budget = Double.parseDouble(budgetField.getText().trim());
                if (budget <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError("Please enter a valid monthly budget amount (e.g. 7500).");
                budgetField.requestFocus();
                return;
            }
        }

        try {
            registeredUser = userDAO.registerUser(username, password, role, fullName, phone, college, genderPref, budget);
            JOptionPane.showMessageDialog(this,
                    "Account registered successfully!\nWelcome to HAVEN, " + fullName + " (" + role + ")!",
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public User getRegisteredUser() {
        return registeredUser;
    }
}
