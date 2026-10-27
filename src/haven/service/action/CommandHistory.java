package haven.service.action;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * History manager maintaining undo and redo stacks for administrative actions.
 */
public class CommandHistory {
    private final Deque<AdminCommand> undoStack = new ArrayDeque<>();
    private final Deque<AdminCommand> redoStack = new ArrayDeque<>();

    /**
     * Executes a command and pushes it onto the undo stack, clearing the redo stack.
     *
     * @param command the command to execute
     */
    public synchronized void executeCommand(AdminCommand command) {
        if (command == null) return;
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Undoes the most recently executed command and pushes it onto the redo stack.
     *
     * @return the undone command, or null if undo stack is empty
     */
    public synchronized AdminCommand undo() {
        if (undoStack.isEmpty()) return null;
        AdminCommand cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return cmd;
    }

    /**
     * Re-executes the most recently undone command.
     *
     * @return the redone command, or null if redo stack is empty
     */
    public synchronized AdminCommand redo() {
        if (redoStack.isEmpty()) return null;
        AdminCommand cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return cmd;
    }

    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public synchronized String getLastUndoDescription() {
        return undoStack.isEmpty() ? "" : undoStack.peek().getDescription();
    }

    public synchronized String getLastRedoDescription() {
        return redoStack.isEmpty() ? "" : redoStack.peek().getDescription();
    }

    public synchronized List<AdminCommand> getUndoHistory() {
        return new ArrayList<>(undoStack);
    }

    public synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
