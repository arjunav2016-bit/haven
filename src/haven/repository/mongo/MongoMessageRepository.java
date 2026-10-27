package haven.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import haven.model.ChatMessage;
import haven.model.Notification;
import haven.pattern.MongoDBConnection;
import haven.pattern.event.ChatEvent;
import haven.pattern.event.HavenEventBus;
import haven.repository.MessageRepository;
import haven.repository.RepositoryProvider;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MongoMessageRepository.java
 * CST 205 OOP Lab Project - MongoDB Data Access for Chat Messaging
 *
 * Handles persistent message storage in MongoDB collection 'messages'.
 * Features:
 * - Thread retrieval sorted chronologically by timestamp
 * - Conversation resolution between student & property warden/owner
 * - Fires ChatEvent on HavenEventBus so open chat windows update in real-time
 * - Creates a Notification record so recipient sees unread badge count
 */
public class MongoMessageRepository implements MessageRepository {
    private static final Logger LOGGER = Logger.getLogger(MongoMessageRepository.class.getName());
    private final MongoCollection<Document> collection;

    public MongoMessageRepository() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("messages");
    }

    @Override
    public ChatMessage save(ChatMessage entity) {
        if (entity == null) return null;
        Document doc = new Document("id", entity.getId())
                .append("enquiryId", entity.getEnquiryId() != null ? entity.getEnquiryId() : "")
                .append("accommodationId", entity.getAccommodationId() != null ? entity.getAccommodationId() : "")
                .append("accommodationName", entity.getAccommodationName() != null ? entity.getAccommodationName() : "")
                .append("senderUsername", entity.getSenderUsername())
                .append("receiverUsername", entity.getReceiverUsername())
                .append("content", entity.getContent())
                .append("timestamp", entity.getTimestamp())
                .append("read", entity.isRead());

        collection.insertOne(doc);
        HavenEventBus.getInstance().publish(new ChatEvent(ChatEvent.Type.MESSAGE_SENT, entity));

        // Automatically notify the recipient
        if (entity.getReceiverUsername() != null && !entity.getReceiverUsername().trim().isEmpty()) {
            try {
                Notification notif = new Notification(
                        entity.getReceiverUsername(),
                        "New Message from @" + entity.getSenderUsername(),
                        entity.getAccommodationName() + ": \"" + entity.getContent() + "\"",
                        Notification.Type.CHAT,
                        entity.getAccommodationId()
                );
                RepositoryProvider.getNotificationRepository().save(notif);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Failed to send chat notification to " + entity.getReceiverUsername(), e);
            }
        }
        return entity;
    }

    @Override
    public Optional<ChatMessage> findById(String id) {
        if (id == null) return Optional.empty();
        Document doc = collection.find(Filters.eq("id", id.trim())).first();
        return doc != null ? Optional.of(mapDocToMessage(doc)) : Optional.empty();
    }

    @Override
    public List<ChatMessage> findAll() {
        List<ChatMessage> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().sort(Sorts.ascending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToMessage(cursor.next()));
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
    public List<ChatMessage> findByEnquiryId(String enquiryId) {
        List<ChatMessage> list = new ArrayList<>();
        if (enquiryId == null || enquiryId.trim().isEmpty()) return list;
        try (MongoCursor<Document> cursor = collection.find(Filters.eq("enquiryId", enquiryId.trim()))
                .sort(Sorts.ascending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToMessage(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public List<ChatMessage> findConversation(String accommodationId, String userA, String userB) {
        List<ChatMessage> list = new ArrayList<>();
        if (userA == null || userB == null) return list;

        List<Bson> filters = new ArrayList<>();
        if (accommodationId != null && !accommodationId.trim().isEmpty()) {
            filters.add(Filters.eq("accommodationId", accommodationId.trim()));
        }

        // Resolve whether either party has a privileged role (OWNER/ADMIN) via role lookup
        boolean isPrivilegedConversation = isPrivilegedUser(userA) || isPrivilegedUser(userB);

        Bson query = filters.isEmpty() ? new Document() : Filters.and(filters);
        try (MongoCursor<Document> cursor = collection.find(query).sort(Sorts.ascending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                ChatMessage m = mapDocToMessage(cursor.next());
                boolean directMatch = (m.getSenderUsername().equalsIgnoreCase(userA) && m.getReceiverUsername().equalsIgnoreCase(userB))
                        || (m.getSenderUsername().equalsIgnoreCase(userB) && m.getReceiverUsername().equalsIgnoreCase(userA));
                if (directMatch) {
                    list.add(m);
                } else if (isPrivilegedConversation) {
                    String studentUser = isPrivilegedUser(userA) ? userB : userA;
                    boolean aliasMatch = studentUser.equalsIgnoreCase(m.getSenderUsername()) || studentUser.equalsIgnoreCase(m.getReceiverUsername());
                    if (aliasMatch) {
                        list.add(m);
                    }
                }
            }
        }
        return list;
    }

    /** Checks whether a username belongs to a user with a privileged role (OWNER or ADMIN). */
    private boolean isPrivilegedUser(String username) {
        if (username == null) return false;
        return RepositoryProvider.getUserRepository().findById(username)
                .map(u -> u.getRoleEnum().isPrivileged())
                .orElse(false);
    }

    @Override
    public List<ChatMessage> findByParticipant(String username) {
        List<ChatMessage> list = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) return list;
        Bson filter = Filters.or(
                Filters.eq("senderUsername", username.trim()),
                Filters.eq("receiverUsername", username.trim())
        );
        try (MongoCursor<Document> cursor = collection.find(filter).sort(Sorts.descending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToMessage(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public int markConversationAsRead(String accommodationId, String senderUsername, String receiverUsername) {
        if (receiverUsername == null) return 0;
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq("receiverUsername", receiverUsername.trim()));
        if (accommodationId != null && !accommodationId.trim().isEmpty()) {
            filters.add(Filters.eq("accommodationId", accommodationId.trim()));
        }
        if (senderUsername != null && !senderUsername.trim().isEmpty()) {
            filters.add(Filters.eq("senderUsername", senderUsername.trim()));
        }
        int modified = (int) collection.updateMany(Filters.and(filters), Updates.set("read", true)).getModifiedCount();
        if (modified > 0) {
            HavenEventBus.getInstance().publish(new ChatEvent(ChatEvent.Type.MESSAGES_READ, null));
        }
        return modified;
    }

    private ChatMessage mapDocToMessage(Document doc) {
        Long ts = doc.getLong("timestamp");
        return new ChatMessage(
                doc.getString("id"),
                doc.getString("senderUsername"),
                doc.getString("receiverUsername"),
                doc.getString("accommodationId"),
                doc.getString("accommodationName"),
                doc.getString("enquiryId"),
                doc.getString("content"),
                ts != null ? ts : System.currentTimeMillis(),
                doc.getBoolean("read", false)
        );
    }
}
