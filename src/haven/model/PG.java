package haven.model;

public class PG extends Accommodation {
    private boolean foodIncluded;
    private double utilityCharges;

    public PG(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude, boolean foodIncluded, double utilityCharges) {
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude);
        this.foodIncluded = foodIncluded;
        this.utilityCharges = utilityCharges;
    }
@Override 
public double calculateTotalMontlyCost(int sharingRooms) {
    //PG calculation : base rate plus utilities and optional food charge
    double sharingDiscount = (sharingRooms > 1) ? 400.0 * (sharingRooms -1) : 0;
    double foodFee = foodIncluded ? 2200.0 : 0.0;
    return (getBaseRent() - sharingDiscount) + utilityCharges + foodFee;
}
public boolean isFoodIncluded() { return foodIncluded; }
public double getUtilityCharges() { return utilityCharges; }
}
