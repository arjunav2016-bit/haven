package haven.repository;

import haven.model.Review;

import java.util.List;

/**
 * Domain-specific contract for student reviews and rating calculations.
 * Extends the generic Repository interface.
 */
public interface ReviewRepository extends Repository<Review, String> {

    List<Review> findByAccommodationId(String accommodationId);

    /**
     * Convenience alias for findByAccommodationId.
     */
    default List<Review> getReviewsForAccommodation(String accommodationId) {
        return findByAccommodationId(accommodationId);
    }

    double getAverageRating(String accommodationId);

    int getReviewCount(String accommodationId);

    boolean hasUserReviewed(String accommodationId, String username);
}
