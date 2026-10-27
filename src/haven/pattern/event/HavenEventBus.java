package haven.pattern.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe EventBus implementing the Observer Pattern for HAVEN.
 * Decouples views and data services by allowing reactive event publishing and subscription.
 */
public class HavenEventBus {
    private static final Logger LOGGER = Logger.getLogger(HavenEventBus.class.getName());
    private static volatile HavenEventBus instance;

    private final Map<Class<? extends HavenEvent>, List<HavenEventListener<?>>> listeners = new ConcurrentHashMap<>();

    private HavenEventBus() {}

    public static HavenEventBus getInstance() {
        if (instance == null) {
            synchronized (HavenEventBus.class) {
                if (instance == null) {
                    instance = new HavenEventBus();
                }
            }
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.listeners.clear();
        }
        instance = null;
    }

    /**
     * Subscribes a listener to a specific event type.
     */
    public <E extends HavenEvent> void subscribe(Class<E> eventClass, HavenEventListener<E> listener) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Unsubscribes a listener from an event type.
     */
    public <E extends HavenEvent> void unsubscribe(Class<E> eventClass, HavenEventListener<E> listener) {
        List<HavenEventListener<?>> list = listeners.get(eventClass);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Publishes an event synchronously to all registered listeners.
     */
    @SuppressWarnings("unchecked")
    public void publish(HavenEvent event) {
        if (event == null) return;

        for (Map.Entry<Class<? extends HavenEvent>, List<HavenEventListener<?>>> entry : listeners.entrySet()) {
            if (entry.getKey().isAssignableFrom(event.getClass())) {
                for (HavenEventListener<?> rawListener : entry.getValue()) {
                    try {
                        ((HavenEventListener<HavenEvent>) rawListener).onEvent(event);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error in listener execution for event " + event.getClass().getSimpleName(), e);
                    }
                }
            }
        }
    }
}
