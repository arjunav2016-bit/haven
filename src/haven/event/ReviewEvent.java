package haven.event;

import haven.model.Review;

/**
 * Event published when a verified review is posted for an accommodation.
 */
public class ReviewEvent extends HavenEvent {

    public enum Type {
        CREATED,
        DELETED
    }

    private final Type type;
    private final Review review;

    public ReviewEvent(Type type, Review review) {
        this.type = (type != null) ? type : Type.CREATED;
        this.review = review;
    }

    public ReviewEvent(Review review) {
        this(Type.CREATED, review);
    }

    public Type getType() {
        return type;
    }

    public Review getReview() {
        return review;
    }
}
