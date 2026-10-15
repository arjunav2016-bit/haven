package haven;

import haven.model.User;
import haven.pattern.MongoDBConnection;
import haven.repository.RepositoryProvider;
import haven.view.LoginDialog;
import haven.view.MainFrame;
import haven.view.SplashScreenWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Enable subpixel anti-aliasing for text rendering across Swing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            System.out.println("[HAVEN] Launching HAVEN Desktop Platform...");
            SplashScreenWindow splash = new SplashScreenWindow();
            splash.setVisible(true);
            splash.toFront();

            SwingWorker<Void, ProgressStep> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish(new ProgressStep(25, "Probing MongoDB Server on localhost:27017..."));
                    Thread.sleep(220);

                    boolean mongoOnline = MongoDBConnection.getInstance().testConnection();

                    if (!mongoOnline) {
                        System.out.println("[HAVEN] MongoDB offline on localhost:27017. Prompting for offline mode...");
                        final int[] choice = new int[1];
                        SwingUtilities.invokeAndWait(() -> {
                            splash.setVisible(false);
                            choice[0] = JOptionPane.showOptionDialog(null,
                                    "<html><body style='width: 340px; padding: 5px;'>" +
                                    "<h3 style='color: #4F46E5; margin-top: 0;'>MongoDB Offline on localhost:27017</h3>" +
                                    "<p>HAVEN was unable to connect to a local MongoDB server.</p>" +
                                    "<p><b>Would you like to launch in Offline Store Mode?</b></p>" +
                                    "<p style='color: #475569; font-size: 11px; line-height: 1.4;'>" +
                                    "• Complete feature set active using persistent JSON file store.<br>" +
                                    "• Pre-loaded with SCTCE hostels, test accounts, and reviews.<br>" +
                                    "• Ideal for lab evaluations and viva demonstrations." +
                                    "</p></body></html>",
                                    "Database Notice - HAVEN",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{"Launch in Offline Store Mode", "Exit Application"},
                                    "Launch in Offline Store Mode");
                        });

                        if (choice[0] == 0) {
                            MongoDBConnection.getInstance().setOfflineMode(true);
                            SwingUtilities.invokeAndWait(() -> splash.setVisible(true));
                        } else {
                            SwingUtilities.invokeLater(splash::dispose);
                            System.exit(0);
                            return null;
                        }
                    } else {
                        System.out.println("[HAVEN] Connected to MongoDB (localhost:27017).");
                    }

                    publish(new ProgressStep(65, "Initializing Abstract Factory & Domain Repositories..."));
                    RepositoryProvider.initialize();
                    Thread.sleep(220);
                    publish(new ProgressStep(100, "Ready — Launching HAVEN Portal..."));
                    Thread.sleep(180);
                    System.out.println("[HAVEN] Abstract Factory & Repositories initialized.");
                    return null;
                }

                @Override
                protected void process(java.util.List<ProgressStep> chunks) {
                    if (!chunks.isEmpty()) {
                        ProgressStep last = chunks.get(chunks.size() - 1);
                        splash.setProgress(last.percent, last.message);
                    }
                }

                @Override
                protected void done() {
                    splash.setVisible(false);
                    splash.dispose();

                    System.out.println("[HAVEN] Opening Login Portal window on your screen...");
                    LoginDialog login = new LoginDialog(null);
                    login.toFront();
                    login.requestFocus();
                    login.setVisible(true);

                    User authenticatedUser = login.getAuthenticatedUser();
                    if (authenticatedUser != null) {
                        System.out.println("[HAVEN] Logged in as: " + authenticatedUser.getUsername() + " [" + authenticatedUser.getRole() + "]");
                        MainFrame frame = new MainFrame(authenticatedUser);
                        frame.toFront();
                        frame.requestFocus();
                        frame.setVisible(true);
                    } else {
                        System.out.println("[HAVEN] Login dismissed. Exiting application.");
                        System.exit(0);
                    }
                }
            };

            worker.execute();
        });
    }

    private static class ProgressStep {
        final int percent;
        final String message;

        ProgressStep(int percent, String message) {
            this.percent = percent;
            this.message = message;
        }
    }
}
