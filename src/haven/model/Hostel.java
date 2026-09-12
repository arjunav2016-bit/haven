package haven.model;

public class Hostel extends Accommodation {
    private double fixedMessFee;
    private String curfewTime;

    public Hostel(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude, double fixedMessFee, String curfewTime){
        super(id, ownerId, name, nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude);
        this.fixedMessFee = fixedMessFee;
        this.curfewTime = curfewTime;
    }
@Override
public double calculateTotalMontlyCost(int sharingRooms){
    //calculation based on rent divided by sharing tier plus mandatory messs charges
    double sharingFactor = (sharingRooms <= 1) ? 1.0 : (sharingRooms == 2 ? 0.75 : 0.6);
    return (getBaseRent() * sharingFactor) + fixedMessFee;
}
public double getFixedMessFee() { return fixedMessFee; }
public String getCurfewTime() { return curfewTime; }
}

