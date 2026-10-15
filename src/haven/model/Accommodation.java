package haven.model;

import haven.exception.VacancyExhaustedException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base domain entity representing a student accommodation listing.
 * Demonstrates Abstraction, Encapsulation, Inheritance, and Runtime Polymorphism
 * via {@link #calculateTotalMonthlyCost(int)}.
 */
public abstract class Accommodation {
    private String id;
    private String ownerId;
    private String name;
    private String nearbyCollege;
    private double baseRent;
    private double distanceKm;
    private String genderAllowed; // "Boys", "Girls", "Both"
    private int vacancies;
    private String locationLink;
    private double latitude;
    private double longitude;
    private String address;
    private String contactPhone;
    private List<String> amenities = new ArrayList<>();
    private List<String> imagePaths = new ArrayList<>();
    private String moderationStatus = "APPROVED"; // "APPROVED", "PENDING", "FLAGGED", "REJECTED"
    private boolean featured = false;

    public Accommodation(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude) {
        this(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude, "", "");
    }

    public Accommodation(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude, String address, String contactPhone) {
        this(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, 
             buildLocationSearchUrl(name, address, nearbyCollege),
             latitude, longitude, address, contactPhone);
    }

    public Accommodation(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, String locationLink, String address, String contactPhone) {
        this(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, locationLink, 0.0, 0.0, address, contactPhone);
    }

    public Accommodation(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, String locationLink, double latitude, double longitude, String address, String contactPhone) {
        if (baseRent <= 0) {
            throw new IllegalArgumentException("Base rent must be strictly positive. Provided: " + baseRent);
        }
        if (vacancies < 0) {
            throw new IllegalArgumentException("Vacancies cannot be negative. Provided: " + vacancies);
        }
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative. Provided: " + distanceKm);
        }
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.nearbyCollege = nearbyCollege;
        this.baseRent = baseRent;
        this.distanceKm = distanceKm;
        this.genderAllowed = genderAllowed;
        this.vacancies = vacancies;
        this.locationLink = (locationLink != null) ? locationLink : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = (address != null) ? address : "";
        this.contactPhone = (contactPhone != null) ? contactPhone : "";
    }

    /**
     * Calculates total monthly cost based on room sharing count.
     * Subclasses override this method to provide polymorphic pricing formulas.
     *
     * @param sharingRooms number of occupants sharing the room (1, 2, or 3)
     * @return total estimated monthly cost in INR
     */
    public abstract double calculateTotalMonthlyCost(int sharingRooms);

    /**
     * Returns the polymorphic domain type of this accommodation ("Hostel" or "PG").
     * Eliminates instanceof dispatch in mapping and presentation layers (OCP).
     *
     * @return type name string
     */
    public abstract String getType();

    /**
     * Atomically decrements vacancy by 1 if available.
     *
     * @throws VacancyExhaustedException if vacancies are 0
     */
    public void decrementVacancy() throws VacancyExhaustedException {
        if (this.vacancies <= 0) {
            throw new VacancyExhaustedException("Rooms are not currently available at " + name);
        }
        this.vacancies--;
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getNearbyCollege() { return nearbyCollege; }
    public double getBaseRent() { return baseRent; }
    public double getDistanceKm() { return distanceKm; }
    public double getDistanceFromCollege() { return distanceKm; }
    public String getGenderAllowed() { return genderAllowed; }
    public int getVacancies() { return vacancies; }
    public String getLocationLink() {
        if (locationLink != null && !locationLink.trim().isEmpty() && !locationLink.matches(".*query=-?\\d+\\.\\d+,-?\\d+\\.\\d+.*")) {
            return locationLink;
        }
        return buildLocationSearchUrl(name, address, nearbyCollege);
    }

    public static String buildLocationSearchUrl(String name, String address, String nearbyCollege) {
        StringBuilder query = new StringBuilder();
        if (name != null && !name.trim().isEmpty()) {
            query.append(name.trim());
        }
        if (address != null && !address.trim().isEmpty()) {
            if (query.length() > 0) query.append(", ");
            query.append(address.trim());
        }
        String lower = query.toString().toLowerCase();
        if (!lower.contains("trivandrum") && !lower.contains("thiruvananthapuram")) {
            query.append(", Thiruvananthapuram");
        }
        try {
            return "https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(query.toString(), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "https://www.google.com/maps/search/?api=1&query=" + query.toString().replace(" ", "+");
        }
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * @deprecated Typo backward-compatibility alias; use {@link #calculateTotalMonthlyCost(int)}.
     */
    @Deprecated
    public double calculateTotalMontlyCost(int sharingRooms) {
        return calculateTotalMonthlyCost(sharingRooms);
    }

    public String getAddress() { return address; }
    public String getContactPhone() { return contactPhone; }

    public void setLocationLink(String locationLink) { this.locationLink = locationLink; }
    public void setVacancies(int vacancies) {
        if (vacancies < 0) {
            throw new IllegalArgumentException("Vacancies cannot be negative. Provided: " + vacancies);
        }
        this.vacancies = vacancies;
    }
    public void setBaseRent(double baseRent) {
        if (baseRent <= 0) {
            throw new IllegalArgumentException("Base rent must be strictly positive. Provided: " + baseRent);
        }
        this.baseRent = baseRent;
    }
    public void setAddress(String address) { this.address = address; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public List<String> getAmenities() {
        if (amenities == null) amenities = new ArrayList<>();
        return new ArrayList<>(amenities);
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = (amenities != null) ? new ArrayList<>(amenities) : new ArrayList<>();
    }

    public void addAmenity(String amenity) {
        if (amenities == null) amenities = new ArrayList<>();
        if (amenity != null && !amenity.trim().isEmpty() && !amenities.contains(amenity.trim())) {
            amenities.add(amenity.trim());
        }
    }

    public boolean hasAmenity(String amenity) {
        if (amenities == null || amenity == null) return false;
        return amenities.stream().anyMatch(a -> a.equalsIgnoreCase(amenity.trim()));
    }

    public boolean hasAllAmenities(List<String> required) {
        if (required == null || required.isEmpty()) return true;
        for (String req : required) {
            if (!hasAmenity(req)) return false;
        }
        return true;
    }

    // --- Image Gallery Support ---
    public List<String> getImagePaths() {
        if (imagePaths == null) imagePaths = new ArrayList<>();
        return new ArrayList<>(imagePaths);
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = (imagePaths != null) ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    public void addImagePath(String path) {
        if (imagePaths == null) imagePaths = new ArrayList<>();
        if (path != null && !path.trim().isEmpty() && !imagePaths.contains(path.trim())) {
            imagePaths.add(path.trim());
        }
    }

    public String getPrimaryImagePath() {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            return imagePaths.get(0);
        }
        return null;
    }

    // --- Moderation & Featured Support ---
    public ModerationStatus getModerationStatusEnum() {
        return ModerationStatus.fromString(moderationStatus);
    }

    public String getModerationStatus() {
        return getModerationStatusEnum().name();
    }

    public void setModerationStatus(ModerationStatus status) {
        this.moderationStatus = (status != null) ? status.name() : ModerationStatus.APPROVED.name();
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = ModerationStatus.fromString(moderationStatus).name();
    }

    public boolean isApproved() {
        return ModerationStatus.APPROVED.matches(getModerationStatus());
    }

    public boolean isFlagged() {
        return ModerationStatus.FLAGGED.matches(getModerationStatus());
    }

    public boolean isRejected() {
        return ModerationStatus.REJECTED.matches(getModerationStatus());
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Accommodation)) return false;
        Accommodation that = (Accommodation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", baseRent=" + baseRent +
                ", vacancies=" + vacancies +
                ", moderationStatus='" + getModerationStatus() + '\'' +
                '}';
    }
}