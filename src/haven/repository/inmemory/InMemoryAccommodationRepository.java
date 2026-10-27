package haven.repository.inmemory;

import haven.model.Accommodation;
import haven.event.AccommodationEvent;
import haven.event.HavenEventBus;
import haven.repository.AccommodationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-Memory & File-backed implementation of AccommodationRepository.
 */
public class InMemoryAccommodationRepository implements AccommodationRepository {
    private final InMemoryStore store;

    public InMemoryAccommodationRepository() {
        this.store = InMemoryStore.getInstance();
    }

    @Override
    public Accommodation save(Accommodation entity) {
        if (entity == null) return null;
        Accommodation existing = store.getAccommodationById(entity.getId());
        if (existing == null) {
            store.insertAccommodation(entity);
            HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.CREATED, entity));
        } else {
            store.updateAccommodation(entity);
            HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.UPDATED, entity));
        }
        return entity;
    }

    @Override
    public Optional<Accommodation> findById(String id) {
        return Optional.ofNullable(store.getAccommodationById(id));
    }

    @Override
    public List<Accommodation> findAll() {
        return store.getAllAccommodations();
    }

    @Override
    public boolean deleteById(String id) {
        Accommodation existing = store.getAccommodationById(id);
        if (existing != null) {
            store.deleteAccommodation(id);
            HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.DELETED, existing));
            return true;
        }
        return false;
    }

    @Override
    public long count() {
        return store.getAllAccommodations().size();
    }

    @Override
    public boolean existsById(String id) {
        return store.getAccommodationById(id) != null;
    }

    @Override
    public List<Accommodation> findByOwnerId(String ownerUsername) {
        return store.getAccommodationsByOwner(ownerUsername);
    }

    @Override
    public List<Accommodation> filter(String college, String gender, double maxRent) {
        return store.filterAccommodations(college, gender, maxRent);
    }

    @Override
    public List<Accommodation> search(String query, String college, String gender, Double maxRent, List<String> requiredAmenities) {
        haven.service.filter.AccommodationFilterCriteria criteria = new haven.service.filter.AccommodationFilterCriteria();
        if (query != null) criteria.setSearchQuery(query);
        if (college != null) criteria.setCollege(college);
        if (gender != null) criteria.setGender(gender);
        if (maxRent != null && maxRent > 0) criteria.setMaxRent(maxRent);
        if (requiredAmenities != null) criteria.setRequiredAmenities(requiredAmenities);
        return new haven.service.filter.FilteredAccommodationIterator(store.getAllAccommodations(), criteria).toList();
    }

    @Override
    public boolean decrementVacancy(String id) {
        boolean success = store.decrementVacancy(id);
        if (success) {
            Accommodation acc = store.getAccommodationById(id);
            if (acc != null) {
                HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.VACANCY_CHANGED, acc));
            }
        }
        return success;
    }

    @Override
    public boolean updateVacancies(String id, int newVacancies) {
        Accommodation acc = store.getAccommodationById(id);
        if (acc != null) {
            store.updateVacancy(id, newVacancies);
            HavenEventBus.getInstance().publish(new AccommodationEvent(AccommodationEvent.Type.VACANCY_CHANGED, acc));
            return true;
        }
        return false;
    }
}
