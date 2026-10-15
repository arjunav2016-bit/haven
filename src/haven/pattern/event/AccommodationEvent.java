package haven.pattern.event;

import haven.model.Accommodation;

/**
 * Event published when an accommodation is created, updated, or vacancies change.
 */
public class AccommodationEvent extends HavenEvent {

    public enum Type {
        CREATED,
        UPDATED,
        VACANCY_CHANGED,
        DELETED
    }

    private final Type type;
    private final Accommodation accommodation;

    public AccommodationEvent(Type type, Accommodation accommodation) {
        this.type = type;
        this.accommodation = accommodation;
    }

    public Type getType() {
        return type;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }
}
