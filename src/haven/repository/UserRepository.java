package haven.repository;

import haven.exception.AuthenticationException;
import haven.model.User;

import java.util.List;

/**
 * Domain-specific contract for User authentication, profile, wishlist, and admin management.
 * Extends the generic Repository interface.
 */
public interface UserRepository extends Repository<User, String> {

    /**
     * Checks if a username already exists.
     */
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

    /**
     * Registers a student user into the repository.
     */
    void registerStudent(String username, String rawPassword, String fullName, String phone,
                         String collegeName, String genderPreference, double maxBudget);

    /**
     * Registers an owner user into the repository.
     */
    void registerOwner(String username, String rawPassword, String fullName, String phone);

    /**
     * Updates an existing user's profile details.
     */
    boolean updateUserProfile(User user);

    /**
     * Verifies the current password and securely updates to a new password.
     */
    boolean changePassword(String username, String currentPassword, String newPassword) throws AuthenticationException;

    /**
     * Retrieves the wishlist accommodation IDs for a student user.
     */
    List<String> getWishlist(String username);

    /**
     * Toggles an accommodation ID in the user's wishlist (adds if absent, removes if present).
     *
     * @return true if added, false if removed
     */
    boolean toggleWishlist(String username, String accommodationId);

    /**
     * Activates or deactivates a user account (Admin operation).
     */
    default boolean setUserActive(String username, boolean active) {
        User u = findById(username).orElse(null);
        if (u != null) {
            u.setActive(active);
            return updateUserProfile(u);
        }
        return false;
    }

    /**
     * Restores a previously deleted user account (used by Command Pattern undo).
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
