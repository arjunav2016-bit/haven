package haven;

import haven.model.User;
import haven.pattern.MongoDBConnection;
import haven.repository.RepositoryProvider;
import haven.view.LoginDialog;
import haven.view.MainFrame;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        // Enable subpixel anti-aliasing for text rendering across Swing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "Could not set system look-and-feel", ex);
        }

        SwingUtilities.invokeLater(() -> {
            // Background initialization worker
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return MongoDBConnection.getInstance().testConnection();
                }

                @Override
                protected void done() {
                    boolean mongoOnline = false;
                    try {
                        mongoOnline = get();
                    } catch (Exception ex) {
                        LOGGER.log(Level.FINE, "Error checking MongoDB connection", ex);
                    }

                    if (!mongoOnline) {
                        LOGGER.info("MongoDB offline on localhost:27017. Prompting for offline mode...");
                        int choice = JOptionPane.showOptionDialog(null,
                                "<html><body style='width: 340px; padding: 5px;'>" +
                                "<h3 style='color: #4F46E5; margin-top: 0;'>MongoDB Offline on localhost:27017</h3>" +
                                "<p>HAVEN was unable to connect to a local MongoDB server.</p>" +
                                "<p><b>Would you like to launch in Offline Store Mode?</b></p>" +
                                "<p style='color: #475569; font-size: 11px; line-height: 1.4;'>" +
                                "• Complete feature set active using persistent JSON file store.<br>" +
                                "• Pre-loaded with SCTCE hostels, test accounts, and reviews.<br>" +
                                "• Runs locally without requiring an active mongod service." +
                                "</p></body></html>",
                                "Database Notice - HAVEN",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new Object[]{"Launch in Offline Store Mode", "Exit Application"},
                                "Launch in Offline Store Mode");

                        if (choice == 0) {
                            MongoDBConnection.getInstance().setOfflineMode(true);
                        } else {
                            System.exit(0);
                            return;
                        }
                    } else {
                        LOGGER.info("Connected to MongoDB (localhost:27017).");
                    }

                    RepositoryProvider.initialize();
                    LOGGER.info("Abstract Factory & Repositories initialized.");

                    LOGGER.info("Opening Login Portal window...");
                    LoginDialog login = new LoginDialog(null);
                    login.toFront();
                    login.requestFocus();
                    login.setVisible(true);

                    User authenticatedUser = login.getAuthenticatedUser();
                    if (authenticatedUser != null) {
                        LOGGER.info("Logged in as: " + authenticatedUser.getUsername() + " [" + authenticatedUser.getRole() + "]");
                        MainFrame frame = new MainFrame(authenticatedUser);
                        frame.toFront();
                        frame.requestFocus();
                        frame.setVisible(true);
                    } else {
                        LOGGER.info("Login dismissed. Exiting application.");
                        System.exit(0);
                    }
                }
            }.execute();
        });
    }
}
