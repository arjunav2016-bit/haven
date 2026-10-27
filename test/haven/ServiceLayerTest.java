package haven.test;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.UserDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Service Layer & Architectural Integration Tests")
public class ServiceLayerTest extends TestBase {

    @Test
    @DisplayName("[TEST 37] Unified Architecture (DAO Facade -> Service Layer -> Repository)")
    public void test37_UnifiedArchitectureDaoServiceRepository() {
        HostelDAO unifiedHostelDAO = new HostelDAO();
        assertNotNull(unifiedHostelDAO.getService(), "HostelDAO must be unified with AccommodationService");
        assertEquals(unifiedHostelDAO.getAllAccommodations().size(), unifiedHostelDAO.getService().getAllAccommodations().size(),
                "HostelDAO and AccommodationService query parity failed");

        UserDAO unifiedUserDAO = new UserDAO();
        assertNotNull(unifiedUserDAO.getAuthService(), "UserDAO must be unified with AuthService");
        assertTrue(unifiedUserDAO.userExists("student") && unifiedUserDAO.getAuthService().userExists("student"),
                "UserDAO and AuthService parity failed for student user");

        EnquiryDAO unifiedEnquiryDAO = new EnquiryDAO();
        assertNotNull(unifiedEnquiryDAO.getService(), "EnquiryDAO must be unified with EnquiryService");
        assertEquals(unifiedEnquiryDAO.getEnquiriesForStudent("student").size(), unifiedEnquiryDAO.getService().getEnquiriesForStudent("student").size(),
                "EnquiryDAO and EnquiryService parity failed for student enquiries");
    }
}
