package haven.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.PG;
import haven.pattern.MongoDBConnection;
import haven.pattern.event.AccommodationEvent;
import haven.pattern.event.HavenEventBus;
import haven.repository.AccommodationRepository;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB concrete implementation of AccommodationRepository.
 */
public class MongoAccommodationRepository implements AccommodationRepository {
    private final MongoCollection<Document> collection;

    public MongoAccommodationRepository() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("hostels");
        if (this.collection.countDocuments() == 0) {
            for (Accommodation acc : haven.db.SeedData.getSCTCEApprovedHostels()) {
                save(acc);
            }
        } else {
            // Migration patch: backfill coordinates for documents seeded before Builder added .coordinates()
            migrateCoordinatesFromSeedData();
        }
    }

    /**
     * One-time migration: if any existing MongoDB documents are missing latitude/longitude
     * (both are null or 0.0), backfill them from SeedData by matching accommodation ID.
     * This fixes the map pin rendering for databases seeded before coordinates were introduced.
     */
    private void migrateCoordinatesFromSeedData() {
        for (Accommodation seed : haven.db.SeedData.getSCTCEApprovedHostels()) {
            if (seed.getLatitude() != 0.0 || seed.getLongitude() != 0.0) {
                collection.updateOne(
                        Filters.and(
                                Filters.eq("id", seed.getId()),
                                Filters.or(
                                        Filters.not(Filters.exists("latitude")),
                                        Filters.ne("latitude", seed.getLatitude()),
                                        Filters.ne("longitude", seed.getLongitude())
                                )
                        ),
                        Updates.combine(
                                Updates.set("latitude", seed.getLatitude()),
                                Updates.set("longitude", seed.getLongitude())
                        )
                );
            }
        }
    }

    @Override
    public Accommodation save(Accommodation entity) {
        if (entity == null) return null;
        if (existsById(entity.getId())) {
            updateAccommodation(entity);
            HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.UPDATED, entity));
        } else {
            collection.insertOne(mapModelToDocument(entity));
            HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.CREATED, entity));
        }
        return entity;
    }

    @Override
    public Optional<Accommodation> findById(String id) {
        if (id == null) return Optional.empty();
        Document doc = collection.find(Filters.eq("id", id)).first();
        return (doc != null) ? Optional.of(mapDocumentToModel(doc)) : Optional.empty();
    }

    @Override
    public List<Accommodation> findAll() {
        List<Accommodation> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocumentToModel(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        Optional<Accommodation> acc = findById(id);
        if (acc.isPresent()) {
            collection.deleteOne(Filters.eq("id", id));
            HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.DELETED, acc.get()));
            return true;
        }
        return false;
    }

    @Override
    public long count() {
        return collection.countDocuments();
    }

    @Override
    public boolean existsById(String id) {
        return collection.countDocuments(Filters.eq("id", id)) > 0;
    }

    @Override
    public List<Accommodation> findByOwnerId(String ownerUsername) {
        List<Accommodation> list = new ArrayList<>();
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            return list;
        }

        Bson query = "owner".equalsIgnoreCase(ownerUsername) ? new Document() : Filters.eq("ownerId", ownerUsername.trim());
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocumentToModel(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public List<Accommodation> filter(String college, String gender, double maxRent) {
        List<Accommodation> list = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        if (college != null && !college.trim().isEmpty() && !college.equalsIgnoreCase("All")) {
            filters.add(Filters.regex("nearbyCollege", "^" + college.trim() + "$", "i"));
        }
        if (gender != null && !gender.trim().isEmpty() && !gender.equalsIgnoreCase("Any")) {
            filters.add(Filters.or(
                    Filters.regex("genderAllowed", "^" + gender.trim() + "$", "i"),
                    Filters.regex("genderAllowed", "^Both$", "i")
            ));
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

    @Override
    public List<Accommodation> search(String query, String college, String gender, Double maxRent, List<String> requiredAmenities) {
        List<Accommodation> all = (college != null || gender != null || (maxRent != null && maxRent > 0))
                ? filter(college, gender, (maxRent != null) ? maxRent : 0)
                : findAll();

        haven.pattern.iterator.AccommodationFilterCriteria criteria = new haven.pattern.iterator.AccommodationFilterCriteria();
        if (query != null) criteria.setSearchQuery(query);
        if (requiredAmenities != null) criteria.setRequiredAmenities(requiredAmenities);
        return new haven.pattern.iterator.FilteredAccommodationIterator(all, criteria).toList();
    }

    @Override
    public boolean decrementVacancy(String id) {
        Accommodation acc = findById(id).orElse(null);
        if (acc != null && acc.getVacancies() > 0) {
            collection.updateOne(
                    Filters.and(Filters.eq("id", id), Filters.gt("vacancies", 0)),
                    Updates.inc("vacancies", -1)
            );
            findById(id).ifPresent(updated ->
                    HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.VACANCY_CHANGED, updated))
            );
            return true;
        }
        return false;
    }

    @Override
    public boolean updateVacancies(String id, int newVacancies) {
        if (newVacancies < 0) return false;
        collection.updateOne(Filters.eq("id", id), Updates.set("vacancies", newVacancies));
        findById(id).ifPresent(updated ->
                HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.VACANCY_CHANGED, updated))
        );
        return true;
    }

    private void updateAccommodation(Accommodation acc) {
        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("name", acc.getName()));
        updates.add(Updates.set("nearbyCollege", acc.getNearbyCollege()));
        updates.add(Updates.set("baseRent", acc.getBaseRent()));
        updates.add(Updates.set("distanceKm", acc.getDistanceKm()));
        updates.add(Updates.set("genderAllowed", acc.getGenderAllowed()));
        updates.add(Updates.set("vacancies", acc.getVacancies()));
        updates.add(Updates.set("locationLink", acc.getLocationLink()));
        updates.add(Updates.set("latitude", acc.getLatitude()));
        updates.add(Updates.set("longitude", acc.getLongitude()));
        updates.add(Updates.set("address", acc.getAddress()));
        updates.add(Updates.set("contactPhone", acc.getContactPhone()));
        updates.add(Updates.set("amenities", acc.getAmenities()));
        updates.add(Updates.set("imagePaths", acc.getImagePaths()));
        updates.add(Updates.set("moderationStatus", acc.getModerationStatus()));
        updates.add(Updates.set("featured", acc.isFeatured()));

        String accType = acc.getType();
        if ("Hostel".equalsIgnoreCase(accType) && acc instanceof Hostel) {
            Hostel h = (Hostel) acc;
            updates.add(Updates.set("type", "HOSTEL"));
            updates.add(Updates.set("fixedMessFee", h.getFixedMessFee()));
            updates.add(Updates.set("curfewTime", h.getCurfewTime()));
        } else if ("PG".equalsIgnoreCase(accType) && acc instanceof PG) {
            PG pg = (PG) acc;
            updates.add(Updates.set("type", "PG"));
            updates.add(Updates.set("foodIncluded", pg.isFoodIncluded()));
            updates.add(Updates.set("utilityCharges", pg.getUtilityCharges()));
        }

        collection.updateOne(Filters.eq("id", acc.getId()), Updates.combine(updates));
    }

    private Document mapModelToDocument(Accommodation acc) {
        haven.pattern.PropertyImageStorage.ensureDefaultImagesForAccommodation(acc);
        Document doc = new Document("id", acc.getId())
                .append("ownerId", acc.getOwnerId())
                .append("name", acc.getName())
                .append("nearbyCollege", acc.getNearbyCollege())
                .append("baseRent", acc.getBaseRent())
                .append("distanceKm", acc.getDistanceKm())
                .append("genderAllowed", acc.getGenderAllowed())
                .append("vacancies", acc.getVacancies())
                .append("locationLink", acc.getLocationLink())
                .append("latitude", acc.getLatitude())
                .append("longitude", acc.getLongitude())
                .append("address", acc.getAddress())
                .append("contactPhone", acc.getContactPhone())
                .append("amenities", acc.getAmenities())
                .append("imagePaths", acc.getImagePaths())
                .append("moderationStatus", acc.getModerationStatus())
                .append("featured", acc.isFeatured());

        String typeStr = acc.getType();
        doc.append("type", typeStr.toUpperCase());
        if ("Hostel".equalsIgnoreCase(typeStr) && acc instanceof Hostel) {
            Hostel h = (Hostel) acc;
            doc.append("fixedMessFee", h.getFixedMessFee())
               .append("curfewTime", h.getCurfewTime());
        } else if ("PG".equalsIgnoreCase(typeStr) && acc instanceof PG) {
            PG pg = (PG) acc;
            doc.append("foodIncluded", pg.isFoodIncluded())
               .append("utilityCharges", pg.getUtilityCharges());
        }
        return doc;
    }

    private Accommodation mapDocumentToModel(Document doc) {
        String type = doc.getString("type");
        String address = doc.getString("address") != null ? doc.getString("address") : "";
        String contactPhone = doc.getString("contactPhone") != null ? doc.getString("contactPhone") : "";
        String locationLink = doc.getString("locationLink");
        double lat = getDoubleSafe(doc, "latitude", 0.0);
        double lon = getDoubleSafe(doc, "longitude", 0.0);

        if (locationLink == null || locationLink.trim().isEmpty()) {
            if (lat != 0.0 && lon != 0.0) {
                locationLink = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
            } else {
                locationLink = Accommodation.buildLocationSearchUrl(doc.getString("name"), address, doc.getString("nearbyCollege"));
            }
        }

        Accommodation acc;
        boolean isHostel = type == null || "HOSTEL".equalsIgnoreCase(type.trim());
        if (isHostel) {
            acc = new Hostel(
                    doc.getString("id"),
                    doc.getString("ownerId"),
                    doc.getString("name"),
                    doc.getString("nearbyCollege"),
                    getDoubleSafe(doc, "baseRent", 4500.0),
                    getDoubleSafe(doc, "distanceKm", 1.0),
                    doc.getString("genderAllowed"),
                    doc.getInteger("vacancies", 0),
                    locationLink,
                    lat,
                    lon,
                    getDoubleSafe(doc, "fixedMessFee", 2500.0),
                    doc.getString("curfewTime") != null ? doc.getString("curfewTime") : "9:30 PM",
                    address,
                    contactPhone
            );
        } else {
            acc = new PG(
                    doc.getString("id"),
                    doc.getString("ownerId"),
                    doc.getString("name"),
                    doc.getString("nearbyCollege"),
                    getDoubleSafe(doc, "baseRent", 4500.0),
                    getDoubleSafe(doc, "distanceKm", 1.0),
                    doc.getString("genderAllowed"),
                    doc.getInteger("vacancies", 0),
                    locationLink,
                    lat,
                    lon,
                    Boolean.TRUE.equals(doc.getBoolean("foodIncluded")),
                    getDoubleSafe(doc, "utilityCharges", 500.0),
                    address,
                    contactPhone
            );
        }

        List<String> amenities = doc.getList("amenities", String.class);
        if (amenities != null && !amenities.isEmpty()) {
            for (String am : amenities) acc.addAmenity(am);
        } else {
            // Defensive enrichment if legacy MongoDB document had empty amenities
            acc.addAmenity("WiFi");
            if (acc instanceof Hostel || (acc instanceof PG && ((PG) acc).isFoodIncluded())) {
                acc.addAmenity("Food / Mess");
            }
            acc.addAmenity("Attached Washroom");
            acc.addAmenity("Power Backup");
            acc.addAmenity("CCTV Security");
        }

        List<String> imagePaths = doc.getList("imagePaths", String.class);
        if (imagePaths != null && !imagePaths.isEmpty()) {
            for (String img : imagePaths) acc.addImagePath(img);
        }
        haven.pattern.PropertyImageStorage.ensureDefaultImagesForAccommodation(acc);

        String modStatus = doc.getString("moderationStatus");
        if (modStatus != null && !modStatus.isEmpty()) {
            acc.setModerationStatus(modStatus);
        }
        Boolean featured = doc.getBoolean("featured");
        if (featured != null) {
            acc.setFeatured(featured);
        } else if ("H1".equalsIgnoreCase(acc.getId()) || "H6".equalsIgnoreCase(acc.getId()) || "P3".equalsIgnoreCase(acc.getId())) {
            acc.setFeatured(true);
        }
        return acc;
    }

    private static double getDoubleSafe(Document doc, String key, double defaultValue) {
        if (doc == null) return defaultValue;
        Object val = doc.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else if (val instanceof String) {
            try {
                return Double.parseDouble(((String) val).trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
