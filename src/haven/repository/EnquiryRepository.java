package haven.repository;

import haven.model.Enquiry;

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
     * Updates the status and optional response message for an enquiry.
     */
    boolean updateEnquiryStatus(String enquiryId, String newStatus, String ownerResponse);
}
