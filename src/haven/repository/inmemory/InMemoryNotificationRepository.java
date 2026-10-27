package haven.repository.inmemory;

import haven.model.Notification;
import haven.event.HavenEventBus;
import haven.event.NotificationEvent;
import haven.repository.NotificationRepository;

import java.util.List;
import java.util.Optional;

/**
 * In-Memory &amp; File-backed implementation of {@link NotificationRepository}.
 */
public class InMemoryNotificationRepository implements NotificationRepository {
    private final InMemoryStore store;

    public InMemoryNotificationRepository() {
        this.store = InMemoryStore.getInstance();
    }

    @Override
    public Notification save(Notification entity) {
        if (entity == null) return null;
        Notification saved = store.saveNotification(entity);
        HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.CREATED, saved, saved.getRecipientUsername()));
        return saved;
    }

    @Override
    public Optional<Notification> findById(String id) {
        if (id == null) return Optional.empty();
        return store.getAllNotifications().stream().filter(n -> id.equalsIgnoreCase(n.getId())).findFirst();
    }

    @Override
    public List<Notification> findAll() {
        return store.getAllNotifications();
    }

    @Override
    public boolean deleteById(String id) {
        boolean removed = store.deleteNotification(id);
        if (removed) {
            HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.CLEARED, null, null));
        }
        return removed;
    }

    @Override
    public long count() {
        return store.getAllNotifications().size();
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public List<Notification> findByRecipient(String username) {
        return store.getNotificationsForUser(username);
    }

    @Override
    public int countUnread(String username) {
        return (int) store.getNotificationsForUser(username).stream().filter(n -> !n.isRead()).count();
    }

    @Override
    public boolean markAsRead(String notificationId) {
        boolean ok = store.markNotificationRead(notificationId);
        if (ok) {
            findById(notificationId).ifPresent(n ->
                    HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.READ, n, n.getRecipientUsername()))
            );
        }
        return ok;
    }

    @Override
    public void markAllAsRead(String username) {
        store.markAllNotificationsRead(username);
        HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.READ, null, username));
    }

    @Override
    public int clearAll(String username) {
        int count = store.clearNotificationsForUser(username);
        if (count > 0) {
            HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.CLEARED, null, username));
        }
        return count;
    }
}
