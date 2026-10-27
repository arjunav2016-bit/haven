package haven.db;

import haven.model.Enquiry;
import haven.model.Role;
import haven.repository.EnquiryRepository;
import haven.repository.RepositoryProvider;
import haven.service.EnquiryService;
import haven.service.ServiceProvider;

import java.util.Date;
import java.util.List;

/**
 * Adapter / Facade maintaining backward compatibility for legacy UI calls,
 * while delegating all data access through the decoupled Service Layer
 * ({@link haven.service.EnquiryService}) and underlying {@link EnquiryRepository}.
 *
 * Underlying repositories publish typed events via HavenEventBus (Observer Pattern)
 * on any data mutation, enabling reactive UI synchronization across all subscribed panels.
 */
public class EnquiryDAO {
    private final EnquiryRepository repository;
    private final EnquiryService service;

    public EnquiryDAO() {
        this(RepositoryProvider.getEnquiryRepository(), ServiceProvider.getEnquiryService());
    }

    public EnquiryDAO(EnquiryRepository repository) {
        this(repository, new EnquiryService(repository, RepositoryProvider.getAccommodationRepository()));
    }

    public EnquiryDAO(EnquiryRepository repository, EnquiryService service) {
        this.repository = repository;
        this.service = service;
    }

    public void saveEnquiry(String accId, String accName, String studentUsername, String studentPhone, String message) {
        saveEnquiry(accId, accName, "", studentUsername, studentPhone, message);
    }

    public void saveEnquiry(String accId, String accName, String ownerId, String studentUsername, String studentPhone, String message) {
        Enquiry eq = new Enquiry(null, accId, accName, ownerId, studentUsername, studentPhone, message, new Date());
        repository.save(eq);
    }

    public List<Enquiry> getAllEnquiries() {
        return repository.findAll();
    }

    public List<Enquiry> getEnquiriesForOwner(String ownerUsername) {
        return service.getEnquiriesForOwner(ownerUsername);
    }

    /** Role-aware overload: privileged roles see all enquiries. */
    public List<Enquiry> getEnquiriesForOwner(String ownerUsername, Role role) {
        return service.getEnquiriesForOwner(ownerUsername, role);
    }

    public List<Enquiry> getEnquiriesForStudent(String studentUsername) {
        return service.getEnquiriesForStudent(studentUsername);
    }

    public void updateStatus(String enquiryId, String newStatus) {
        updateStatus(enquiryId, newStatus, "");
    }

    public void updateStatus(String enquiryId, String newStatus, String ownerResponse) {
        boolean handled = service.updateEnquiryStatus(enquiryId, newStatus, ownerResponse);
        if (!handled) {
            repository.updateEnquiryStatus(enquiryId, newStatus, ownerResponse);
        }
    }

    public void deleteEnquiry(String enquiryId) {
        repository.deleteById(enquiryId);
    }

    public EnquiryService getService() {
        return service;
    }
}
