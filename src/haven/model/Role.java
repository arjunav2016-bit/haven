package haven.model;

/**
 * Domain Enum representing authenticated user authorization roles.
 * Replaces hardcoded string role identifiers across the platform.
 */
public enum Role {
    STUDENT,
    OWNER,
    ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return STUDENT;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STUDENT;
        }
    }

    public boolean matches(String roleStr) {
        return name().equalsIgnoreCase(roleStr);
    }
}
