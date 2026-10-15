package haven.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import haven.model.Notification;
import haven.pattern.MongoDBConnection;
import haven.pattern.event.HavenEventBus;
import haven.pattern.event.NotificationEvent;
import haven.repository.NotificationRepository;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB concrete implementation of NotificationRepository.
 */
public class MongoNotificationRepository implements NotificationRepository {
    private final MongoCollection<Document> collection;

    public MongoNotificationRepository() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("notifications");
    }

    @Override
    public Notification save(Notification entity) {
        if (entity == null) return null;
        collection.deleteOne(Filters.eq("id", entity.getId()));
        Document doc = new Document("id", entity.getId())
                .append("recipientUsername", entity.getRecipientUsername())
                .append("title", entity.getTitle())
                .append("message", entity.getMessage())
                .append("type", entity.getType() != null ? entity.getType().name() : "SYSTEM")
                .append("linkedEntityId", entity.getLinkedEntityId() != null ? entity.getLinkedEntityId() : "")
                .append("timestamp", entity.getTimestamp())
                .append("read", entity.isRead());
        collection.insertOne(doc);
        HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.CREATED, entity, entity.getRecipientUsername()));
        return entity;
    }

    @Override
    public Optional<Notification> findById(String id) {
        if (id == null) return Optional.empty();
        Document doc = collection.find(Filters.eq("id", id.trim())).first();
        return doc != null ? Optional.of(mapDocToNotification(doc)) : Optional.empty();
    }

    @Override
    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().sort(Sorts.descending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToNotification(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        return collection.deleteOne(Filters.eq("id", id.trim())).getDeletedCount() > 0;
    }

    @Override
    public long count() {
        return collection.countDocuments();
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        return collection.countDocuments(Filters.eq("id", id.trim())) > 0;
    }

    @Override
    public List<Notification> findByRecipient(String username) {
        List<Notification> list = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) return list;
        try (MongoCursor<Document> cursor = collection.find(Filters.eq("recipientUsername", username.trim()))
                .sort(Sorts.descending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToNotification(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public int countUnread(String username) {
        if (username == null || username.trim().isEmpty()) return 0;
        return (int) collection.countDocuments(Filters.and(
                Filters.eq("recipientUsername", username.trim()),
                Filters.eq("read", false)
        ));
    }

    @Override
    public boolean markAsRead(String notificationId) {
        if (notificationId == null) return false;
        boolean ok = collection.updateOne(Filters.eq("id", notificationId.trim()), Updates.set("read", true)).getModifiedCount() > 0;
        if (ok) {
            findById(notificationId).ifPresent(n ->
                    HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.READ, n, n.getRecipientUsername()))
            );
        }
        return ok;
    }

    @Override
    public void markAllAsRead(String username) {
        if (username == null) return;
        collection.updateMany(Filters.eq("recipientUsername", username.trim()), Updates.set("read", true));
        HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.READ, null, username));
    }

    private Notification mapDocToNotification(Document doc) {
        Long ts = doc.getLong("timestamp");
        String typeStr = doc.getString("type");
        Notification.Type type = Notification.Type.SYSTEM;
        if (typeStr != null) {
            try {
                type = Notification.Type.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new Notification(
                doc.getString("id"),
                doc.getString("recipientUsername"),
                doc.getString("title"),
                doc.getString("message"),
                type,
                doc.getString("linkedEntityId"),
                ts != null ? ts : System.currentTimeMillis(),
                doc.getBoolean("read", false)
        );
    }
}
