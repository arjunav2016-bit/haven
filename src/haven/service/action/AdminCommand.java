package haven.service.action;

import java.util.Date;

/**
 * Encapsulates an administrative action as an executable and reversible
 * operation supporting undo and redo history.
 */
public interface AdminCommand {

    void execute();

    /**
     * Reverses the effects of a previously executed command.
     */
    void undo();

    /**
     * Returns a human-readable summary of the command for audit logs and UI display.
     *
     * @return command description
     */
    String getDescription();

    Date getExecutedAt();
}
