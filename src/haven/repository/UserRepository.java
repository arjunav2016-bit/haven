package haven.repository;

import haven.exception.AuthenticationException;
import haven.model.User;

import java.util.List;

/**
 * Domain-specific contract for User authentication, profile, wishlist, and admin management.
 * Extends the generic Repository interface.
 */
public interface UserRepository extends Repository<User, String> {

    boolean userExists(String username);

    /**
     * Authenticates user credentials using SHA-256 password matching.
     *
     * @param username username to authenticate
     * @param rawPassword plaintext password
     * @return authenticated User instance (with password field stripped)
     * @throws AuthenticationException if username not found, account deactivated, or password incorrect
     */
    User authenticate(String username, String rawPassword) throws AuthenticationException;

    void registerStudent(String username, String rawPassword, String fullName, String phone,
                         String collegeName, String genderPreference, double maxBudget);

    void registerOwner(String username, String rawPassword, String fullName, String phone);

    boolean updateUserProfile(User user);

    boolean changePassword(String username, String currentPassword, String newPassword) throws AuthenticationException;

    List<String> getWishlist(String username);

    /**
     * Toggles an accommodation ID in the user's wishlist (adds if absent, removes if present).
     *
     * @return true if added, false if removed
     */
    boolean toggleWishlist(String username, String accommodationId);

    default boolean setUserActive(String username, boolean active) {
        User u = findById(username).orElse(null);
        if (u != null) {
            u.setActive(active);
            return updateUserProfile(u);
        }
        return false;
    }

    /**
     * Restores a previously deleted user account (used during administrative undo).
     */
    default void restoreUser(User user, String passwordHash, List<String> wishlist) {
        save(user);
    }

    /**
     * Retrieves the stored password hash for backup prior to deletion.
     */
    default String getPasswordHash(String username) {
        return null;
    }
}
