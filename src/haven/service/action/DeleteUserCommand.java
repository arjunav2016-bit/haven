package haven.service.action;

import haven.model.User;
import haven.repository.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Concrete Command for deleting a user account with full state backup for undo restoration.
 */
public class DeleteUserCommand implements AdminCommand {
    private final UserRepository userRepository;
    private final String username;
    private User backedUpUser;
    private String backedUpPasswordHash;
    private List<String> backedUpWishlist;
    private final Date executedAt;

    public DeleteUserCommand(UserRepository userRepository, String username) {
        this.userRepository = userRepository;
        this.username = username;
        this.executedAt = new Date();
    }

    @Override
    public void execute() {
        this.backedUpUser = userRepository.findById(username).orElse(null);
        this.backedUpPasswordHash = userRepository.getPasswordHash(username);
        this.backedUpWishlist = new ArrayList<>(userRepository.getWishlist(username));
        userRepository.deleteById(username);
    }

    @Override
    public void undo() {
        if (backedUpUser != null) {
            userRepository.restoreUser(backedUpUser, backedUpPasswordHash, backedUpWishlist);
        }
    }

    @Override
    public String getDescription() {
        return "Delete user @" + username;
    }

    @Override
    public Date getExecutedAt() {
        return executedAt;
    }
}
