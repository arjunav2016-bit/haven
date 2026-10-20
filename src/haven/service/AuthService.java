package haven.service;

import haven.exception.AuthenticationException;
import haven.model.User;
import haven.repository.RepositoryProvider;
import haven.repository.UserRepository;

import java.util.Optional;

/**
 * Service Layer component for user authentication, registration, and account governance.
 * Coordinates with {@link UserRepository} and enforces salted PBKDF2 cryptography.
 */
public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this(RepositoryProvider.getUserRepository());
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String username, String password) throws AuthenticationException {
        return userRepository.authenticate(username, password);
    }

    public boolean userExists(String username) {
        return userRepository.userExists(username);
    }

    public Optional<User> getUser(String username) {
        return userRepository.findById(username);
    }

    public void registerStudent(String username, String password, String fullName, String phone,
                                String collegeName, String genderPreference, double maxBudget) throws Exception {
        userRepository.registerStudent(username, password, fullName, phone, collegeName, genderPreference, maxBudget);
    }

    public void registerOwner(String username, String password, String fullName, String phone) throws Exception {
        userRepository.registerOwner(username, password, fullName, phone);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        return userRepository.changePassword(username, oldPassword, newPassword);
    }

    public void updateProfile(User user) {
        userRepository.updateUserProfile(user);
    }
}
