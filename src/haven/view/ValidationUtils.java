package haven.view;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Predicate;

/**
 * ValidationUtils - Reusable inline form validation helper for Swing text inputs.
 * Highlights invalid fields with a rose border and updates an inline feedback label.
 */
public final class ValidationUtils {
    private ValidationUtils() {}

    public static CompoundBorder createValidBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    public static CompoundBorder createErrorBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(HavenTheme.DANGER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    public static void attachValidator(JTextField field, Predicate<String> validator, String errorMsg, JLabel feedbackLabel) {
        DocumentListener dl = new DocumentListener() {
            private void validate() {
                String text = field.getText() != null ? field.getText().trim() : "";
                boolean valid = validator.test(text);
                field.setBorder(valid ? createValidBorder() : createErrorBorder());
                if (feedbackLabel != null) {
                    if (!valid && !text.isEmpty()) {
                        feedbackLabel.setText(errorMsg);
                        feedbackLabel.setForeground(HavenTheme.DANGER);
                    } else {
                        feedbackLabel.setText(" ");
                    }
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { validate(); }
            @Override public void removeUpdate(DocumentEvent e) { validate(); }
            @Override public void changedUpdate(DocumentEvent e) { validate(); }
        };
        field.getDocument().addDocumentListener(dl);
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isPositiveDouble(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Double.parseDouble(text.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInt(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Integer.parseInt(text.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPhone(String text) {
        return text != null && text.trim().matches("^[0-9+ ,-]{7,20}$");
    }

    public static boolean isValidUsername(String text) {
        return text != null && text.trim().matches("^[a-zA-Z0-9_]{3,30}$");
    }

    public static boolean isValidPassword(String text) {
        return text != null && text.trim().length() >= 4;
    }
}
