package haven.view;

import haven.db.UserDAO;
import haven.exception.AuthenticationException;
import haven.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * LoginDialog - Authentication and role selection for HAVEN.
 * Allows sign in as Student, Owner, or Admin with test account quick-fill options.
 */
public class LoginDialog extends JDialog {
    private final UserDAO userDAO;
    private User authenticatedUser;

    private final JTextField userField;
    private final JPasswordField passField;

    public LoginDialog(Frame parent) {
        super(parent, "HAVEN - Login Portal", true);
        this.userDAO = new UserDAO();

        setSize(460, 340);
        setLocationRelativeTo(parent);
        setResizable(false);
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());

        // Header Panel - HavenTheme styling
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HavenTheme.BG_CANVAS);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(16, 24, 14, 24)
        ));

        JLabel titleLabel = new JLabel("Welcome to HAVEN");
        titleLabel.setFont(HavenTheme.FONT_TITLE);
        titleLabel.setForeground(HavenTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Student Accommodation Discovery Platform - Trivandrum");
        subtitleLabel.setFont(HavenTheme.FONT_CAPTION);
        subtitleLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Credentials Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(HavenTheme.SURFACE_WHITE);
        formPanel.setBorder(new EmptyBorder(14, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(HavenTheme.FONT_BODY_BOLD);
        userLabel.setForeground(HavenTheme.TEXT_BODY);

        userField = new JTextField("", 15);
        userField.setFont(HavenTheme.FONT_BODY);
        userField.setPreferredSize(new Dimension(220, 32));
        userField.setBorder(HavenTheme.createInputPadding());

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(HavenTheme.FONT_BODY_BOLD);
        passLabel.setForeground(HavenTheme.TEXT_BODY);

        passField = new JPasswordField("", 15);
        passField.setFont(HavenTheme.FONT_BODY);
        passField.setPreferredSize(new Dimension(220, 32));
        passField.setBorder(HavenTheme.createInputPadding());

        // Enter key submits login
        userField.addActionListener(e -> attemptLogin());
        passField.addActionListener(e -> attemptLogin());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        formPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formPanel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        formPanel.add(passField, gbc);

        // Quick Demo Fillers Bar
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        demoBar.setOpaque(false);
        JLabel demoLbl = new JLabel("Quick Demo Fill:");
        demoLbl.setFont(HavenTheme.FONT_CAPTION_BOLD);
        demoLbl.setForeground(HavenTheme.TEXT_SECONDARY);

        JButton demoStudentBtn = ModernButton.subtle("Demo Student");
        demoStudentBtn.setFont(HavenTheme.FONT_CAPTION);
        demoStudentBtn.addActionListener(e -> {
            userField.setText("student");
            passField.setText("pass123");
        });

        JButton demoOwnerBtn = ModernButton.subtle("Demo Owner");
        demoOwnerBtn.setFont(HavenTheme.FONT_CAPTION);
        demoOwnerBtn.addActionListener(e -> {
            userField.setText("owner");
            passField.setText("pass123");
        });

        JButton demoAdminBtn = ModernButton.subtle("Demo Admin");
        demoAdminBtn.setFont(HavenTheme.FONT_CAPTION);
        demoAdminBtn.addActionListener(e -> {
            userField.setText("admin");
            passField.setText("admin123");
        });

        demoBar.add(demoLbl);
        demoBar.add(demoStudentBtn);
        demoBar.add(demoOwnerBtn);
        demoBar.add(demoAdminBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(demoBar, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Button Bar
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(HavenTheme.BG_CANVAS);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 24, 14, 24)
        ));

        ModernButton exitBtn = ModernButton.subtle("Exit");
        ModernButton registerBtn = ModernButton.secondary("Create Account");
        ModernButton loginBtn = ModernButton.primary("Login");

        exitBtn.addActionListener(e -> System.exit(0));
        registerBtn.addActionListener(e -> openRegisterDialog());
        loginBtn.addActionListener(e -> attemptLogin());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(exitBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(registerBtn);
        rightPanel.add(loginBtn);

        btnPanel.add(leftPanel, BorderLayout.WEST);
        btnPanel.add(rightPanel, BorderLayout.EAST);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void attemptLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authenticatedUser = userDAO.authenticate(username, password);
            dispose();
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterDialog() {
        RegisterDialog regDialog = new RegisterDialog(this, userDAO);
        regDialog.setVisible(true);

        User newUser = regDialog.getRegisteredUser();
        if (newUser != null) {
            this.authenticatedUser = newUser;
            dispose();
        }
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
