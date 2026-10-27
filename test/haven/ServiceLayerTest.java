package haven.test;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.UserDAO;
import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.pattern.builder.HostelBuilder;
import haven.repository.AccommodationRepository;
import haven.repository.EnquiryRepository;
import haven.repository.RepositoryProvider;
import haven.service.EnquiryService;
import haven.service.ServiceProvider;
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

    @Test
    @DisplayName("[TEST 38] Enquiry State Transitions & Vacancy Invariant (InMemory)")
    public void test38_EnquiryStateTransitionsAndVacancyInMemory() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        EnquiryRepository enqRepo = RepositoryProvider.getEnquiryRepository();
        EnquiryService enqService = ServiceProvider.getEnquiryService();

        verifyEnquiryTransitionsAndVacancy(accRepo, enqRepo, enqService, "INMEM");
    }

    @Test
    @DisplayName("[TEST 39] Enquiry State Transitions & Vacancy Invariant (MongoDB)")
    public void test39_EnquiryStateTransitionsAndVacancyMongo() {
        assumeMongoAvailable();

        haven.repository.mongo.MongoAccommodationRepository accRepo = new haven.repository.mongo.MongoAccommodationRepository();
        haven.repository.mongo.MongoEnquiryRepository enqRepo = new haven.repository.mongo.MongoEnquiryRepository();
        EnquiryService enqService = new EnquiryService(enqRepo, accRepo);

        verifyEnquiryTransitionsAndVacancy(accRepo, enqRepo, enqService, "MONGO");
    }

    private void verifyEnquiryTransitionsAndVacancy(AccommodationRepository accRepo,
                                                   EnquiryRepository enqRepo,
                                                   EnquiryService enqService,
                                                   String suffix) {
        String accId = "REG_ACC_" + suffix;
        Accommodation testStay = new HostelBuilder()
                .id(accId)
                .ownerId("owner")
                .name("Test Hostel " + suffix)
                .nearbyCollege("SCTCE")
                .baseRent(6000.0)
                .vacancies(5)
                .distanceKm(1.0)
                .genderAllowed("Boys")
                .address("Test Address")
                .contactPhone("9876543210")
                .fixedMessFee(1500.0)
                .curfewTime("21:30")
                .build();
        accRepo.save(testStay);

        int initialVac = accRepo.findById(accId).map(Accommodation::getVacancies).orElse(0);
        assertTrue(initialVac >= 5, "Initial vacancy should be at least 5");

        // 1. Submit enquiry: vacancy decrements by 1
        Enquiry enq1 = enqService.createEnquiry(accId, "student", "9876543210", "Booking enquiry 1");
        assertNotNull(enq1);
        String enqId1 = enq1.getEnquiryId();
        assertEquals("PENDING", enq1.getStatus());
        int afterSubmitVac = accRepo.findById(accId).map(Accommodation::getVacancies).orElse(0);
        assertEquals(initialVac - 1, afterSubmitVac, "Vacancy must decrement by 1 on enquiry submission");

        // 2. Cancel enquiry: vacancy increments by exactly 1
        boolean cancelled = enqService.updateEnquiryStatus(enqId1, "CANCELLED", "Student decided not to join");
        assertTrue(cancelled, "Valid transition PENDING -> CANCELLED must succeed");
        int afterCancelVac = accRepo.findById(accId).map(Accommodation::getVacancies).orElse(0);
        assertEquals(initialVac, afterCancelVac, "Cancel must increment vacancy by exactly 1");

        Enquiry reloadedEnq1 = enqRepo.findById(enqId1).orElseThrow();
        assertEquals("CANCELLED", reloadedEnq1.getStatus(), "Enquiry status must be CANCELLED");

        // 3. CANCELLED -> CONFIRMED must be rejected and status unchanged
        boolean illegalConfirm = enqService.updateEnquiryStatus(enqId1, "CONFIRMED", "Attempted confirmation");
        assertFalse(illegalConfirm, "CANCELLED -> CONFIRMED must be rejected");
        Enquiry afterIllegalConfirm = enqRepo.findById(enqId1).orElseThrow();
        assertEquals("CANCELLED", afterIllegalConfirm.getStatus(), "Status must remain CANCELLED after rejected transition");
        int vacAfterIllegalConfirm = accRepo.findById(accId).map(Accommodation::getVacancies).orElse(0);
        assertEquals(initialVac, vacAfterIllegalConfirm, "Vacancy must remain unchanged on rejected transition");

        // Direct repository write rejection for CANCELLED -> CONFIRMED
        boolean repoBypass1 = enqRepo.updateEnquiryStatus(enqId1, "CONFIRMED", "Bypass attempt");
        assertFalse(repoBypass1, "Repository must directly reject CANCELLED -> CONFIRMED");
        assertEquals("CANCELLED", enqRepo.findById(enqId1).orElseThrow().getStatus(), "Repository status must remain CANCELLED");

        // 4. Repeated cancel must not double-increment vacancy (cancel increments vacancy exactly once)
        enqService.updateEnquiryStatus(enqId1, "CANCELLED", "Cancel again");
        assertEquals(initialVac, accRepo.findById(accId).map(Accommodation::getVacancies).orElse(0),
                "Vacancy must NOT increment again on repeated cancel; cancel increments vacancy exactly once");
        assertEquals("CANCELLED", enqRepo.findById(enqId1).orElseThrow().getStatus(),
                "Status must remain CANCELLED");

        // 5. Submit second enquiry for DECLINED -> CONTACTED test
        Enquiry enq2 = enqService.createEnquiry(accId, "student", "9876543210", "Booking enquiry 2");
        assertNotNull(enq2);
        String enqId2 = enq2.getEnquiryId();
        assertEquals("PENDING", enq2.getStatus());

        // Move PENDING -> DECLINED
        boolean declined = enqService.updateEnquiryStatus(enqId2, "DECLINED", "No rooms available");
        assertTrue(declined, "PENDING -> DECLINED must succeed");
        Enquiry reloadedEnq2 = enqRepo.findById(enqId2).orElseThrow();
        assertEquals("DECLINED", reloadedEnq2.getStatus(), "Enquiry status must be DECLINED");

        // DECLINED -> CONTACTED must be rejected and status unchanged
        boolean illegalContact = enqService.updateEnquiryStatus(enqId2, "CONTACTED", "Owner contacting declined student");
        assertFalse(illegalContact, "DECLINED -> CONTACTED must be rejected");
        Enquiry afterIllegalContact = enqRepo.findById(enqId2).orElseThrow();
        assertEquals("DECLINED", afterIllegalContact.getStatus(), "Status must remain DECLINED after rejected transition");

        // Direct repository write rejection for DECLINED -> CONTACTED
        boolean repoBypass2 = enqRepo.updateEnquiryStatus(enqId2, "CONTACTED", "Bypass attempt");
        assertFalse(repoBypass2, "Repository must directly reject DECLINED -> CONTACTED");
        assertEquals("DECLINED", enqRepo.findById(enqId2).orElseThrow().getStatus(), "Repository status must remain DECLINED");

        // Clean up
        enqRepo.deleteById(enqId1);
        enqRepo.deleteById(enqId2);
        accRepo.deleteById(accId);
        java.io.File imgDir = new java.io.File("images", accId);
        if (imgDir.exists()) {
            java.io.File[] files = imgDir.listFiles();
            if (files != null) {
                for (java.io.File f : files) f.delete();
            }
            imgDir.delete();
        }
    }
}
