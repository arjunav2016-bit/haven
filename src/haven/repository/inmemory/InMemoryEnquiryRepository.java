package haven.repository.inmemory;

import haven.model.Enquiry;
import haven.model.Notification;
import haven.event.EnquiryEvent;
import haven.event.HavenEventBus;
import haven.event.NotificationEvent;
import haven.repository.EnquiryRepository;

import java.util.List;
import java.util.Optional;

/**
 * In-Memory &amp; File-backed implementation of {@link EnquiryRepository}.
 */
public class InMemoryEnquiryRepository implements EnquiryRepository {
    private final InMemoryStore store;

    public InMemoryEnquiryRepository() {
        this.store = InMemoryStore.getInstance();
    }

    @Override
    public Enquiry save(Enquiry entity) {
        if (entity == null) return null;
        Enquiry saved = store.saveEnquiry(entity);
        HavenEventBus.getInstance().publish(new EnquiryEvent(EnquiryEvent.Type.SUBMITTED, saved));

        // Notify property owner of new student enquiry
        String targetOwner = (saved.getOwnerId() != null && !saved.getOwnerId().isEmpty()) ? saved.getOwnerId() : "owner";
        Notification n = new Notification(
                targetOwner,
                "New Student Enquiry (" + saved.getEnquiryId() + ")",
                "Student @" + saved.getStudentUsername() + " applied for " + saved.getAccommodationName(),
                Notification.Type.ENQUIRY,
                saved.getEnquiryId()
        );
        store.saveNotification(n);
        HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.CREATED, n, targetOwner));
        return saved;
    }

    @Override
    public Optional<Enquiry> findById(String id) {
        return store.getAllEnquiries().stream()
                .filter(e -> e.getEnquiryId().equalsIgnoreCase(id))
                .findFirst();
    }

    @Override
    public List<Enquiry> findAll() {
        return store.getAllEnquiries();
    }

    @Override
    public boolean deleteById(String id) {
        store.deleteEnquiry(id);
        return true;
    }

    @Override
    public long count() {
        return store.getAllEnquiries().size();
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public List<Enquiry> findByStudentUsername(String studentUsername) {
        return store.getEnquiriesForStudent(studentUsername);
    }

    @Override
    public List<Enquiry> findByOwnerId(String ownerId) {
        return store.getEnquiriesForOwner(ownerId);
    }

    @Override
    public boolean updateEnquiryStatus(String enquiryId, String newStatus, String ownerResponse) {
        boolean updated = store.updateEnquiryStatus(enquiryId, newStatus, ownerResponse);
        if (!updated) {
            return false;
        }
        findById(enquiryId).ifPresent(eq -> {
            HavenEventBus.getInstance().publish(new EnquiryEvent(EnquiryEvent.Type.STATUS_UPDATED, eq));
            Notification n = new Notification(
                    eq.getStudentUsername(),
                    "Enquiry " + eq.getEnquiryId() + " Status: " + eq.getStatus(),
                    "Your application for " + eq.getAccommodationName() + " is now [" + eq.getStatus() + "].",
                    Notification.Type.ENQUIRY,
                    eq.getEnquiryId()
            );
            store.saveNotification(n);
            HavenEventBus.getInstance().publish(new NotificationEvent(NotificationEvent.Type.CREATED, n, eq.getStudentUsername()));
        });
        return true;
    }
}
