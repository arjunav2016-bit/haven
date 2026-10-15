package haven.repository.inmemory;

import haven.db.InMemoryStore;
import haven.model.PropertyImage;
import haven.repository.ImageRepository;

import java.util.List;
import java.util.Optional;

/**
 * In-Memory &amp; File-backed implementation of {@link ImageRepository}.
 */
public class InMemoryImageRepository implements ImageRepository {
    private final InMemoryStore store;

    public InMemoryImageRepository() {
        this.store = InMemoryStore.getInstance();
    }

    @Override
    public PropertyImage save(PropertyImage entity) {
        return store.saveImage(entity);
    }

    @Override
    public Optional<PropertyImage> findById(String id) {
        if (id == null) return Optional.empty();
        return store.getAllImages().stream().filter(i -> id.equalsIgnoreCase(i.getId())).findFirst();
    }

    @Override
    public List<PropertyImage> findAll() {
        return store.getAllImages();
    }

    @Override
    public boolean deleteById(String id) {
        return store.deleteImage(id);
    }

    @Override
    public long count() {
        return store.getAllImages().size();
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public List<PropertyImage> findByAccommodationId(String accommodationId) {
        return store.getImagesByAccommodation(accommodationId);
    }
}
