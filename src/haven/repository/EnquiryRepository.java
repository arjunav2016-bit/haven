package haven.repository;

import haven.model.Enquiry;
import haven.model.Role;

import java.util.List;

/**
 * Domain-specific contract for student-owner Accommodation Enquiries.
 * Extends the generic Repository interface.
 */
public interface EnquiryRepository extends Repository<Enquiry, String> {

    List<Enquiry> findByStudentUsername(String studentUsername);

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

    boolean updateEnquiryStatus(String enquiryId, String newStatus, String ownerResponse);
}
