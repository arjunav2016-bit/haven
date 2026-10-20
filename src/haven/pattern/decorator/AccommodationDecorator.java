package haven.pattern.decorator;

import haven.exception.VacancyExhaustedException;
import haven.model.Accommodation;
import haven.model.ModerationStatus;

import java.util.List;

/**
 * Abstract Decorator (Decorator Pattern) wrapping an {@link Accommodation} instance.
 * Allows dynamic attachment of additional responsibilities (such as featured badges,
 * scholarship discounts, or verified inspection tags) without altering concrete subclasses.
 *
 * <p>Implements complete delegation across all domain methods to guarantee that mutations
 * (such as decrementVacancy), vacancies, image paths, and amenities always reflect the
 * live wrapped accommodation rather than a detached copy.</p>
 */
public abstract class AccommodationDecorator extends Accommodation {
    protected final Accommodation wrapped;

    public AccommodationDecorator(Accommodation wrapped) {
        super(
                wrapped.getId(),
                wrapped.getOwnerId(),
                wrapped.getName(),
                wrapped.getNearbyCollege(),
                wrapped.getBaseRent(),
                wrapped.getDistanceKm(),
                wrapped.getGenderAllowed(),
                wrapped.getVacancies(),
                wrapped.getLocationLink(),
                wrapped.getLatitude(),
                wrapped.getLongitude(),
                wrapped.getAddress(),
                wrapped.getContactPhone()
        );
        this.wrapped = wrapped;
    }

    public Accommodation getWrapped() {
        return wrapped;
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public String getOwnerId() {
        return wrapped.getOwnerId();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String getNearbyCollege() {
        return wrapped.getNearbyCollege();
    }

    @Override
    public double getBaseRent() {
        return wrapped.getBaseRent();
    }

    @Override
    public void setBaseRent(double baseRent) {
        wrapped.setBaseRent(baseRent);
    }

    @Override
    public double getDistanceKm() {
        return wrapped.getDistanceKm();
    }

    @Override
    public double getDistanceFromCollege() {
        return wrapped.getDistanceFromCollege();
    }

    @Override
    public String getGenderAllowed() {
        return wrapped.getGenderAllowed();
    }

    @Override
    public int getVacancies() {
        return wrapped.getVacancies();
    }

    @Override
    public void setVacancies(int vacancies) {
        wrapped.setVacancies(vacancies);
    }

    @Override
    public void decrementVacancy() throws VacancyExhaustedException {
        wrapped.decrementVacancy();
    }

    @Override
    public String getLocationLink() {
        return wrapped.getLocationLink();
    }

    @Override
    public void setLocationLink(String locationLink) {
        wrapped.setLocationLink(locationLink);
    }

    @Override
    public double getLatitude() {
        return wrapped.getLatitude();
    }

    @Override
    public void setLatitude(double latitude) {
        wrapped.setLatitude(latitude);
    }

    @Override
    public double getLongitude() {
        return wrapped.getLongitude();
    }

    @Override
    public void setLongitude(double longitude) {
        wrapped.setLongitude(longitude);
    }

    @Override
    public void setCoordinates(double latitude, double longitude) {
        wrapped.setCoordinates(latitude, longitude);
    }

    @Override
    public String getAddress() {
        return wrapped.getAddress();
    }

    @Override
    public void setAddress(String address) {
        wrapped.setAddress(address);
    }

    @Override
    public String getContactPhone() {
        return wrapped.getContactPhone();
    }

    @Override
    public void setContactPhone(String contactPhone) {
        wrapped.setContactPhone(contactPhone);
    }

    @Override
    public List<String> getAmenities() {
        return wrapped.getAmenities();
    }

    @Override
    public void setAmenities(List<String> amenities) {
        wrapped.setAmenities(amenities);
    }

    @Override
    public void addAmenity(String amenity) {
        wrapped.addAmenity(amenity);
    }

    @Override
    public boolean hasAmenity(String amenity) {
        return wrapped.hasAmenity(amenity);
    }

    @Override
    public boolean hasAllAmenities(List<String> required) {
        return wrapped.hasAllAmenities(required);
    }

    @Override
    public List<String> getImagePaths() {
        return wrapped.getImagePaths();
    }

    @Override
    public void setImagePaths(List<String> imagePaths) {
        wrapped.setImagePaths(imagePaths);
    }

    @Override
    public void addImagePath(String path) {
        wrapped.addImagePath(path);
    }

    @Override
    public String getPrimaryImagePath() {
        return wrapped.getPrimaryImagePath();
    }

    @Override
    public ModerationStatus getModerationStatusEnum() {
        return wrapped.getModerationStatusEnum();
    }

    @Override
    public String getModerationStatus() {
        return wrapped.getModerationStatus();
    }

    @Override
    public void setModerationStatus(ModerationStatus status) {
        wrapped.setModerationStatus(status);
    }

    @Override
    public void setModerationStatus(String moderationStatus) {
        wrapped.setModerationStatus(moderationStatus);
    }

    @Override
    public boolean isApproved() {
        return wrapped.isApproved();
    }

    @Override
    public boolean isFlagged() {
        return wrapped.isFlagged();
    }

    @Override
    public boolean isRejected() {
        return wrapped.isRejected();
    }

    @Override
    public boolean isFeatured() {
        return wrapped.isFeatured();
    }

    @Override
    public void setFeatured(boolean featured) {
        wrapped.setFeatured(featured);
    }

    @Override
    public double calculateTotalMonthlyCost(int sharingRooms) {
        return wrapped.calculateTotalMonthlyCost(sharingRooms);
    }

    @Override
    public String getType() {
        return wrapped.getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof AccommodationDecorator) {
            return wrapped.equals(((AccommodationDecorator) o).wrapped);
        }
        return wrapped.equals(o);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " wrapping " + wrapped.toString();
    }

    public abstract String getHighlightBadgeText();
}
