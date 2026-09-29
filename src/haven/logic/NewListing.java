package haven.logic;

import haven.model.Hostel;
import haven.model.PG;

public class NewListing {
    public Hostel createHostel(String id,
            String ownerId,
            String name,
            String nearbyCollege,
            double baseRent,
            double distanceKm,
            String genderAllowed,
            int vacancies,
            double latitude,
            double longitude,
            double fixedMessFee,
            String curfewTime){
                Hostel hostel=new Hostel(id,
                    ownerId,
                    name,
                    nearbyCollege,
                    baseRent,
                    distanceKm,
                    genderAllowed,
                    vacancies,
                    latitude,
                    longitude,
                    fixedMessFee,
                    curfewTime);
        return hostel;
    }
    public PG CreatePG(String id,
            String ownerId,
            String name,
            String nearbyCollege,
            double baseRent,
            double distanceKm,
            String genderAllowed,
            int vacancies,
            double latitude,
            double longitude,
            boolean foodIncluded,
            double utilityCharges){
                PG pg = new PG(id,ownerId,name,nearbyCollege, baseRent, distanceKm, genderAllowed, vacancies, latitude, longitude, foodIncluded,utilityCharges);
                return pg;

            }
            
        }
    

