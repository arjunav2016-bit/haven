package haven.db;

import haven.model.Accommodation;
import haven.repository.AccommodationRepository;
import haven.repository.RepositoryProvider;

import java.util.List;

/**
 * Adapter / Facade maintaining backward compatibility for legacy UI calls,
 * while delegating all data access to the decoupled AccommodationRepository.
 *
 * Underlying repositories publish typed events via HavenEventBus (Observer Pattern)
 * on any data mutation, enabling reactive UI synchronization across all subscribed panels.
 */
public class HostelDAO {
    private final AccommodationRepository repository;

    public HostelDAO() {
        this(RepositoryProvider.getAccommodationRepository());
    }

    public HostelDAO(AccommodationRepository repository) {
        this.repository = repository;
    }

    public void insertAccommodation(Accommodation acc) {
        repository.save(acc);
    }

    public List<Accommodation> getAllAccommodations() {
        return repository.findAll();
    }

    public List<Accommodation> getAccommodationsByOwner(String ownerUsername) {
        return repository.findByOwnerId(ownerUsername);
    }

    public List<Accommodation> filterAccommodations(String college, String gender, double maxRent) {
        return repository.filter(college, gender, maxRent);
    }

    public List<Accommodation> searchAccommodations(String query, String college, String gender, Double maxRent, List<String> requiredAmenities) {
        return repository.search(query, college, gender, maxRent, requiredAmenities);
    }

    public void updateVacancy(String accommodationId, int newVacancy) {
        repository.updateVacancies(accommodationId, newVacancy);
    }

    public boolean decrementVacancy(String accommodationId) {
        return repository.decrementVacancy(accommodationId);
    }

    public void incrementVacancy(String accommodationId) {
        Accommodation acc = getAccommodationById(accommodationId);
        if (acc != null) {
            repository.updateVacancies(accommodationId, acc.getVacancies() + 1);
        }
    }

    public Accommodation getAccommodationById(String accommodationId) {
        return repository.findById(accommodationId).orElse(null);
    }

    public void deleteAccommodation(String accommodationId) {
        repository.deleteById(accommodationId);
    }

    public void updateAccommodation(Accommodation acc) {
        repository.save(acc);
    }
}