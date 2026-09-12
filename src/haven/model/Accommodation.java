package haven.model;

import haven.exception.VacancyExhaustedException;

public abstract class Accommodation {
    private String id;
    private String ownerId;
    private String name;
    private String nearbyCollege;
    private double baseRent;
    private double distanceKm;
    private String genderAllowed; // ""Boys", "Girls", "Both"
    private int vacancies;
    private double latitude;
    private double longitude;

    public Accommodation(String id, String ownerId, String name, String nearbyCollege, double baseRent, double distanceKm, String genderAllowed, int vacancies, double latitude, double longitude){
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.nearbyCollege = nearbyCollege;
        this.baseRent = baseRent;
        this.distanceKm = distanceKm;
        this.genderAllowed = genderAllowed;
        this.vacancies = vacancies;
        this.latitude = latitude;
        this.longitude = longitude;
    }

// Abstract method showing Dynamic Method Dispatch
public abstract double calculateTotalMontlyCost(int sharingRooms);

public void decrementVacancy() throws VacancyExhaustedException {
    if (this.vacancies <= 0) {
        throw new VacancyExhaustedException("Rooms are not currently available at " + name);
    }
    this.vacancies--;
}
public String getId() { return id; }
public String getOwnerId() { return ownerId; }
public String getName() { return name; }
public String getNearbyCollege() { return nearbyCollege; }
public double getBaseRent() { return baseRent; }
public double getDistanceKm() { return distanceKm; }
public String getGenderAllowed() { return genderAllowed; }
public int getVacancies() { return vacancies; }
public double getLatitude() { return latitude; }
public double getLongitude() { return longitude; }

public void setVacancies(int vacancies) { this.vacancies = vacancies; }
public void setBaseRent(double baseRent) { this.baseRent = baseRent; }
}