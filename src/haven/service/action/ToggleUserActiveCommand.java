package haven.service.action;

import haven.repository.UserRepository;
import java.util.Date;

/**
 * Concrete Command for activating or deactivating a user account with undo capability.
 */
public class ToggleUserActiveCommand implements AdminCommand {
    private final UserRepository userRepository;
    private final String username;
    private final boolean newActiveState;
    private final Date executedAt;

    public ToggleUserActiveCommand(UserRepository userRepository, String username, boolean newActiveState) {
        this.userRepository = userRepository;
        this.username = username;
        this.newActiveState = newActiveState;
        this.executedAt = new Date();
    }

    @Override
    public void execute() {
        userRepository.setUserActive(username, newActiveState);
    }

    @Override
    public void undo() {
        userRepository.setUserActive(username, !newActiveState);
    }

    @Override
    public String getDescription() {
        return (newActiveState ? "Activate" : "Deactivate") + " user @" + username;
    }

    @Override
    public Date getExecutedAt() {
        return executedAt;
    }
}
