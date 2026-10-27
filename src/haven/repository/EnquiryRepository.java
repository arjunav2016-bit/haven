package haven.repository;

import haven.model.Enquiry;
import haven.model.Role;

import java.util.List;

/**
 * Domain-specific contract for student-owner Accommodation Enquiries.
 * Extends the generic Repository interface.
 */
public interface EnquiryRepository extends Repository<Enquiry, String> {

    /**
     * Retrieves all enquiries submitted by a specific student.
     */
    List<Enquiry> findByStudentUsername(String studentUsername);

    /**
     * Retrieves all enquiries addressed to a specific property owner.
     */
    List<Enquiry> findByOwnerId(String ownerId);

    /**
     * Role-aware overload: privileged roles (OWNER, ADMIN) see all enquiries;
     * others see only those matching ownerId.
     */
    default List<Enquiry> findByOwnerId(String ownerId, Role role) {
        if (role != null && role.isPrivileged()) {
            return findAll();
        }
        return findByOwnerId(ownerId);
    }

    /**
     * Updates the status and optional response message for an enquiry.
     */
    boolean updateEnquiryStatus(String enquiryId, String newStatus, String ownerResponse);
}
