package haven.model;

import haven.pattern.builder.PGBuilder;

/**
 * Concrete Accommodation subclass representing a Paying Guest (PG) facility.
 * Adds optional food provision and utility charges.
 */
public class PG extends Accommodation {
    private boolean foodIncluded;
    private double utilityCharges;

    public static PGBuilder builder() {
        return new PGBuilder();
    }

    public PG(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude, boolean foodIncluded, double utilityCharges) {
        this(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude, foodIncluded, utilityCharges, "", "");
    }

    public PG(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude, boolean foodIncluded, double utilityCharges, String address, String contactPhone) {
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude, address, contactPhone);
        this.foodIncluded = foodIncluded;
        this.utilityCharges = utilityCharges;
    }

    public PG(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, String locationLink, boolean foodIncluded, double utilityCharges, String address, String contactPhone) {
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, locationLink, address, contactPhone);
        this.foodIncluded = foodIncluded;
        this.utilityCharges = utilityCharges;
    }

    public PG(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, String locationLink, double latitude, double longitude, boolean foodIncluded, double utilityCharges, String address, String contactPhone) {
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, locationLink, latitude, longitude, address, contactPhone);
        this.foodIncluded = foodIncluded;
        this.utilityCharges = utilityCharges;
    }

    @Override 
    public double calculateTotalMonthlyCost(int sharingRooms) {
        // PG calculation: base rate plus utilities and optional food charge
        double sharingDiscount = (sharingRooms > 1) ? 400.0 * (sharingRooms - 1) : 0;
        double foodFee = foodIncluded ? 2200.0 : 0.0;
        return (getBaseRent() - sharingDiscount) + utilityCharges + foodFee;
    }

    @Override
    public String getType() {
        return "PG";
    }

    public boolean isFoodIncluded() { return foodIncluded; }
    public double getUtilityCharges() { return utilityCharges; }
}
