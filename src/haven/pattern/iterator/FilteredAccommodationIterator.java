package haven.pattern.iterator;

import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.PG;
import haven.repository.ReviewRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom Iterator (Iterator Pattern) that lazily traverses a collection of {@link Accommodation}
 * entities and yields only those matching an {@link AccommodationFilterCriteria} specification.
 */
public class FilteredAccommodationIterator implements Iterator<Accommodation>, Iterable<Accommodation> {
    private final List<Accommodation> source;
    private final AccommodationFilterCriteria criteria;
    private final ReviewRepository reviewRepository;
    private int currentIndex = 0;
    private Accommodation nextMatch = null;

    public FilteredAccommodationIterator(Collection<Accommodation> source,
                                         AccommodationFilterCriteria criteria,
                                         ReviewRepository reviewRepository) {
        this.source = (source != null) ? new ArrayList<>(source) : new ArrayList<>();
        this.criteria = (criteria != null) ? criteria : new AccommodationFilterCriteria();
        this.reviewRepository = reviewRepository;
        advanceToNextMatch();
    }

    public FilteredAccommodationIterator(Collection<Accommodation> source, AccommodationFilterCriteria criteria) {
        this(source, criteria, null);
    }

    private void advanceToNextMatch() {
        nextMatch = null;
        while (currentIndex < source.size()) {
            Accommodation candidate = source.get(currentIndex++);
            if (candidate != null && matches(candidate)) {
                nextMatch = candidate;
                break;
            }
        }
    }

    private boolean matches(Accommodation a) {
        if (a == null) return false;

        // 1. Moderation status check (Properly enforce only APPROVED stays, excluding PENDING, FLAGGED, and REJECTED)
        if (criteria.isApprovedOnly() && !a.isApproved()) {
            return false;
        }

        // 2. Text search query with null safety
        String q = (criteria.getSearchQuery() != null) ? criteria.getSearchQuery().trim().toLowerCase() : "";
        if (!q.isEmpty()) {
            boolean textMatch = (a.getName() != null && a.getName().toLowerCase().contains(q))
                    || (a.getAddress() != null && a.getAddress().toLowerCase().contains(q))
                    || (a.getNearbyCollege() != null && a.getNearbyCollege().toLowerCase().contains(q));
            if (!textMatch) return false;
        }

        // 3. College filter with null safety
        String col = criteria.getCollege();
        if (col != null && !col.isEmpty() && !col.equalsIgnoreCase("All") && !col.toLowerCase().startsWith("all")) {
            if (a.getNearbyCollege() == null || !a.getNearbyCollege().toLowerCase().contains(col.toLowerCase())) {
                return false;
            }
        }

        // 4. Gender filter with null safety
        String gender = criteria.getGender();
        if (gender != null && !gender.isEmpty() && !gender.equalsIgnoreCase("Any")) {
            if (a.getGenderAllowed() == null
                    || (!a.getGenderAllowed().equalsIgnoreCase(gender) && !a.getGenderAllowed().equalsIgnoreCase("Both"))) {
                return false;
            }
        }

        // 5. Price range filter (minRent .. maxRent)
        if (criteria.getMinRent() > 0 && a.getBaseRent() < criteria.getMinRent()) {
            return false;
        }
        if (criteria.getMaxRent() > 0 && a.getBaseRent() > criteria.getMaxRent()) {
            return false;
        }

        // 6. Distance filter
        if (criteria.getMaxDistanceKm() > 0 && a.getDistanceKm() > criteria.getMaxDistanceKm()) {
            return false;
        }

        // 7. Accommodation Type filter (HOSTEL / PG / ALL)
        String type = criteria.getAccommodationType();
        if ("HOSTEL".equalsIgnoreCase(type) && !(a instanceof Hostel)) {
            return false;
        }
        if ("PG".equalsIgnoreCase(type) && !(a instanceof PG)) {
            return false;
        }

        // 8. Vacancies only filter
        if (criteria.isVacanciesOnly() && a.getVacancies() <= 0) {
            return false;
        }

        // 9. Wishlist only filter
        if (criteria.isWishlistOnly()) {
            if (criteria.getWishlistIds() == null || !criteria.getWishlistIds().contains(a.getId())) {
                return false;
            }
        }

        // 10. Minimum star rating filter
        if (criteria.getMinRating() > 0.0 && reviewRepository != null) {
            double avg = reviewRepository.getAverageRating(a.getId());
            if (avg < criteria.getMinRating()) {
                return false;
            }
        }

        // 11. Required Amenities filter (Delegates to Accommodation.hasAmenity to prevent substring false-positives)
        List<String> reqAmenities = criteria.getRequiredAmenities();
        if (reqAmenities != null && !reqAmenities.isEmpty()) {
            for (String req : reqAmenities) {
                if (req != null && !req.trim().isEmpty() && !a.hasAmenity(req)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean hasNext() {
        return nextMatch != null;
    }

    @Override
    public Accommodation next() {
        if (nextMatch == null) {
            throw new NoSuchElementException("No more matching accommodations in iterator.");
        }
        Accommodation result = nextMatch;
        advanceToNextMatch();
        return result;
    }

    /**
     * Resets the iterator traversal back to the start of the underlying accommodation source.
     * Allows consumers to re-traverse the filtered collection without instantiating a new iterator.
     */
    public void reset() {
        this.currentIndex = 0;
        this.nextMatch = null;
        advanceToNextMatch();
    }

    /**
     * Returns a fresh iterator instance over the underlying source and filter criteria,
     * ensuring repeated enhanced for-each loops traverse the matched results reliably.
     */
    @Override
    public Iterator<Accommodation> iterator() {
        return new FilteredAccommodationIterator(source, criteria, reviewRepository);
    }

    /**
     * Convenience method to collect all remaining matching elements into a List.
     */
    public List<Accommodation> toList() {
        List<Accommodation> list = new ArrayList<>();
        while (hasNext()) {
            list.add(next());
        }
        return list;
    }
}
