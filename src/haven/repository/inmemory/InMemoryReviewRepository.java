package haven.repository.inmemory;

import haven.db.InMemoryStore;
import haven.model.Review;
import haven.pattern.event.HavenEventBus;
import haven.pattern.event.ReviewEvent;
import haven.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

/**
 * In-Memory &amp; File-backed implementation of {@link ReviewRepository}.
 */
public class InMemoryReviewRepository implements ReviewRepository {
    private final InMemoryStore store;

    public InMemoryReviewRepository() {
        this.store = InMemoryStore.getInstance();
    }

    @Override
    public Review save(Review entity) {
        if (entity == null) return null;
        store.addReview(entity);
        HavenEventBus.getInstance().publish(new ReviewEvent(entity));
        return entity;
    }

    @Override
    public Optional<Review> findById(String id) {
        return store.getReviewById(id);
    }

    @Override
    public List<Review> findAll() {
        return store.getAllReviews();
    }

    @Override
    public boolean deleteById(String id) {
        Optional<Review> existing = store.getReviewById(id);
        boolean deleted = store.deleteReview(id);
        if (deleted) {
            HavenEventBus.getInstance().publish(new ReviewEvent(ReviewEvent.Type.DELETED, existing.orElse(null)));
        }
        return deleted;
    }

    @Override
    public long count() {
        return store.getAllReviews().size();
    }

    @Override
    public boolean existsById(String id) {
        return store.getReviewById(id).isPresent();
    }

    @Override
    public List<Review> findByAccommodationId(String accommodationId) {
        return store.getReviewsForAccommodation(accommodationId);
    }

    @Override
    public double getAverageRating(String accommodationId) {
        return store.getAverageRating(accommodationId);
    }

    @Override
    public int getReviewCount(String accommodationId) {
        return store.getReviewCount(accommodationId);
    }

    @Override
    public boolean hasUserReviewed(String accommodationId, String username) {
        return store.hasUserReviewed(accommodationId, username);
    }
}
