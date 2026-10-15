package haven.db;

import haven.exception.AuthenticationException;
import haven.model.User;
import haven.repository.RepositoryProvider;
import haven.repository.UserRepository;

import java.util.List;

/**
 * Adapter / Facade maintaining backward compatibility for legacy UI calls,
 * while delegating all data access to the decoupled UserRepository.
 */
public class UserDAO {
    private final UserRepository repository;

    public UserDAO() {
        this(RepositoryProvider.getUserRepository());
    }

    public UserDAO(UserRepository repository) {
        this.repository = repository;
    }

    public boolean userExists(String username) {
        return repository.userExists(username);
    }

    public User registerUser(String username, String password, String role, String fullName, String phone,
                             String collegeName, String genderPreference, double maxBudget) throws Exception {
        if ("OWNER".equalsIgnoreCase(role)) {
            repository.registerOwner(username, password, fullName, phone);
        } else {
            repository.registerStudent(username, password, fullName, phone, collegeName, genderPreference, maxBudget);
        }
        return repository.findById(username).orElse(null);
    }

    public User authenticate(String username, String password) throws AuthenticationException {
        return repository.authenticate(username, password);
    }

    public void updateProfile(String username, String fullName, String phone, String collegeName, String genderPreference, double maxBudget) {
        User u = repository.findById(username).orElse(null);
        if (u != null) {
            u.setFullName(fullName);
            u.setPhone(phone);
            if (u instanceof haven.model.Student) {
                haven.model.Student s = (haven.model.Student) u;
                s.setCollegeName(collegeName);
                s.setGenderPreference(genderPreference);
                s.setMaxBudget(maxBudget);
            }
            repository.updateUserProfile(u);
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            return repository.changePassword(username, oldPassword, newPassword);
        } catch (AuthenticationException e) {
            return false;
        }
    }

    public void toggleWishlist(String username, String accommodationId) {
        repository.toggleWishlist(username, accommodationId);
    }

    public List<String> getWishlist(String username) {
        return repository.getWishlist(username);
    }
}
