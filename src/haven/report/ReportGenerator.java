package haven.report;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class defining the structured layout and export workflow for
 * documents and reports across HAVEN.
 */
public abstract class ReportGenerator {
    private static final Logger LOGGER = Logger.getLogger(ReportGenerator.class.getName());
    protected static final String SEPARATOR_DOUBLE = "================================================================================\n";
    protected static final String SEPARATOR_SINGLE = "--------------------------------------------------------------------------------\n";
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    /**
     * Template Method defining the invariant algorithm for report generation.
     * Subclasses customize specific sections without altering the overall document structure.
     *
     * @return formatted plain-text report
     */
    public final String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR_DOUBLE);
        sb.append(buildHeaderBanner());
        sb.append(SEPARATOR_DOUBLE).append("\n");
        sb.append(buildMetadataSection());
        sb.append(buildBodyContent());
        sb.append(buildFooterSection());
        sb.append(SEPARATOR_DOUBLE);
        return sb.toString();
    }

    /**
     * Returns the centered title lines for the top banner.
     */
    protected abstract String buildHeaderBanner();

    /**
     * Returns the reference metadata block (timestamps, reference IDs, user handles).
     */
    protected abstract String buildMetadataSection();

    /**
     * Returns the primary report body content.
     */
    protected abstract String buildBodyContent();

    /**
     * Returns the closing footer or checklist section.
     */
    protected abstract String buildFooterSection();

    protected String formatNow() {
        return dateFormat.format(new Date());
    }

    /**
     * Exports the generated report to disk via a Swing {@link JFileChooser} save dialog.
     */
    public void exportWithDialog(Component parent, String defaultFileName, String dialogTitle) {
        exportTextToFileWithDialog(parent, defaultFileName, generate(), dialogTitle);
    }

    public static void exportTextToFileWithDialog(Component parent, String defaultFileName, String content, String dialogTitle) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(dialogTitle);
        chooser.setSelectedFile(new File(defaultFileName));

        int result = chooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(selectedFile)) {
                writer.write(content);
                int openChoice = JOptionPane.showConfirmDialog(
                        parent,
                        "Document saved successfully to:\n" + selectedFile.getAbsolutePath() + "\n\nWould you like to open it now?",
                        "File Saved Successfully",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );

                if (openChoice == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(selectedFile);
                    } catch (IOException | RuntimeException ex) {
                        LOGGER.log(Level.WARNING, "Could not open file automatically: " + selectedFile.getAbsolutePath(), ex);
                        JOptionPane.showMessageDialog(parent, "Could not open file automatically: " + ex.getMessage(), "Notice", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error saving report file: " + selectedFile.getAbsolutePath(), ex);
                JOptionPane.showMessageDialog(parent, "Error saving file: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
