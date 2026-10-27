package haven.service;

import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.model.Role;
import haven.pattern.event.EnquiryEvent;
import haven.pattern.event.HavenEventBus;
import haven.repository.AccommodationRepository;
import haven.repository.EnquiryRepository;
import haven.repository.RepositoryProvider;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Layer component for managing student enquiries and room reservation lifecycles.
 * Coordinates EnquiryRepository, State Pattern transitions, and vacancy deductions.
 */
public class EnquiryService {
    private final EnquiryRepository enquiryRepository;
    private final AccommodationRepository accommodationRepository;

    public EnquiryService() {
        this(RepositoryProvider.getEnquiryRepository(), RepositoryProvider.getAccommodationRepository());
    }

    public EnquiryService(EnquiryRepository enquiryRepository, AccommodationRepository accommodationRepository) {
        this.enquiryRepository = enquiryRepository;
        this.accommodationRepository = accommodationRepository;
    }

    public Optional<Enquiry> getEnquiryById(String id) {
        return enquiryRepository.findById(id);
    }

    public List<Enquiry> getEnquiriesForStudent(String studentUsername) {
        return enquiryRepository.findByStudentUsername(studentUsername);
    }

    public List<Enquiry> getEnquiriesForOwner(String ownerId) {
        return enquiryRepository.findByOwnerId(ownerId);
    }

    /** Role-aware overload: privileged roles see all enquiries. */
    public List<Enquiry> getEnquiriesForOwner(String ownerId, Role role) {
        return enquiryRepository.findByOwnerId(ownerId, role);
    }

    /**
     * Submits a new booking enquiry and atomically decrements property vacancy.
     */
    public Enquiry createEnquiry(String accommodationId, String studentUsername, String phone, String notes) {
        if (accommodationId == null || studentUsername == null) {
            throw new IllegalArgumentException("Accommodation ID and student username are required.");
        }

        Accommodation acc = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + accommodationId));

        if (acc.getVacancies() <= 0) {
            throw new IllegalStateException("No vacancies available at " + acc.getName());
        }

        String enqId = "ENQ-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        Enquiry enquiry = new Enquiry(
                enqId,
                acc.getId(),
                acc.getName(),
                acc.getOwnerId(),
                studentUsername,
                phone,
                notes,
                new Date()
        );

        Enquiry saved = enquiryRepository.save(enquiry);
        accommodationRepository.decrementVacancy(acc.getId());
        HavenEventBus.getInstance().publish(new EnquiryEvent(EnquiryEvent.Type.SUBMITTED, saved));
        return saved;
    }

    /**
     * Updates enquiry status through verified State Pattern transitions.
     * If transitioning to CANCELLED, vacancy is restored.
     */
    public boolean updateEnquiryStatus(String enquiryId, String newStatus, String ownerResponse) {
        Optional<Enquiry> opt = enquiryRepository.findById(enquiryId);
        if (opt.isEmpty()) return false;
        Enquiry current = opt.get();

        String normalized = newStatus.trim().toUpperCase();
        if (!current.canTransitionTo(normalized)) {
            return false;
        }

        boolean ok = enquiryRepository.updateEnquiryStatus(enquiryId, normalized, ownerResponse);
        if (ok && "CANCELLED".equals(normalized)) {
            // Restore vacancy
            accommodationRepository.findById(current.getAccommodationId()).ifPresent(acc -> {
                accommodationRepository.updateVacancies(acc.getId(), acc.getVacancies() + 1);
            });
        }
        return ok;
    }
}
