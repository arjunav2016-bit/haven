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

    int countUnread(String username);

    boolean markAsRead(String notificationId);

    void markAllAsRead(String username);

    int clearAll(String username);
}
