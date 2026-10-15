package haven.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import haven.model.Review;
import haven.pattern.MongoDBConnection;
import haven.pattern.event.HavenEventBus;
import haven.pattern.event.ReviewEvent;
import haven.repository.ReviewRepository;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MongoDB concrete implementation of ReviewRepository.
 */
public class MongoReviewRepository implements ReviewRepository {
    private final MongoCollection<Document> collection;

    public MongoReviewRepository() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("reviews");
    }

    @Override
    public Review save(Review entity) {
        if (entity == null) return null;
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            entity.setId("REV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        collection.deleteOne(buildIdFilter(entity.getId()));
        Document doc = new Document("id", entity.getId())
                .append("accommodationId", entity.getAccommodationId())
                .append("studentUsername", entity.getUsername())
                .append("studentFullName", entity.getAuthorName())
                .append("rating", entity.getRating())
                .append("comment", entity.getComment())
                .append("flagged", entity.isFlagged())
                .append("timestamp", entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : System.currentTimeMillis());

        collection.insertOne(doc);
        HavenEventBus.getInstance().publish(new ReviewEvent(entity));
        return entity;
    }

    @Override
    public Optional<Review> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        Document doc = collection.find(buildIdFilter(id.trim())).first();
        return doc != null ? Optional.of(mapDocToReview(doc)) : Optional.empty();
    }

    @Override
    public List<Review> findAll() {
        List<Review> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().sort(Sorts.descending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToReview(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        Optional<Review> existing = findById(id);
        boolean deleted = collection.deleteOne(buildIdFilter(id.trim())).getDeletedCount() > 0;
        if (deleted) {
            HavenEventBus.getInstance().publish(new ReviewEvent(ReviewEvent.Type.DELETED, existing.orElse(null)));
        }
        return deleted;
    }

    @Override
    public long count() {
        return collection.countDocuments();
    }

    @Override
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return collection.countDocuments(buildIdFilter(id.trim())) > 0;
    }

    private Bson buildIdFilter(String id) {
        if (ObjectId.isValid(id)) {
            return Filters.or(Filters.eq("id", id), Filters.eq("_id", new ObjectId(id)));
        }
        return Filters.eq("id", id);
    }

    @Override
    public List<Review> findByAccommodationId(String accommodationId) {
        if (accommodationId == null || accommodationId.trim().isEmpty()) return new ArrayList<>();
        List<Review> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(Filters.eq("accommodationId", accommodationId.trim()))
                .sort(Sorts.descending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToReview(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public double getAverageRating(String accommodationId) {
        List<Review> list = findByAccommodationId(accommodationId);
        if (list.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Review r : list) sum += r.getRating();
        return sum / list.size();
    }

    @Override
    public int getReviewCount(String accommodationId) {
        if (accommodationId == null || accommodationId.trim().isEmpty()) return 0;
        return (int) collection.countDocuments(Filters.eq("accommodationId", accommodationId.trim()));
    }

    @Override
    public boolean hasUserReviewed(String accommodationId, String username) {
        if (accommodationId == null || username == null) return false;
        return collection.countDocuments(Filters.and(
                Filters.eq("accommodationId", accommodationId.trim()),
                Filters.eq("studentUsername", username.trim())
        )) > 0;
    }

    private Review mapDocToReview(Document doc) {
        String id = doc.getString("id");
        if (id == null && doc.get("_id") != null) {
            id = doc.get("_id").toString();
        }
        String user = doc.getString("studentUsername") != null ? doc.getString("studentUsername") : doc.getString("username");
        String name = doc.getString("studentFullName") != null ? doc.getString("studentFullName") : doc.getString("authorName");
        Long ts = doc.getLong("timestamp");
        if (ts == null && doc.getDate("createdAt") != null) {
            ts = doc.getDate("createdAt").getTime();
        }
        Review rev = new Review(
                id,
                doc.getString("accommodationId"),
                user != null ? user : "anonymous",
                name != null ? name : "Student",
                doc.getInteger("rating", 5),
                doc.getString("comment"),
                ts != null ? ts : System.currentTimeMillis()
        );
        rev.setFlagged(doc.getBoolean("flagged", false));
        return rev;
    }
}
