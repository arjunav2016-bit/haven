package haven.pattern.event;

import haven.model.Notification;

/**
 * Domain event published via {@link HavenEventBus} (Observer Pattern)
 * whenever a persistent notification is created or updated.
 */
public class NotificationEvent extends HavenEvent {
    public enum Type {
        CREATED,
        READ,
        CLEARED
    }

    private final Type type;
    private final Notification notification;
    private final String recipientUsername;

    public NotificationEvent(Type type, Notification notification, String recipientUsername) {
        super();
        this.type = type;
        this.notification = notification;
        this.recipientUsername = recipientUsername;
    }

    public Type getType() {
        return type;
    }

    public Notification getNotification() {
        return notification;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }
}
