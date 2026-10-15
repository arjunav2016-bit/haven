package haven.repository;

import haven.model.Accommodation;

import java.util.List;

/**
 * Domain-specific contract for Accommodation persistence and querying.
 * Extends the generic Repository interface.
 */
public interface AccommodationRepository extends Repository<Accommodation, String> {

    /**
     * Finds all accommodations owned by a specific owner username.
     */
    List<Accommodation> findByOwnerId(String ownerUsername);

    /**
     * Filters accommodations by college, gender allowed, and maximum monthly rent.
     */
    List<Accommodation> filter(String college, String gender, double maxRent);

    /**
     * Multi-criteria search supporting text search, college, gender, budget, and amenity flags.
     */
    List<Accommodation> search(String query, String college, String gender, Double maxRent, List<String> requiredAmenities);

    /**
     * Atomically decrements vacancy count for an accommodation if vacancies > 0.
     *
     * @return true if successfully decremented, false if zero vacancies or not found
     */
    boolean decrementVacancy(String id);

    /**
     * Updates the total vacancies for an accommodation.
     */
    boolean updateVacancies(String id, int newVacancies);
}
