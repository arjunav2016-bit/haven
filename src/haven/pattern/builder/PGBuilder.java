package haven.pattern.builder;

import haven.model.PG;

/**
 * Concrete Builder (Builder Pattern) for constructing {@link PG} instances fluently.
 */
public class PGBuilder extends AccommodationBuilder<PG, PGBuilder> {
    private boolean foodIncluded = true;
    private double utilityCharges = 500.0;

    public PGBuilder foodIncluded(boolean foodIncluded) {
        this.foodIncluded = foodIncluded;
        return this;
    }

    public PGBuilder utilityCharges(double utilityCharges) {
        if (utilityCharges < 0) {
            throw new IllegalArgumentException("Utility charges cannot be negative.");
        }
        this.utilityCharges = utilityCharges;
        return this;
    }

    @Override
    public PG build() {
        validateCommon();
        PG pg = new PG(
                id, ownerId, name, nearbyCollege, baseRent, distanceKm,
                genderAllowed, vacancies, locationLink, latitude, longitude,
                foodIncluded, utilityCharges, address, contactPhone
        );
        applyCommonPostBuild(pg);
        return pg;
    }
}
