package haven.pattern.decorator;

import haven.model.Accommodation;
import java.util.List;

/**
 * Abstract Decorator (Decorator Pattern) wrapping an {@link Accommodation} instance.
 * Allows dynamic attachment of additional responsibilities (such as featured badges,
 * scholarship discounts, or verified inspection tags) without altering concrete subclasses.
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
    public double calculateTotalMonthlyCost(int sharingRooms) {
        return wrapped.calculateTotalMonthlyCost(sharingRooms);
    }

    @Override
    public String getType() {
        return wrapped.getType();
    }

    @Override
    public List<String> getAmenities() {
        return wrapped.getAmenities();
    }

    @Override
    public List<String> getImagePaths() {
        return wrapped.getImagePaths();
    }

    @Override
    public String getModerationStatus() {
        return wrapped.getModerationStatus();
    }

    public abstract String getHighlightBadgeText();
}
