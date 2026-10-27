package haven.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import haven.model.Enquiry;
import haven.model.Notification;
import haven.pattern.MongoDBConnection;
import haven.pattern.event.EnquiryEvent;
import haven.pattern.event.HavenEventBus;
import haven.repository.EnquiryRepository;
import haven.repository.RepositoryProvider;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MongoDB concrete implementation of EnquiryRepository.
 */
public class MongoEnquiryRepository implements EnquiryRepository {
    private static final Logger LOGGER = Logger.getLogger(MongoEnquiryRepository.class.getName());
    private final MongoCollection<Document> collection;

    public MongoEnquiryRepository() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("enquiries");
    }

    @Override
    public Enquiry save(Enquiry entity) {
        if (entity == null) return null;
        String eqId = (entity.getEnquiryId() != null && !entity.getEnquiryId().trim().isEmpty())
                ? entity.getEnquiryId()
                : "ENQ-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Document doc = new Document("enquiryId", eqId)
                .append("accommodationId", entity.getAccommodationId())
                .append("accommodationName", entity.getAccommodationName())
                .append("ownerId", entity.getOwnerId() != null ? entity.getOwnerId() : "")
                .append("studentUsername", entity.getStudentUsername())
                .append("studentPhone", entity.getStudentPhone())
                .append("message", entity.getMessage())
                .append("status", entity.getStatus() != null ? entity.getStatus() : "PENDING")
                .append("createdAt", entity.getCreatedAt() != null ? entity.getCreatedAt() : new Date())
                .append("ownerResponse", entity.getOwnerResponse() != null ? entity.getOwnerResponse() : "");

        collection.insertOne(doc);
        Enquiry saved = mapDocToEnquiry(doc);
        HavenEventBus.getInstance().publish(new EnquiryEvent(EnquiryEvent.Type.SUBMITTED, saved));

        // Notify owner
        String ownerUser = (saved.getOwnerId() == null || saved.getOwnerId().trim().isEmpty() || "OWNER_CURRENT".equalsIgnoreCase(saved.getOwnerId()))
                ? "owner" : saved.getOwnerId();
        try {
            Notification notif = new Notification(
                    ownerUser,
                    "New Booking Enquiry (" + saved.getEnquiryId() + ")",
                    "@" + saved.getStudentUsername() + " applied for " + saved.getAccommodationName(),
                    Notification.Type.ENQUIRY,
                    saved.getEnquiryId()
            );
            RepositoryProvider.getNotificationRepository().save(notif);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to send notification for enquiry: " + saved.getEnquiryId(), e);
        }
        return saved;
    }

    @Override
    public Optional<Enquiry> findById(String id) {
        if (id == null) return Optional.empty();
        Document doc = collection.find(Filters.eq("enquiryId", id.trim())).first();
        return (doc != null) ? Optional.of(mapDocToEnquiry(doc)) : Optional.empty();
    }

    @Override
    public List<Enquiry> findAll() {
        List<Enquiry> list = new ArrayList<>();
        for (Document doc : collection.find().sort(new Document("createdAt", -1))) {
            list.add(mapDocToEnquiry(doc));
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        return collection.deleteOne(Filters.eq("enquiryId", id.trim())).getDeletedCount() > 0;
    }

    @Override
    public long count() {
        return collection.countDocuments();
    }

    @Override
    public boolean existsById(String id) {
        return collection.countDocuments(Filters.eq("enquiryId", id.trim())) > 0;
    }

    @Override
    public List<Enquiry> findByStudentUsername(String studentUsername) {
        List<Enquiry> list = new ArrayList<>();
        if (studentUsername == null || studentUsername.trim().isEmpty()) return list;
        Bson filter = Filters.eq("studentUsername", studentUsername.trim());
        for (Document doc : collection.find(filter).sort(new Document("createdAt", -1))) {
            list.add(mapDocToEnquiry(doc));
        }
        return list;
    }

    @Override
    public List<Enquiry> findByOwnerId(String ownerId) {
        List<Enquiry> list = new ArrayList<>();
        if (ownerId == null || ownerId.trim().isEmpty()) return list;

        Bson filter = Filters.eq("ownerId", ownerId);

        for (Document doc : collection.find(filter).sort(new Document("createdAt", -1))) {
            list.add(mapDocToEnquiry(doc));
        }
        return list;
    }

    @Override
    public boolean updateEnquiryStatus(String enquiryId, String newStatus, String ownerResponse) {
        if (enquiryId == null || newStatus == null) return false;
        Optional<Enquiry> existingOpt = findById(enquiryId);
        if (existingOpt.isEmpty()) return false;
        Enquiry existing = existingOpt.get();
        if (!existing.canTransitionTo(newStatus.trim().toUpperCase())) {
            return false;
        }

        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("status", newStatus.trim().toUpperCase()));
        if (ownerResponse != null && !ownerResponse.trim().isEmpty()) {
            updates.add(Updates.set("ownerResponse", ownerResponse.trim()));
        }

        // Atomic compare-and-swap query: guarantee that the status has not been changed concurrently
        Bson atomicFilter = Filters.and(
                Filters.eq("enquiryId", enquiryId.trim()),
                Filters.eq("status", existing.getStatus())
        );

        long modified = collection.updateOne(atomicFilter, Updates.combine(updates)).getModifiedCount();
        if (modified > 0) {
            findById(enquiryId).ifPresent(eq -> {
                HavenEventBus.getInstance().publish(new EnquiryEvent(EnquiryEvent.Type.STATUS_UPDATED, eq));
                try {
                    Notification notif = new Notification(
                            eq.getStudentUsername(),
                            "Enquiry " + eq.getStatus() + ": " + eq.getAccommodationName(),
                            (eq.getOwnerResponse() != null && !eq.getOwnerResponse().isEmpty())
                                    ? eq.getOwnerResponse()
                                    : "Your enquiry (" + eq.getEnquiryId() + ") status was updated to " + eq.getStatus() + ".",
                            Notification.Type.ENQUIRY,
                            eq.getEnquiryId()
                    );
                    RepositoryProvider.getNotificationRepository().save(notif);
                } catch (RuntimeException e) {
                    LOGGER.log(Level.WARNING, "Failed to send update notification for enquiry: " + eq.getEnquiryId(), e);
                }
            });
            return true;
        }
        return false;
    }

    private Enquiry mapDocToEnquiry(Document doc) {
        String ownerId = doc.getString("ownerId");
        String ownerResponse = doc.getString("ownerResponse");
        return new Enquiry(
                doc.getString("enquiryId"),
                doc.getString("accommodationId"),
                doc.getString("accommodationName"),
                ownerId != null ? ownerId : "",
                doc.getString("studentUsername"),
                doc.getString("studentPhone"),
                doc.getString("message"),
                doc.getString("status"),
                doc.getDate("createdAt") != null ? doc.getDate("createdAt") : new Date(),
                ownerResponse != null ? ownerResponse : ""
        );
    }
}
