package haven.pattern.builder;

import haven.model.Accommodation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract Generic Builder (Builder Pattern) for constructing {@link Accommodation} subclasses
 * cleanly without telescoping constructors.
 *
 * @param <T> The concrete Accommodation type produced
 * @param <B> The self-referential concrete Builder type for fluent method chaining
 */
public abstract class AccommodationBuilder<T extends Accommodation, B extends AccommodationBuilder<T, B>> {
    protected String id;
    protected String ownerId = "owner";
    protected String name;
    protected String nearbyCollege = "SCTCE";
    protected double baseRent = 4500.0;
    protected double distanceKm = 0.8;
    protected String genderAllowed = "Boys";
    protected int vacancies = 10;
    protected String locationLink = "";
    protected double latitude = 8.4795;
    protected double longitude = 76.9742;
    protected String address = "";
    protected String contactPhone = "";
    protected List<String> amenities = new ArrayList<>();
    protected List<String> imagePaths = new ArrayList<>();
    protected String moderationStatus = "APPROVED";
    protected boolean featured = false;

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    public B id(String id) {
        this.id = id;
        return self();
    }

    public B ownerId(String ownerId) {
        this.ownerId = ownerId;
        return self();
    }

    public B name(String name) {
        this.name = name;
        return self();
    }

    public B nearbyCollege(String nearbyCollege) {
        this.nearbyCollege = nearbyCollege;
        return self();
    }

    public B baseRent(double baseRent) {
        this.baseRent = baseRent;
        return self();
    }

    public B distanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
        return self();
    }

    public B genderAllowed(String genderAllowed) {
        this.genderAllowed = genderAllowed;
        return self();
    }

    public B vacancies(int vacancies) {
        this.vacancies = vacancies;
        return self();
    }

    public B locationLink(String locationLink) {
        this.locationLink = locationLink;
        return self();
    }

    public B coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return self();
    }

    public B address(String address) {
        this.address = address;
        return self();
    }

    public B contactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
        return self();
    }

    public B addAmenity(String amenity) {
        if (amenity != null && !amenity.trim().isEmpty() && !this.amenities.contains(amenity.trim())) {
            this.amenities.add(amenity.trim());
        }
        return self();
    }

    public B amenities(List<String> amenities) {
        if (amenities != null) {
            for (String a : amenities) addAmenity(a);
        }
        return self();
    }

    public B addImagePath(String path) {
        if (path != null && !path.trim().isEmpty() && !this.imagePaths.contains(path.trim())) {
            this.imagePaths.add(path.trim());
        }
        return self();
    }

    public B imagePaths(List<String> paths) {
        if (paths != null) {
            for (String p : paths) addImagePath(p);
        }
        return self();
    }

    public B moderationStatus(String status) {
        this.moderationStatus = status;
        return self();
    }

    public B featured(boolean featured) {
        this.featured = featured;
        return self();
    }

    protected void validateCommon() {
        if (id == null || id.trim().isEmpty()) {
            id = "ACC-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Property name is required when building an Accommodation.");
        }
        if (baseRent <= 0) {
            throw new IllegalArgumentException("Base rent must be strictly positive. Provided: " + baseRent);
        }
        if (vacancies < 0) {
            throw new IllegalArgumentException("Vacancies cannot be negative. Provided: " + vacancies);
        }
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative. Provided: " + distanceKm);
        }
        if (locationLink == null || locationLink.trim().isEmpty()) {
            if (latitude != 0.0 && longitude != 0.0) {
                locationLink = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
            } else {
                locationLink = Accommodation.buildLocationSearchUrl(name, address, nearbyCollege);
            }
        }
    }

    protected void applyCommonPostBuild(T instance) {
        instance.setAmenities(this.amenities);
        instance.setImagePaths(this.imagePaths);
        instance.setModerationStatus(this.moderationStatus);
        instance.setFeatured(this.featured);
    }

    /**
     * Builds and validates the target {@link Accommodation} instance.
     *
     * @return constructed domain instance
     */
    public abstract T build();
}
