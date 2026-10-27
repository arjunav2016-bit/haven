package haven.db;

import haven.exception.AuthenticationException;
import haven.model.User;
import haven.repository.RepositoryProvider;
import haven.repository.UserRepository;
import haven.service.AuthService;
import haven.service.ServiceProvider;

import java.util.List;

/**
 * Adapter / Facade maintaining backward compatibility for legacy UI calls,
 * while delegating all data access through the decoupled Service Layer
 * ({@link haven.service.AuthService}) and underlying {@link UserRepository}.
 */
public class UserDAO {
    private final UserRepository repository;
    private final AuthService authService;

    public UserDAO() {
        this(RepositoryProvider.getUserRepository(), ServiceProvider.getAuthService());
    }

    public UserDAO(UserRepository repository) {
        this(repository, new AuthService(repository));
    }

    public UserDAO(UserRepository repository, AuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    public boolean userExists(String username) {
        return authService.userExists(username);
    }

    public User registerUser(String username, String password, String role, String fullName, String phone,
                             String collegeName, String genderPreference, double maxBudget) throws Exception {
        if ("OWNER".equalsIgnoreCase(role)) {
            authService.registerOwner(username, password, fullName, phone);
        } else {
            authService.registerStudent(username, password, fullName, phone, collegeName, genderPreference, maxBudget);
        }
        return authService.getUser(username).orElse(null);
    }

    public User authenticate(String username, String password) throws AuthenticationException {
        return authService.authenticate(username, password);
    }

    public void updateProfile(String username, String fullName, String phone, String collegeName, String genderPreference, double maxBudget) {
        User u = authService.getUser(username).orElse(null);
        if (u != null) {
            u.setFullName(fullName);
            u.setPhone(phone);
            if (u instanceof haven.model.Student) {
                haven.model.Student s = (haven.model.Student) u;
                s.setCollegeName(collegeName);
                s.setGenderPreference(genderPreference);
                s.setMaxBudget(maxBudget);
            }
            authService.updateProfile(u);
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            return authService.changePassword(username, oldPassword, newPassword);
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

    public AuthService getAuthService() {
        return authService;
    }
}
