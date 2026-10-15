package haven.model;

/**
 * Domain Enum representing accommodation moderation status lifecycle.
 * Replaces raw string status literals across filtering and admin workflows.
 */
public enum ModerationStatus {
    APPROVED,
    PENDING,
    FLAGGED,
    REJECTED;

    public static ModerationStatus fromString(String statusStr) {
        if (statusStr == null) return APPROVED;
        try {
            return ModerationStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return APPROVED;
        }
    }

    public boolean matches(String statusStr) {
        return name().equalsIgnoreCase(statusStr);
    }
}
