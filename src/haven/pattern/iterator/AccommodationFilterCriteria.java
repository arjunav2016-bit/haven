package haven.pattern.iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object encapsulating multi-criteria filter parameters for the
 * {@link FilteredAccommodationIterator} (Iterator Pattern).
 */
public class AccommodationFilterCriteria {
    private String searchQuery = "";
    private String college = "All";
    private String gender = "Any";
    private double minRent = 0.0;
    private double maxRent = 0.0;
    private double maxDistanceKm = 0.0;
    private String accommodationType = "ALL"; // "ALL", "HOSTEL", "PG"
    private double minRating = 0.0;
    private boolean vacanciesOnly = false;
    private boolean wishlistOnly = false;
    private List<String> wishlistIds = new ArrayList<>();
    private List<String> requiredAmenities = new ArrayList<>();
    private boolean approvedOnly = true;

    public String getSearchQuery() { return searchQuery; }
    public AccommodationFilterCriteria setSearchQuery(String searchQuery) {
        this.searchQuery = (searchQuery != null) ? searchQuery.trim() : "";
        return this;
    }

    public String getCollege() { return college; }
    public AccommodationFilterCriteria setCollege(String college) {
        this.college = (college != null) ? college.trim() : "All";
        return this;
    }

    public String getGender() { return gender; }
    public AccommodationFilterCriteria setGender(String gender) {
        this.gender = (gender != null) ? gender.trim() : "Any";
        return this;
    }

    public double getMinRent() { return minRent; }
    public AccommodationFilterCriteria setMinRent(double minRent) {
        this.minRent = Math.max(0.0, minRent);
        return this;
    }

    public double getMaxRent() { return maxRent; }
    public AccommodationFilterCriteria setMaxRent(double maxRent) {
        this.maxRent = Math.max(0.0, maxRent);
        return this;
    }

    public double getMaxDistanceKm() { return maxDistanceKm; }
    public AccommodationFilterCriteria setMaxDistanceKm(double maxDistanceKm) {
        this.maxDistanceKm = Math.max(0.0, maxDistanceKm);
        return this;
    }

    public String getAccommodationType() { return accommodationType; }
    public AccommodationFilterCriteria setAccommodationType(String accommodationType) {
        this.accommodationType = (accommodationType != null) ? accommodationType.trim().toUpperCase() : "ALL";
        return this;
    }

    public double getMinRating() { return minRating; }
    public AccommodationFilterCriteria setMinRating(double minRating) {
        this.minRating = Math.max(0.0, Math.min(5.0, minRating));
        return this;
    }

    public boolean isVacanciesOnly() { return vacanciesOnly; }
    public AccommodationFilterCriteria setVacanciesOnly(boolean vacanciesOnly) {
        this.vacanciesOnly = vacanciesOnly;
        return this;
    }

    public boolean isWishlistOnly() { return wishlistOnly; }
    public AccommodationFilterCriteria setWishlistOnly(boolean wishlistOnly) {
        this.wishlistOnly = wishlistOnly;
        return this;
    }

    public List<String> getWishlistIds() { return wishlistIds; }
    public AccommodationFilterCriteria setWishlistIds(List<String> wishlistIds) {
        this.wishlistIds = (wishlistIds != null) ? new ArrayList<>(wishlistIds) : new ArrayList<>();
        return this;
    }

    public List<String> getRequiredAmenities() { return requiredAmenities; }
    public AccommodationFilterCriteria setRequiredAmenities(List<String> requiredAmenities) {
        this.requiredAmenities = (requiredAmenities != null) ? new ArrayList<>(requiredAmenities) : new ArrayList<>();
        return this;
    }

    public boolean isApprovedOnly() { return approvedOnly; }
    public AccommodationFilterCriteria setApprovedOnly(boolean approvedOnly) {
        this.approvedOnly = approvedOnly;
        return this;
    }
}
