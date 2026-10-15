package haven.pattern.command;

import java.util.Date;

/**
 * Command Pattern interface encapsulating an administrative action as an executable
 * and reversible object supporting full Undo/Redo history.
 */
public interface AdminCommand {

    /**
     * Executes the command operation.
     */
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

    /**
     * Returns the timestamp when the command was created or executed.
     *
     * @return execution timestamp
     */
    Date getExecutedAt();
}
