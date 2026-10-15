package haven.model;

import haven.pattern.builder.HostelBuilder;

/**
 * Concrete Accommodation subclass representing a college-approved Hostel.
 * Adds fixed mess fee and curfew timing policies.
 */
public class Hostel extends Accommodation {
    private double fixedMessFee;
    private String curfewTime;

    public static HostelBuilder builder() {
        return new HostelBuilder();
    }

    public Hostel(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude, double fixedMessFee, String curfewTime) {
        this(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude, fixedMessFee, curfewTime, "", "");
    }

    public Hostel(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude, double fixedMessFee, String curfewTime, String address, String contactPhone) {
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude, address, contactPhone);
        this.fixedMessFee = fixedMessFee;
        this.curfewTime = curfewTime;
    }

    public Hostel(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, String locationLink, double fixedMessFee, String curfewTime, String address, String contactPhone) {
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, locationLink, address, contactPhone);
        this.fixedMessFee = fixedMessFee;
        this.curfewTime = curfewTime;
    }

    public Hostel(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, String locationLink, double latitude, double longitude, double fixedMessFee, String curfewTime, String address, String contactPhone) {
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, locationLink, latitude, longitude, address, contactPhone);
        this.fixedMessFee = fixedMessFee;
        this.curfewTime = curfewTime;
    }

    @Override
    public double calculateTotalMonthlyCost(int sharingRooms) {
        // Calculation based on rent divided by sharing tier plus mandatory mess charges
        double sharingFactor = (sharingRooms <= 1) ? 1.0 : (sharingRooms == 2 ? 0.75 : 0.6);
        return (getBaseRent() * sharingFactor) + fixedMessFee;
    }

    @Override
    public String getType() {
        return "Hostel";
    }

    public double getFixedMessFee() { return fixedMessFee; }
    public double getMessFee() { return fixedMessFee; }
    public String getCurfewTime() { return curfewTime; }
}
