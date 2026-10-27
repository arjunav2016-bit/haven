package haven.model.builder;

import haven.model.Hostel;

/**
 * Builder for constructing {@link Hostel} instances fluently.
 */
public class HostelBuilder extends AccommodationBuilder<Hostel, HostelBuilder> {
    private double fixedMessFee = 2500.0;
    private String curfewTime = "9:30 PM";

    public HostelBuilder fixedMessFee(double fixedMessFee) {
        if (fixedMessFee < 0) {
            throw new IllegalArgumentException("Fixed mess fee cannot be negative.");
        }
        this.fixedMessFee = fixedMessFee;
        return this;
    }

    public HostelBuilder curfewTime(String curfewTime) {
        this.curfewTime = (curfewTime != null && !curfewTime.trim().isEmpty()) ? curfewTime.trim() : "9:30 PM";
        return this;
    }

    @Override
    public Hostel build() {
        validateCommon();
        Hostel hostel = new Hostel(
                id, ownerId, name, nearbyCollege, baseRent, distanceKm,
                genderAllowed, vacancies, locationLink, latitude, longitude,
                fixedMessFee, curfewTime, address, contactPhone
        );
        applyCommonPostBuild(hostel);
        return hostel;
    }
}
