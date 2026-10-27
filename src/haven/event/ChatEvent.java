package haven.event;

import haven.model.ChatMessage;

/**
 * Domain event published via {@link HavenEventBus}
 * whenever a chat message is sent or marked as read.
 */
public class ChatEvent extends HavenEvent {
    public enum Type {
        MESSAGE_SENT,
        MESSAGES_READ
    }

    private final Type type;
    private final ChatMessage message;

    public ChatEvent(Type type, ChatMessage message) {
        super();
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
