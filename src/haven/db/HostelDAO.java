package haven.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.PG;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class HostelDAO {
    private final MongoCollection<Document> collection;

    public HostelDAO() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("hostels");
    }

    public void insertAccommodation(Accommodation acc) {
        Document doc = new Document("id", acc.getId())
                .append("ownerId", acc.getOwnerId())
                .append("name", acc.getName())
                .append("nearbyCollege", acc.getNearbyCollege())
                .append("baseRent", acc.getBaseRent())
                .append("distanceKm", acc.getDistanceKm())
                .append("genderAllowed", acc.getGenderAllowed())
                .append("vacancies", acc.getVacancies())
                .append("latitude", acc.getLatitude())
                .append("longitude", acc.getLongitude());

        if (acc instanceof Hostel) {
            Hostel h = (Hostel) acc;
            doc.append("type", "HOSTEL")
               .append("fixedMessFee", h.getFixedMessFee())
               .append("curfewTime", h.getCurfewTime());
        } else if (acc instanceof PG) {
            PG pg = (PG) acc;
            doc.append("type", "PG")
               .append("foodIncluded", pg.isFoodIncluded())
               .append("utilityCharges", pg.getUtilityCharges());
        }

        collection.insertOne(doc);
    }

    public List<Accommodation> getAllAccommodations() {
        List<Accommodation> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                list.add(mapDocumentToModel(doc));
            }
        }
        return list;
    }

    public List<Accommodation> filterAccommodations(String college, String gender, double maxRent) {
        List<Accommodation> list = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        if (college != null && !college.trim().isEmpty() && !college.equalsIgnoreCase("All")) {
            filters.add(Filters.regex("nearbyCollege", college, "i"));
        }
        if (gender != null && !gender.equalsIgnoreCase("Any")) {
            filters.add(Filters.or(Filters.eq("genderAllowed", gender), Filters.eq("genderAllowed", "Both")));
        }
        if (maxRent > 0) {
            filters.add(Filters.lte("baseRent", maxRent));
        }

        Bson query = filters.isEmpty() ? new Document() : Filters.and(filters);

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocumentToModel(cursor.next()));
            }
        }
        return list;
    }

    public void updateVacancy(String accommodationId, int newVacancy) {
        collection.updateOne(Filters.eq("id", accommodationId), Updates.set("vacancies", newVacancy));
    }

    private Accommodation mapDocumentToModel(Document doc) {
        String type = doc.getString("type");
        if ("HOSTEL".equalsIgnoreCase(type)) {
            return new Hostel(
                    doc.getString("id"),
                    doc.getString("ownerId"),
                    doc.getString("name"),
                    doc.getString("nearbyCollege"),
                    doc.getDouble("baseRent"),
                    doc.getDouble("distanceKm"),
                    doc.getString("genderAllowed"),
                    doc.getInteger("vacancies", 0),
                    doc.getDouble("latitude"),
                    doc.getDouble("longitude"),
                    doc.getDouble("fixedMessFee"),
                    doc.getString("curfewTime")
            );
        } else {
            return new PG(
                    doc.getString("id"),
                    doc.getString("ownerId"),
                    doc.getString("name"),
                    doc.getString("nearbyCollege"),
                    doc.getDouble("baseRent"),
                    doc.getDouble("distanceKm"),
                    doc.getString("genderAllowed"),
                    doc.getInteger("vacancies", 0),
                    doc.getDouble("latitude"),
                    doc.getDouble("longitude"),
                    doc.getBoolean("foodIncluded", false),
                    doc.getDouble("utilityCharges")
            );
        }
    }
}