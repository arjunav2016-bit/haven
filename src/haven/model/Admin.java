package haven.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete User subclass representing a System Administrator with moderation,
 * user management, and platform-wide analytics privileges.
 */
public class Admin extends User {
    private String department;
    private String accessLevel;

    public Admin(String username, String password, String fullName, String phone) {
        this(username, password, fullName, phone, "Platform Governance", "SUPER_ADMIN", new ArrayList<>());
    }

    public Admin(String username, String password, String fullName, String phone,
                 String department, String accessLevel, List<String> wishlist) {
        super(username, password, "ADMIN", fullName, phone, wishlist, true);
        this.department = (department != null) ? department : "Platform Governance";
        this.accessLevel = (accessLevel != null) ? accessLevel : "SUPER_ADMIN";
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}
