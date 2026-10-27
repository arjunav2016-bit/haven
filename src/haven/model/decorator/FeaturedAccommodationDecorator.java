package haven.model.decorator;

import haven.model.Accommodation;

/**
 * Enriches an {@link Accommodation} with featured status and an optional promotional discount.
 */
public class FeaturedAccommodationDecorator extends AccommodationDecorator {
    private final String badgeLabel;
    private final double discountPercentage;

    public FeaturedAccommodationDecorator(Accommodation wrapped) {
        this(wrapped, "FEATURED", 0.0);
    }

    public FeaturedAccommodationDecorator(Accommodation wrapped, String badgeLabel, double discountPercentage) {
        super(wrapped);
        this.badgeLabel = (badgeLabel != null && !badgeLabel.trim().isEmpty()) ? badgeLabel.trim() : "FEATURED";
        this.discountPercentage = Math.max(0.0, Math.min(50.0, discountPercentage));
        this.setFeatured(true);
    }

    @Override
    public double getBaseRent() {
        double rawRent = wrapped.getBaseRent();
        if (discountPercentage > 0.0) {
            return Math.round(rawRent * (1.0 - (discountPercentage / 100.0)));
        }
        return rawRent;
    }

    public double getOriginalBaseRent() {
        return wrapped.getBaseRent();
    }

    @Override
    public double calculateTotalMonthlyCost(int sharingRooms) {
        double rawCost = wrapped.calculateTotalMonthlyCost(sharingRooms);
        if (discountPercentage > 0.0) {
            return Math.round(rawCost * (1.0 - (discountPercentage / 100.0)));
        }
        return rawCost;
    }

    @Override
    public String getHighlightBadgeText() {
        if (discountPercentage > 0.0) {
            return String.format("★ %s (%.0f%% OFF)", badgeLabel, discountPercentage);
        }
        return "★ " + badgeLabel;
    }

    @Override
    public boolean isFeatured() {
        return true;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }
}
