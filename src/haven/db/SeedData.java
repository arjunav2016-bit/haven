package haven.db;

import haven.model.Hostel;
import haven.model.PG;

public class SeedData {
    public static void main(String[] args) {
        System.out.println("Connecting to MongoDB and seeding data...");
        HostelDAO dao = new HostelDAO();

        // Clear existing data so duplicates aren't created
        //MongoDBConnection.getInstance().getDatabase().getCollection("hostels").drop();

        // Sample stays around SCTCE Pappanamcode
        dao.insertAccommodation(new Hostel(
                "H01", "OWN01", "Sapphire Hostel", "SCTCE", 
                3200.0, 0.8, "Boys", 5, 
                8.4800, 76.9750, 2500.0, "9:30 PM"
        ));

        dao.insertAccommodation(new PG(
                "PG01", "OWN02", "Green Palms PG", "SCTCE", 
                4500.0, 1.2, "Boys", 12, 
                8.4830, 76.9780, true, 500.0
        ));

        dao.insertAccommodation(new Hostel(
                "H02", "OWN03", "Ivy Luxury Hostel", "SCTCE", 
                5200.0, 1.5, "Girls", 4, 
                8.4790, 76.9710, 2800.0, "8:30 PM"
        ));

        dao.insertAccommodation(new PG(
                "PG02", "OWN04", "Rose Residency PG", "SCTCE", 
                4900.0, 0.5, "Girls", 6, 
                8.4815, 76.9740, true, 600.0
        ));

        dao.insertAccommodation(new PG(
                "PG03", "OWN05", "Cedar Co-living PG", "SCTCE", 
                3800.0, 2.0, "Both", 0, 
                8.4860, 76.9800, false, 400.0
        ));

        System.out.println("Database successfully seeded with 5 accommodations for SCTCE!");
        long count = MongoDBConnection.getInstance()
        .getDatabase()
        .getCollection("hostels")
        .countDocuments();

        System.out.println("Total accommodations: " + count);
        MongoDBConnection.getInstance().close();
    }
}