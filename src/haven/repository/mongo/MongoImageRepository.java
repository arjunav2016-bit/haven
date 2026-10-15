package haven.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import haven.model.PropertyImage;
import haven.pattern.MongoDBConnection;
import haven.repository.ImageRepository;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB concrete implementation of ImageRepository.
 */
public class MongoImageRepository implements ImageRepository {
    private final MongoCollection<Document> collection;

    public MongoImageRepository() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("property_images");
    }

    @Override
    public PropertyImage save(PropertyImage entity) {
        if (entity == null) return null;
        collection.deleteOne(Filters.eq("id", entity.getId()));
        Document doc = new Document("id", entity.getId())
                .append("accommodationId", entity.getAccommodationId())
                .append("filePath", entity.getFilePath())
                .append("caption", entity.getCaption() != null ? entity.getCaption() : "")
                .append("uploadedBy", entity.getUploadedBy() != null ? entity.getUploadedBy() : "owner")
                .append("uploadedAt", entity.getUploadedAt())
                .append("primary", entity.isPrimary());
        collection.insertOne(doc);
        return entity;
    }

    @Override
    public Optional<PropertyImage> findById(String id) {
        if (id == null) return Optional.empty();
        Document doc = collection.find(Filters.eq("id", id.trim())).first();
        return doc != null ? Optional.of(mapDocToImage(doc)) : Optional.empty();
    }

    @Override
    public List<PropertyImage> findAll() {
        List<PropertyImage> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToImage(cursor.next()));
            }
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        return collection.deleteOne(Filters.eq("id", id.trim())).getDeletedCount() > 0;
    }

    @Override
    public long count() {
        return collection.countDocuments();
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        return collection.countDocuments(Filters.eq("id", id.trim())) > 0;
    }

    @Override
    public List<PropertyImage> findByAccommodationId(String accommodationId) {
        List<PropertyImage> list = new ArrayList<>();
        if (accommodationId == null || accommodationId.trim().isEmpty()) return list;
        try (MongoCursor<Document> cursor = collection.find(Filters.eq("accommodationId", accommodationId.trim())).iterator()) {
            while (cursor.hasNext()) {
                list.add(mapDocToImage(cursor.next()));
            }
        }
        return list;
    }

    private PropertyImage mapDocToImage(Document doc) {
        Long ts = doc.getLong("uploadedAt");
        return new PropertyImage(
                doc.getString("id"),
                doc.getString("accommodationId"),
                doc.getString("filePath"),
                doc.getString("caption"),
                doc.getString("uploadedBy"),
                ts != null ? ts : System.currentTimeMillis(),
                doc.getBoolean("primary", false)
        );
    }
}
