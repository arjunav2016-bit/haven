package haven.repository;

import haven.model.Review;

import java.util.List;

/**
 * Domain-specific contract for student reviews and rating calculations.
 * Extends the generic Repository interface.
 */
public interface ReviewRepository extends Repository<Review, String> {

    /**
     * Finds all reviews for a specific accommodation.
     */
    List<Review> findByAccommodationId(String accommodationId);

    /**
     * Convenience alias for findByAccommodationId.
     */
    default List<Review> getReviewsForAccommodation(String accommodationId) {
        return findByAccommodationId(accommodationId);
    }

    /**
     * Computes the mathematical average star rating (1.0 to 5.0) for an accommodation.
     */
    double getAverageRating(String accommodationId);

    /**
     * Returns total verified reviews count for an accommodation.
     */
    int getReviewCount(String accommodationId);

    /**
     * Checks if a user has already reviewed an accommodation.
     */
    boolean hasUserReviewed(String accommodationId, String username);
}
