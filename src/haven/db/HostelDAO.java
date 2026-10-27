package haven.db;

import haven.model.Accommodation;
import haven.model.Role;
import haven.repository.AccommodationRepository;
import haven.repository.RepositoryProvider;
import haven.service.AccommodationService;
import haven.service.ServiceProvider;

import java.util.List;

/**
 * Facade used by UI components to interact with accommodation services and repositories.
 */
public class HostelDAO {
    private final AccommodationRepository repository;
    private final AccommodationService service;

    public HostelDAO() {
        this(RepositoryProvider.getAccommodationRepository(), ServiceProvider.getAccommodationService());
    }

    public HostelDAO(AccommodationRepository repository) {
        this(repository, new AccommodationService(repository, RepositoryProvider.getReviewRepository()));
    }

    public HostelDAO(AccommodationRepository repository, AccommodationService service) {
        this.repository = repository;
        this.service = service;
    }

    public HostelDAO(AccommodationService service) {
        this(RepositoryProvider.getAccommodationRepository(), service);
    }

    public void insertAccommodation(Accommodation acc) {
        service.saveAccommodation(acc);
    }

    public List<Accommodation> getAllAccommodations() {
        return service.getAllAccommodations();
    }

    public List<Accommodation> getAccommodationsByOwner(String ownerUsername) {
        return repository.findByOwnerId(ownerUsername);
    }

    /** Role-aware overload: privileged roles see all accommodations. */
    public List<Accommodation> getAccommodationsByOwner(String ownerUsername, Role role) {
        return repository.findByOwnerId(ownerUsername, role);
    }

    public List<Accommodation> filterAccommodations(String college, String gender, double maxRent) {
        return repository.filter(college, gender, maxRent);
    }

    public List<Accommodation> searchAccommodations(String query, String college, String gender, Double maxRent, List<String> requiredAmenities) {
        return repository.search(query, college, gender, maxRent, requiredAmenities);
    }

    public void updateVacancy(String accommodationId, int newVacancy) {
        service.updateVacancies(accommodationId, newVacancy);
    }

    public boolean decrementVacancy(String accommodationId) {
        return service.decrementVacancy(accommodationId);
    }

    public void incrementVacancy(String accommodationId) {
        Accommodation acc = getAccommodationById(accommodationId);
        if (acc != null) {
            service.updateVacancies(accommodationId, acc.getVacancies() + 1);
        }
    }

    public Accommodation getAccommodationById(String accommodationId) {
        return service.getAccommodationById(accommodationId).orElse(null);
    }

    public void deleteAccommodation(String accommodationId) {
        service.deleteAccommodation(accommodationId);
    }

    public void updateAccommodation(Accommodation acc) {
        service.saveAccommodation(acc);
    }

    public AccommodationService getService() {
        return service;
    }
}