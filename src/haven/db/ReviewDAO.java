package haven.db;

import haven.model.Review;
import haven.repository.RepositoryProvider;
import haven.repository.ReviewRepository;

import java.util.List;

/**
 * Adapter / Facade maintaining backward compatibility for legacy UI calls,
 * while delegating all data access to the decoupled ReviewRepository.
 *
 * Underlying repositories publish typed events via HavenEventBus (Observer Pattern)
 * on any data mutation, enabling reactive UI synchronization across all subscribed panels.
 */
public class ReviewDAO {
    private final ReviewRepository repository;

    public ReviewDAO() {
        this(RepositoryProvider.getReviewRepository());
    }

    public ReviewDAO(ReviewRepository repository) {
        this.repository = repository;
    }

    public void addReview(Review review) {
        repository.save(review);
    }

    public List<Review> getReviewsForAccommodation(String accommodationId) {
        return repository.findByAccommodationId(accommodationId);
    }

    public double getAverageRating(String accommodationId) {
        return repository.getAverageRating(accommodationId);
    }

    public int getReviewCount(String accommodationId) {
        return repository.getReviewCount(accommodationId);
    }

    public boolean hasUserReviewed(String accommodationId, String username) {
        return repository.hasUserReviewed(accommodationId, username);
    }

    public void deleteReview(String reviewId) {
        repository.deleteById(reviewId);
    }
}

