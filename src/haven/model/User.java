package haven.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Base User domain entity supporting Student, Owner, and Admin roles.
 */
public abstract class User {
    private String username;
    private String password;
    private String role; // "STUDENT", "OWNER", or "ADMIN"
    private String fullName;
    private String phone;
    private List<String> wishlist;
    private boolean active = true;

    public User(String username, String password, String role, String fullName, String phone) {
        this(username, password, role, fullName, phone, new ArrayList<>(), true);
    }

    public User(String username, String password, String role, String fullName, String phone, List<String> wishlist) {
        this(username, password, role, fullName, phone, wishlist, true);
    }

    public User(String username, String password, String role, String fullName, String phone, List<String> wishlist, boolean active) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.wishlist = (wishlist != null) ? new ArrayList<>(wishlist) : new ArrayList<>();
        this.active = active;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public List<String> getWishlist() {
        return (wishlist != null) ? new ArrayList<>(wishlist) : new ArrayList<>();
    }
    public boolean isActive() { return active; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setActive(boolean active) { this.active = active; }

    public Role getRoleEnum() {
        return Role.fromString(role);
    }

    public boolean isOwner() {
        return Role.OWNER.matches(role);
    }

    public boolean isStudent() {
        return Role.STUDENT.matches(role);
    }

    public boolean isAdmin() {
        return Role.ADMIN.matches(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;
        User user = (User) o;
        return username != null && username.equalsIgnoreCase(user.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.toLowerCase().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                ", active=" + active +
                '}';
    }
}