package haven.repository.inmemory;

import haven.model.ChatMessage;
import haven.model.Notification;
import haven.event.ChatEvent;
import haven.event.HavenEventBus;
import haven.event.NotificationEvent;
import haven.repository.MessageRepository;

import java.util.List;
import java.util.Optional;

/**
 * In-Memory &amp; File-backed implementation of {@link MessageRepository}.
 * Publishes {@link ChatEvent} and creates recipient {@link Notification} via {@link HavenEventBus}.
 */
public class InMemoryMessageRepository implements MessageRepository {
    private final InMemoryStore store;

    public InMemoryMessageRepository() {
        this.store = InMemoryStore.getInstance();
    }

    @Override
    public ChatMessage save(ChatMessage entity) {
        if (entity == null) return null;
        ChatMessage saved = store.saveMessage(entity);
        HavenEventBus.getInstance().publish(new ChatEvent(ChatEvent.Type.MESSAGE_SENT, saved));

        // Create persistent notification for recipient
        if (saved.getReceiverUsername() != null && !saved.getReceiverUsername().isEmpty()) {
            Notification n = new Notification(
                    saved.getReceiverUsername(),
                    "New Message from @" + saved.getSenderUsername(),
                    saved.getAccommodationName() + ": \"" + saved.getContent() + "\"",
                    Notification.Type.CHAT,
                    saved.getAccommodationId()
            );
            store.saveNotification(n);
            HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.CREATED, n, saved.getReceiverUsername()));
        }
        return saved;
    }

    @Override
    public Optional<ChatMessage> findById(String id) {
        if (id == null) return Optional.empty();
        return store.getAllMessages().stream().filter(m -> id.equalsIgnoreCase(m.getId())).findFirst();
    }

    @Override
    public List<ChatMessage> findAll() {
        return store.getAllMessages();
    }

    @Override
    public boolean deleteById(String id) {
        return false;
    }

    @Override
    public long count() {
        return store.getAllMessages().size();
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public List<ChatMessage> findByEnquiryId(String enquiryId) {
        return store.getMessagesByEnquiryId(enquiryId);
    }

    @Override
    public List<ChatMessage> findConversation(String accommodationId, String userA, String userB) {
        return store.getConversation(accommodationId, userA, userB);
    }

    @Override
    public List<ChatMessage> findByParticipant(String username) {
        return store.getMessagesByParticipant(username);
    }

    @Override
    public int markConversationAsRead(String accommodationId, String senderUsername, String receiverUsername) {
        int updated = store.markConversationAsRead(accommodationId, senderUsername, receiverUsername);
        if (updated > 0) {
            HavenEventBus.getInstance().publish(new ChatEvent(ChatEvent.Type.MESSAGES_READ, null));
        }
        return updated;
    }
}
