package haven.repository.inmemory;

import haven.db.InMemoryStore;
import haven.exception.AuthenticationException;
import haven.model.Student;
import haven.model.User;
import haven.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * In-Memory &amp; File-backed implementation of {@link UserRepository}.
 */
public class InMemoryUserRepository implements UserRepository {
    private final InMemoryStore store;

    public InMemoryUserRepository() {
        this.store = InMemoryStore.getInstance();
    }

    @Override
    public User save(User entity) {
        if (entity == null) return null;
        updateUserProfile(entity);
        return entity;
    }

    @Override
    public Optional<User> findById(String username) {
        if (username == null) return Optional.empty();
        return store.userExists(username) ? Optional.ofNullable(store.getUser(username)) : Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return store.getAllUsers();
    }

    @Override
    public boolean deleteById(String username) {
        return store.deleteUser(username);
    }

    @Override
    public long count() {
        return store.getAllUsers().size();
    }

    @Override
    public boolean existsById(String username) {
        return store.userExists(username);
    }

    @Override
    public boolean userExists(String username) {
        return store.userExists(username);
    }

    @Override
    public User authenticate(String username, String rawPassword) throws AuthenticationException {
        return store.authenticate(username, rawPassword);
    }

    @Override
    public void registerStudent(String username, String rawPassword, String fullName, String phone,
                                String collegeName, String genderPreference, double maxBudget) {
        try {
            store.registerUser(username, rawPassword, "STUDENT", fullName, phone, collegeName, genderPreference, maxBudget);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void registerOwner(String username, String rawPassword, String fullName, String phone) {
        try {
            store.registerUser(username, rawPassword, "OWNER", fullName, phone, null, null, 0);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean updateUserProfile(User user) {
        if (user == null || user.getUsername() == null) return false;
        String college = (user instanceof Student) ? ((Student) user).getCollegeName() : null;
        String gender = (user instanceof Student) ? ((Student) user).getGenderPreference() : null;
        double budget = (user instanceof Student) ? ((Student) user).getMaxBudget() : 0.0;
        store.updateProfile(user.getUsername(), user.getFullName(), user.getPhone(), college, gender, budget);
        store.setUserActive(user.getUsername(), user.isActive());
        return true;
    }

    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) throws AuthenticationException {
        if (!store.changePassword(username, currentPassword, newPassword)) {
            throw new AuthenticationException("Current password verification failed.");
        }
        return true;
    }

    @Override
    public List<String> getWishlist(String username) {
        return store.getWishlist(username);
    }

    @Override
    public boolean toggleWishlist(String username, String accommodationId) {
        store.toggleWishlist(username, accommodationId);
        return store.getWishlist(username).contains(accommodationId);
    }

    @Override
    public boolean setUserActive(String username, boolean active) {
        return store.setUserActive(username, active);
    }

    @Override
    public void restoreUser(User user, String passwordHash, List<String> wishlist) {
        store.restoreUser(user, passwordHash, wishlist);
    }

    @Override
    public String getPasswordHash(String username) {
        return store.getPasswordHash(username);
    }
}
