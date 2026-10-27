package haven.event;

/**
 * Functional interface for listening to domain events.
 *
 * @param <E> the specific HavenEvent subtype
 */
@FunctionalInterface
public interface HavenEventListener<E extends HavenEvent> {
    void onEvent(E event);
}
