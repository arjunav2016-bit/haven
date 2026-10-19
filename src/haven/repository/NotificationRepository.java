package haven.repository;

import haven.model.Notification;
import java.util.List;

/**
 * Domain-specific contract for persistent user notifications.
 * Extends the generic {@link Repository} interface.
 */
public interface NotificationRepository extends Repository<Notification, String> {

    /**
     * Retrieves all notifications for a user, sorted newest first.
     *
     * @param username recipient username
     * @return list of notifications
     */
    List<Notification> findByRecipient(String username);

    /**
     * Returns the count of unread notifications for a user.
     *
     * @param username recipient username
     * @return unread count
     */
    int countUnread(String username);

    /**
     * Marks a specific notification as read.
     *
     * @param notificationId notification ID
     * @return true if updated
     */
    boolean markAsRead(String notificationId);

    /**
     * Marks all notifications for a user as read.
     *
     * @param username recipient username
     */
    void markAllAsRead(String username);

    /**
     * Clears (deletes) all notifications for a user.
     *
     * @param username recipient username
     * @return count of deleted notifications
     */
    int clearAll(String username);
}
