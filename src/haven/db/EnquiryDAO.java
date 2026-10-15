package haven.db;

import haven.model.Enquiry;
import haven.repository.EnquiryRepository;
import haven.repository.RepositoryProvider;

import java.util.Date;
import java.util.List;

/**
 * Adapter / Facade maintaining backward compatibility for legacy UI calls,
 * while delegating all data access to the decoupled EnquiryRepository.
 *
 * Underlying repositories publish typed events via HavenEventBus (Observer Pattern)
 * on any data mutation, enabling reactive UI synchronization across all subscribed panels.
 */
public class EnquiryDAO {
    private final EnquiryRepository repository;

    public EnquiryDAO() {
        this(RepositoryProvider.getEnquiryRepository());
    }

    public EnquiryDAO(EnquiryRepository repository) {
        this.repository = repository;
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
        return repository.findByOwnerId(ownerUsername);
    }

    public List<Enquiry> getEnquiriesForStudent(String studentUsername) {
        return repository.findByStudentUsername(studentUsername);
    }

    public void updateStatus(String enquiryId, String newStatus) {
        repository.updateEnquiryStatus(enquiryId, newStatus, "");
    }

    public void updateStatus(String enquiryId, String newStatus, String ownerResponse) {
        repository.updateEnquiryStatus(enquiryId, newStatus, ownerResponse);
    }

    public void deleteEnquiry(String enquiryId) {
        repository.deleteById(enquiryId);
    }
}

