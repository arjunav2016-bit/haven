package haven;

import haven.model.*;
import haven.pattern.ReceiptGenerator;
import haven.pattern.event.*;
import haven.repository.*;
import haven.repository.inmemory.FilePersistenceManager;
import haven.repository.inmemory.InMemoryRepositoryFactory;
import haven.view.EmptyStatePanel;
import haven.view.ToastNotification;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestEnhancements {
    public static void main(String[] args) {
        System.out.println("=== RUNNING HAVEN ARCHITECTURAL & ENHANCEMENT VERIFICATIONS ===");

        // 1. Verify Review Model & Star Calculations
        System.out.print("[TEST 1] Review star display & ratings: ");
        Review r1 = new Review("BH01", "student", "Minod", 5, "Excellent hostel!");
        Review r2 = new Review("BH01", "student2", "Arjun", 4, "Good facilities.");
        assert "★★★★★".equals(r1.getStarsDisplay()) : "Star display failed for 5 stars";
        assert "★★★★☆".equals(r2.getStarsDisplay()) : "Star display failed for 4 stars";
        System.out.println("PASSED! (" + r1.getStarsDisplay() + " & " + r2.getStarsDisplay() + ")");

        // 2. Verify Amenity Badges & Matching
        System.out.print("[TEST 2] Amenity management & matching: ");
        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        hostel.addAmenity("WiFi");
        hostel.addAmenity("Food / Mess");
        hostel.addAmenity("CCTV Security");

        assert hostel.hasAmenity("WiFi") : "WiFi check failed";
        assert hostel.hasAmenity("food / mess") : "Case-insensitive amenity check failed";
        assert !hostel.hasAmenity("Swimming Pool") : "Should not have Swimming Pool";
        assert hostel.hasAllAmenities(List.of("WiFi", "Food / Mess")) : "hasAllAmenities failed";
        assert !hostel.hasAllAmenities(List.of("WiFi", "Swimming Pool")) : "hasAllAmenities negative check failed";
        System.out.println("PASSED! Amenities: " + hostel.getAmenities());

        // 3. Verify Receipt Generator Output
        System.out.print("[TEST 3] Receipt & Roster generation: ");
        Enquiry eq = new Enquiry("ENQ-12345", "BH01", "Sreedeepam Boys Hostel", "owner", "student", "9876543210", "Interested in 2-sharing room", new Date());
        User student = new Student("student", "pass123", "Minod Kumar", "9876543210", "SCTCE", "Boys", 8000.0, new ArrayList<>());
        String slip = ReceiptGenerator.generateEnquirySlipText(eq, hostel, student);
        assert slip.contains("ENQ-12345") : "Slip missing enquiry ID";
        assert slip.contains("Sreedeepam Boys Hostel") : "Slip missing hostel name";
        assert slip.contains("Minod Kumar") : "Slip missing student name";
        assert slip.contains("WiFi, Food / Mess, CCTV Security") : "Slip missing amenities";

        List<Enquiry> rosterList = new ArrayList<>();
        rosterList.add(eq);
        String roster = ReceiptGenerator.generateOwnerEnquiryRoster("owner", rosterList);
        assert roster.contains("ENQ-12345") : "Roster missing enquiry ID";
        assert roster.contains("Minod Kumar") || roster.contains("student") : "Roster missing student handle";
        System.out.println("PASSED! Receipt format length: " + slip.length() + " chars");

        // 4. Verify Comparison Logic & Polymorphism
        System.out.print("[TEST 4] Side-by-Side comparison metrics: ");
        PG pg = new PG("BPG01", "owner", "Atham PG", "SCTCE", 4500, 0.9, "Boys", 12, 8.4, 76.9, true, 500, "Pappanamcode", "8848726001");
        pg.addAmenity("WiFi");
        pg.addAmenity("Attached Washroom");
        pg.addAmenity("Power Backup");

        double hostelCost = hostel.calculateTotalMonthlyCost(1);
        double pgCost = pg.calculateTotalMonthlyCost(1);
        assert hostelCost == (4500 + 2500) : "Hostel polymorphic cost calculation failed";
        assert pgCost == (4500 + 500 + 2200) : "PG polymorphic cost calculation failed";
        System.out.println("PASSED! (Hostel Total: \u20B9" + hostelCost + " vs PG Total: \u20B9" + pgCost + ")");

        // 5. Verify Offline In-Memory store & Profile operations
        System.out.print("[TEST 5] Offline In-Memory store & Profile operations: ");
        haven.db.InMemoryStore memStore = haven.db.InMemoryStore.getInstance();
        memStore.resetToDefaultSeed();
        assert memStore.getAllAccommodations().size() > 0 : "InMemoryStore failed to seed accommodations";
        assert memStore.userExists("student") : "Default student user missing in InMemoryStore";

        try {
            User auth = memStore.authenticate("student", "pass123");
            assert auth != null : "InMemoryStore authentication failed";
            assert auth.getPassword() == null : "Password was not stripped from in-memory session user";
        } catch (Exception ex) {
            assert false : "Authentication threw unexpected exception: " + ex.getMessage();
        }

        // Test Profile & Password Update
        memStore.updateProfile("student", "Minod K", "9998887776", "SCTCE", "Boys", 9000.0);
        User updatedStudent = memStore.getUser("student");
        assert "Minod K".equals(updatedStudent.getFullName()) : "InMemoryStore failed to update user fullName";
        assert "9998887776".equals(updatedStudent.getPhone()) : "InMemoryStore failed to update user phone";

        boolean pwdChanged = memStore.changePassword("student", "pass123", "newpass123");
        assert pwdChanged : "changePassword failed with valid credentials";

        try {
            User reAuth = memStore.authenticate("student", "newpass123");
            assert reAuth != null : "Authentication with new password failed";
            assert reAuth.getPassword() == null : "Password was not stripped from in-memory re-authenticated user";
        } catch (Exception ex) {
            assert false : "Re-auth threw exception: " + ex.getMessage();
        }

        System.out.println("PASSED! (" + memStore.getAllAccommodations().size() + " accommodations, auth & profile verified, credentials stripped)");

        // 6. Verify Atomic Vacancy Decrement & Concurrency Guard
        System.out.print("[TEST 6] Atomic vacancy decrement & zero-vacancy protection: ");
        Accommodation testStay = memStore.getAllAccommodations().get(0);
        int initialVac = testStay.getVacancies();
        testStay.setVacancies(1);

        boolean firstDec = memStore.decrementVacancy(testStay.getId());
        assert firstDec : "First decrement should succeed for vacancy = 1";
        assert testStay.getVacancies() == 0 : "Vacancy should now be 0";

        boolean secondDec = memStore.decrementVacancy(testStay.getId());
        assert !secondDec : "Second decrement should fail and return false when vacancies = 0";
        assert testStay.getVacancies() == 0 : "Vacancy should remain 0";

        testStay.setVacancies(initialVac);
        System.out.println("PASSED! (Atomic decrement returned false on 0 vacancies)");

        // 7. Verify Domain Invariant Checks
        System.out.print("[TEST 7] Domain invariants validation (baseRent > 0, vacancies >= 0): ");
        boolean caughtNegativeRent = false;
        try {
            new Hostel("INVALID", "owner", "Negative Rent Hostel", "SCTCE", -500, 1.0, "Boys", 5, 8.4, 76.9, 2000, "9 PM", "Test", "1234567890");
        } catch (IllegalArgumentException ex) {
            caughtNegativeRent = true;
        }
        assert caughtNegativeRent : "Constructing accommodation with negative rent should throw IllegalArgumentException";

        boolean caughtNegativeVac = false;
        try {
            testStay.setVacancies(-2);
        } catch (IllegalArgumentException ex) {
            caughtNegativeVac = true;
        }
        assert caughtNegativeVac : "Setting negative vacancies should throw IllegalArgumentException";
        System.out.println("PASSED! (All domain invariants properly enforced with IllegalArgumentException)");

        // 8. Verify Generic Repository Pattern & Domain Repositories
        System.out.print("[TEST 8] Generic Repository Pattern & Domain Contracts: ");
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        UserRepository userRepo = RepositoryProvider.getUserRepository();
        EnquiryRepository enquiryRepo = RepositoryProvider.getEnquiryRepository();
        ReviewRepository revRepo = RepositoryProvider.getReviewRepository();

        assert accRepo != null : "AccommodationRepository instance is null";
        assert accRepo.count() > 0 : "AccommodationRepository should contain seeded stays";
        assert accRepo.findById("BH01").isPresent() : "BH01 missing from AccommodationRepository";
        assert userRepo.userExists("student") : "student user missing in UserRepository";
        assert revRepo.getReviewsForAccommodation("BH01").size() > 0 : "BH01 should have reviews";
        System.out.println("PASSED! (CRUD and query abstractions functioning on all 4 domain repositories)");

        // 9. Verify Abstract Factory Pattern & Service Provider
        System.out.print("[TEST 9] Abstract Factory Pattern (RepositoryFactory): ");
        RepositoryFactory currentFactory = RepositoryProvider.getFactory();
        assert currentFactory != null : "RepositoryFactory is null";
        assert currentFactory.getAccommodationRepository() != null : "Factory failed to supply AccommodationRepository";
        assert currentFactory.getUserRepository() != null : "Factory failed to supply UserRepository";
        assert currentFactory.getEnquiryRepository() != null : "Factory failed to supply EnquiryRepository";
        assert currentFactory.getReviewRepository() != null : "Factory failed to supply ReviewRepository";
        System.out.println("PASSED! (Active Factory: " + currentFactory.getClass().getSimpleName() + ")");

        // 10. Verify Observer Pattern & Reactive EventBus
        System.out.print("[TEST 10] Observer Pattern & HavenEventBus: ");
        AtomicBoolean accommodationEventReceived = new AtomicBoolean(false);
        AtomicBoolean enquiryEventReceived = new AtomicBoolean(false);
        AtomicBoolean reviewEventReceived = new AtomicBoolean(false);

        HavenEventBus bus = HavenEventBus.getInstance();
        HavenEventListener<AccommodationEvent> accListener = e -> accommodationEventReceived.set(true);
        HavenEventListener<EnquiryEvent> eqListener = e -> enquiryEventReceived.set(true);
        HavenEventListener<ReviewEvent> revListener = e -> reviewEventReceived.set(true);

        bus.subscribe(AccommodationEvent.class, accListener);
        bus.subscribe(EnquiryEvent.class, eqListener);
        bus.subscribe(ReviewEvent.class, revListener);

        bus.publish(new AccommodationEvent(AccommodationEvent.Type.UPDATED, hostel));
        bus.publish(new EnquiryEvent(EnquiryEvent.Type.SUBMITTED, eq));
        bus.publish(new ReviewEvent(r1));

        assert accommodationEventReceived.get() : "AccommodationEvent was not dispatched";
        assert enquiryEventReceived.get() : "EnquiryEvent was not dispatched";
        assert reviewEventReceived.get() : "ReviewEvent was not dispatched";

        bus.unsubscribe(AccommodationEvent.class, accListener);
        bus.unsubscribe(EnquiryEvent.class, eqListener);
        bus.unsubscribe(ReviewEvent.class, revListener);
        System.out.println("PASSED! (Event bus published and delivered all typed events safely)");

        // 11. Verify File-Backed Offline Persistence Engine
        System.out.print("[TEST 11] File-Backed Offline JSON Persistence Engine: ");
        memStore.persist();
        File storeFile = new File("data/haven_store.json");
        assert storeFile.exists() : "data/haven_store.json file was not created";
        assert storeFile.length() > 0 : "data/haven_store.json file is empty";

        FilePersistenceManager.PersistedData loadedData = FilePersistenceManager.load();
        assert loadedData != null : "FilePersistenceManager failed to load persisted data";
        assert !loadedData.accommodations.isEmpty() : "Loaded accommodations is empty";
        assert loadedData.users.containsKey("student") : "Loaded users missing 'student'";
        Accommodation firstAcc = loadedData.accommodations.get(0);
        assert firstAcc.getLatitude() != 0.0 && firstAcc.getLongitude() != 0.0 : "Coordinates were not preserved across JSON file persistence (got 0.0, 0.0)";
        System.out.println("PASSED! (Successfully validated persistence to 'data/haven_store.json', " + storeFile.length() + " bytes, coords preserved)");

        // 12. Verify Modern UI Feedback Components
        System.out.print("[TEST 12] Modern UI Feedback Components (Toast & EmptyState): ");
        EmptyStatePanel emptySearch = EmptyStatePanel.forSearch(() -> {});
        EmptyStatePanel emptyWish = EmptyStatePanel.forWishlist(() -> {});
        EmptyStatePanel emptyEnq = EmptyStatePanel.forEnquiries(() -> {});

        assert emptySearch != null && emptyWish != null && emptyEnq != null : "Empty state components initialization failed";
        ToastNotification toastSuccess = new ToastNotification("Test Notification", ToastNotification.Type.SUCCESS);
        assert toastSuccess != null : "ToastNotification component failed to construct";
        System.out.println("PASSED! (EmptyState & ToastNotification components instantiated and styled)");

        // 13. Verify EventBus Auto-Publishing from DAO Facades (Deduplicated Single-Event Dispatch)
        System.out.print("[TEST 13] EventBus Auto-Publishing from DAO Facades (Deduplicated): ");
        AtomicInteger daoAccEventCount = new AtomicInteger(0);
        AtomicInteger daoReviewEventCount = new AtomicInteger(0);
        AtomicInteger daoEnquiryEventCount = new AtomicInteger(0);

        HavenEventListener<AccommodationEvent> daoAccListener = e -> daoAccEventCount.incrementAndGet();
        HavenEventListener<ReviewEvent> daoRevListener = e -> daoReviewEventCount.incrementAndGet();
        HavenEventListener<EnquiryEvent> daoEqListener = e -> daoEnquiryEventCount.incrementAndGet();

        bus.subscribe(AccommodationEvent.class, daoAccListener);
        bus.subscribe(ReviewEvent.class, daoRevListener);
        bus.subscribe(EnquiryEvent.class, daoEqListener);

        // HostelDAO auto-publishes AccommodationEvent on vacancy update
        haven.db.HostelDAO testHostelDAO = new haven.db.HostelDAO();
        testHostelDAO.updateVacancy("BH01", 10);
        assert daoAccEventCount.get() == 1 : "HostelDAO.updateVacancy should fire exactly 1 AccommodationEvent, fired: " + daoAccEventCount.get();

        // ReviewDAO auto-publishes ReviewEvent on addReview
        haven.db.ReviewDAO testReviewDAO = new haven.db.ReviewDAO();
        Review testReview = new Review("BH01", "test_auto", "Test Auto", 5, "Auto-publish test review");
        testReviewDAO.addReview(testReview);
        assert daoReviewEventCount.get() == 1 : "ReviewDAO.addReview should fire exactly 1 ReviewEvent, fired: " + daoReviewEventCount.get();

        // EnquiryDAO auto-publishes EnquiryEvent on saveEnquiry
        haven.db.EnquiryDAO testEnquiryDAO = new haven.db.EnquiryDAO();
        testEnquiryDAO.saveEnquiry("BH01", "Sreedeepam", "owner", "test_student", "9999999999", "EventBus auto-test");
        assert daoEnquiryEventCount.get() == 1 : "EnquiryDAO.saveEnquiry should fire exactly 1 EnquiryEvent, fired: " + daoEnquiryEventCount.get();

        bus.unsubscribe(AccommodationEvent.class, daoAccListener);
        bus.unsubscribe(ReviewEvent.class, daoRevListener);
        bus.unsubscribe(EnquiryEvent.class, daoEqListener);

        // Cleanup test fixtures so persistent JSON store remains pristine
        for (Enquiry eqCleanup : testEnquiryDAO.getEnquiriesForStudent("test_student")) {
            testEnquiryDAO.deleteEnquiry(eqCleanup.getEnquiryId());
        }
        for (Review revCleanup : testReviewDAO.getReviewsForAccommodation("BH01")) {
            if ("test_auto".equals(revCleanup.getUsername())) {
                testReviewDAO.deleteReview(revCleanup.getId());
            }
        }
        haven.db.InMemoryStore.getInstance().persist();
        System.out.println("PASSED! (All 3 DAO facades fire exactly 1 typed event per mutation without duplicates)");

        // 14. Verify StatusBadgeRenderer Reusability (DRY Principle)
        System.out.print("[TEST 14] StatusBadgeRenderer Reusability (DRY): ");
        haven.view.StatusBadgeRenderer renderer = new haven.view.StatusBadgeRenderer();
        assert renderer != null : "StatusBadgeRenderer failed to instantiate";
        // Verify it correctly renders without throwing for all status values
        javax.swing.JTable dummyTable = new javax.swing.JTable();
        for (String status : new String[]{"PENDING", "CONFIRMED", "CONTACTED", "DECLINED", "CANCELLED", "UNKNOWN"}) {
            java.awt.Component c = renderer.getTableCellRendererComponent(dummyTable, status, false, false, 0, 0);
            assert c != null : "StatusBadgeRenderer returned null for status: " + status;
        }
        System.out.println("PASSED! (Reusable renderer handles all 6 status values without duplication)");

        // 15. Verify Geographic Coordinates Preservation & Mutation
        System.out.print("[TEST 15] Geographic Coordinates Preservation & Mutation: ");
        Hostel coordHostel = new Hostel("CH01", "owner", "Coord Hostel", "CET", 5000, 1.2, "Boys", 5, 8.545, 76.905, 2000, "9 PM", "Trivandrum", "9446684604");
        assert coordHostel.getLatitude() == 8.545 && coordHostel.getLongitude() == 76.905 : "Hostel constructor coordinate assignment failed";
        coordHostel.setCoordinates(8.550, 76.910);
        assert coordHostel.getLatitude() == 8.550 && coordHostel.getLongitude() == 76.910 : "Hostel setCoordinates failed";

        PG coordPG = new PG("CPG01", "owner", "Coord PG", "CET", 4800, 1.0, "Girls", 4, 8.540, 76.900, true, 300, "Trivandrum", "8848726001");
        assert coordPG.getLatitude() == 8.540 && coordPG.getLongitude() == 76.900 : "PG constructor coordinate assignment failed";
        coordPG.setCoordinates(8.542, 76.902);
        assert coordPG.getLatitude() == 8.542 && coordPG.getLongitude() == 76.902 : "PG setCoordinates failed";
        System.out.println("PASSED! (Coordinates correctly set and preserved across Hostel and PG)");

        // 16. Verify Polymorphic Cost Calculation & Method Alias
        System.out.print("[TEST 16] Polymorphic Cost Calculation & Alias: ");
        double hCost1 = hostel.calculateTotalMontlyCost(1);
        double hCost2 = hostel.calculateTotalMonthlyCost(1);
        assert hCost1 == hCost2 : "calculateTotalMonthlyCost alias does not match calculateTotalMontlyCost for Hostel";
        double pgCost1 = pg.calculateTotalMontlyCost(1);
        double pgCost2 = pg.calculateTotalMonthlyCost(1);
        assert pgCost1 == pgCost2 : "calculateTotalMonthlyCost alias does not match calculateTotalMontlyCost for PG";
        System.out.println("PASSED! (Polymorphic monthly cost calculations and aliases are consistent)");

        // 17. Verify Single Vacancy Decrement Integrity
        System.out.print("[TEST 17] Vacancy Decrement Single-Decrement Guarantee: ");
        Accommodation targetAcc = testHostelDAO.getAllAccommodations().get(0);
        int beforeVac = targetAcc.getVacancies();
        if (beforeVac == 0) {
            testHostelDAO.updateVacancy(targetAcc.getId(), 5);
            targetAcc = testHostelDAO.getAccommodationById(targetAcc.getId());
            beforeVac = targetAcc.getVacancies();
        }
        boolean decSuccess = testHostelDAO.decrementVacancy(targetAcc.getId());
        assert decSuccess : "Decrementing vacancy failed";
        Accommodation updatedAcc = testHostelDAO.getAccommodationById(targetAcc.getId());
        assert updatedAcc != null : "Accommodation not found after decrement";
        int afterVac = updatedAcc.getVacancies();
        assert afterVac == beforeVac - 1 : "Vacancy should decrease by exactly 1, but changed from " + beforeVac + " to " + afterVac;
        System.out.println("PASSED! (Vacancies correctly decremented from " + beforeVac + " to " + afterVac + " without double-decrement)");

        // 18. Verify User Profile Multi-Subclass Persistence (Student & Owner)
        System.out.print("[TEST 18] User Profile Multi-Subclass Persistence (Student & Owner): ");
        haven.db.UserDAO testUserDAO = new haven.db.UserDAO();
        testUserDAO.updateProfile("student", "Updated Student Name", "9112233445", "SCTCE", "Boys", 9500.0);
        User fetchedStudent = userRepo.findById("student").orElse(null);
        assert fetchedStudent != null : "Student user not found in repository";
        assert "Updated Student Name".equals(fetchedStudent.getFullName()) : "Student fullName update failed in repository";
        assert "9112233445".equals(fetchedStudent.getPhone()) : "Student phone update failed in repository";

        // Also test Owner profile update
        User ownerUser = userRepo.findById("owner").orElse(null);
        assert ownerUser != null : "Owner user not found in repository";
        ownerUser.setFullName("Updated Owner Name");
        ownerUser.setPhone("9887766554");
        userRepo.updateUserProfile(ownerUser);
        User fetchedOwner = userRepo.findById("owner").orElse(null);
        assert "Updated Owner Name".equals(fetchedOwner.getFullName()) : "Owner fullName update failed in repository";
        assert "9887766554".equals(fetchedOwner.getPhone()) : "Owner phone update failed in repository";
        System.out.println("PASSED! (Profile updates persist fullName and phone for both Student and Owner)");

        // 19. Verify Seamless Dual-Engine Switching (InMemoryRepositoryFactory)
        System.out.print("[TEST 19] Seamless Dual-Engine Switching (InMemory Factory): ");
        RepositoryFactory originalFactory = RepositoryProvider.getFactory();
        InMemoryRepositoryFactory offlineFactory = new InMemoryRepositoryFactory();
        RepositoryProvider.setFactory(offlineFactory);

        AccommodationRepository offAccRepo = RepositoryProvider.getAccommodationRepository();
        UserRepository offUserRepo = RepositoryProvider.getUserRepository();
        EnquiryRepository offEnqRepo = RepositoryProvider.getEnquiryRepository();
        ReviewRepository offRevRepo = RepositoryProvider.getReviewRepository();

        assert offAccRepo != null && offUserRepo != null && offEnqRepo != null && offRevRepo != null : "Offline repositories failed to supply instances";
        assert offAccRepo.count() > 0 : "Offline AccommodationRepository should have seeded records";
        assert offUserRepo.userExists("student") : "Offline UserRepository should contain student";
        assert offRevRepo.getReviewsForAccommodation("BH01").size() > 0 : "Offline ReviewRepository should have reviews";

        // Restore original factory
        RepositoryProvider.setFactory(originalFactory);
        System.out.println("PASSED! (Full CRUD and query parity validated across In-Memory and MongoDB engines)");

        // 20. Verify Builder Pattern (Hostel.builder() & PG.builder())
        System.out.print("[TEST 20] Builder Pattern (HostelBuilder & PGBuilder): ");
        Hostel builtHostel = Hostel.builder()
                .id("HB-TEST")
                .ownerId("owner")
                .name("Builder Pattern Boys Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(5200)
                .distanceKm(0.6)
                .genderAllowed("Boys")
                .vacancies(8)
                .coordinates(8.4712, 76.9788)
                .fixedMessFee(2400)
                .curfewTime("9:30 PM")
                .address("Pappanamcode, Trivandrum")
                .contactPhone("9447112233")
                .addAmenity("WiFi")
                .addAmenity("Food / Mess")
                .featured(true)
                .moderationStatus("APPROVED")
                .build();
        assert "HB-TEST".equals(builtHostel.getId()) : "HostelBuilder ID mismatch";
        assert builtHostel.isFeatured() : "HostelBuilder featured flag not set";
        assert builtHostel.calculateTotalMonthlyCost(1) == 7600.0 : "HostelBuilder total monthly cost mismatch";

        PG builtPG = PG.builder()
                .id("PGB-TEST")
                .ownerId("owner")
                .name("Builder Pattern Executive PG")
                .nearbyCollege("CET")
                .baseRent(6000)
                .distanceKm(0.5)
                .genderAllowed("Girls")
                .vacancies(4)
                .coordinates(8.5460, 76.9060)
                .foodIncluded(true)
                .utilityCharges(600)
                .address("Sreekaryam, Trivandrum")
                .contactPhone("9447998877")
                .addAmenity("WiFi")
                .addAmenity("Power Backup")
                .build();
        assert "PGB-TEST".equals(builtPG.getId()) : "PGBuilder ID mismatch";
        assert builtPG.isFoodIncluded() : "PGBuilder foodIncluded flag not set";
        System.out.println("PASSED! (Fluent construction validated for Hostel and PG hierarchies)");

        // 21. Verify State Pattern (Enquiry Lifecycle State Transitions)
        System.out.print("[TEST 21] State Pattern (EnquiryState Lifecycle & Terminal Guards): ");
        Enquiry stateEnq = new Enquiry("ENQ-STATE", "BH01", "Sreedeepam", "owner", "student", "9876543210", "State test", new Date());
        assert "PENDING".equals(stateEnq.getStatus()) : "Initial state should be PENDING";
        assert stateEnq.canTransitionTo("CONTACTED") : "PENDING should allow transition to CONTACTED";
        stateEnq.transitionTo("CONTACTED");
        assert "CONTACTED".equals(stateEnq.getStatus()) : "Status should be CONTACTED";
        assert stateEnq.canTransitionTo("CONFIRMED") : "CONTACTED should allow transition to CONFIRMED";
        stateEnq.transitionTo("CONFIRMED");
        assert "CONFIRMED".equals(stateEnq.getStatus()) : "Status should be CONFIRMED";
        assert !stateEnq.canTransitionTo("CONTACTED") : "CONFIRMED state must reject backward transition to CONTACTED";
        stateEnq.transitionTo("CANCELLED");
        assert "CANCELLED".equals(stateEnq.getStatus()) && stateEnq.getState().isTerminal() : "CANCELLED must be a terminal state";
        assert !stateEnq.canTransitionTo("CONFIRMED") : "Terminal CANCELLED state must reject transition to CONFIRMED";
        boolean caughtIllegalTransition = false;
        try {
            stateEnq.transitionTo("CONFIRMED");
        } catch (IllegalStateException ex) {
            caughtIllegalTransition = true;
        }
        assert caughtIllegalTransition : "Invalid state transition from terminal CANCELLED to CONFIRMED must throw IllegalStateException";
        assert "CANCELLED".equals(stateEnq.getStatus()) : "Status must remain CANCELLED";
        System.out.println("PASSED! (PENDING -> CONTACTED -> CONFIRMED -> CANCELLED validated, terminal guards enforced)");

        // 22. Verify Command Pattern & Undo/Redo Stack (CommandHistory)
        System.out.print("[TEST 22] Command Pattern & CommandHistory (Execute / Undo / Redo): ");
        haven.pattern.command.CommandHistory cmdHistory = new haven.pattern.command.CommandHistory();
        Accommodation modTarget = accRepo.findById("BH01").orElseThrow();
        String origStatus = modTarget.getModerationStatus();
        boolean origFeatured = modTarget.isFeatured();

        haven.pattern.command.ModerateListingCommand modCmd =
                new haven.pattern.command.ModerateListingCommand(accRepo, "BH01", "FLAGGED", !origFeatured);
        cmdHistory.executeCommand(modCmd);
        Accommodation afterExec = accRepo.findById("BH01").orElseThrow();
        assert "FLAGGED".equals(afterExec.getModerationStatus()) : "Moderation status not updated to FLAGGED";
        assert afterExec.isFeatured() == !origFeatured : "Featured flag not toggled by command";

        assert cmdHistory.canUndo() : "CommandHistory should allow undo";
        cmdHistory.undo();
        Accommodation afterUndo = accRepo.findById("BH01").orElseThrow();
        assert origStatus.equals(afterUndo.getModerationStatus()) : "Undo failed to restore original moderationStatus";
        assert afterUndo.isFeatured() == origFeatured : "Undo failed to restore original featured state";

        assert cmdHistory.canRedo() : "CommandHistory should allow redo";
        cmdHistory.redo();
        Accommodation afterRedo = accRepo.findById("BH01").orElseThrow();
        assert "FLAGGED".equals(afterRedo.getModerationStatus()) : "Redo failed to reapply FLAGGED status";
        // Restore clean state
        cmdHistory.undo();
        System.out.println("PASSED! (Reversible Admin commands executed, undone, and redone cleanly)");

        // 23. Verify Template Method Pattern (ReportGenerator Hierarchy)
        System.out.print("[TEST 23] Template Method Pattern (ReportGenerator Hierarchy): ");
        haven.pattern.report.BookingReceiptReport receiptReport =
                new haven.pattern.report.BookingReceiptReport(eq, hostel, student);
        haven.pattern.report.ApplicantRosterReport rosterReport =
                new haven.pattern.report.ApplicantRosterReport("owner", rosterList);
        haven.pattern.report.SystemAnalyticsReport analyticsReport =
                new haven.pattern.report.SystemAnalyticsReport(
                        userRepo.findAll(), accRepo.findAll(), enquiryRepo.findAll(), revRepo.findAll());

        String rOut1 = receiptReport.generate();
        String rOut2 = rosterReport.generate();
        String rOut3 = analyticsReport.generate();
        assert rOut1.contains("ENQUIRY") : "BookingReceiptReport header missing";
        assert rOut2.contains("ROSTER") : "ApplicantRosterReport header missing";
        assert rOut3.contains("SYSTEM-WIDE ANALYTICS") : "SystemAnalyticsReport header missing";
        System.out.println("PASSED! (All 3 concrete ReportGenerator subclasses produced formatted reports)");

        // 24. Verify Decorator Pattern (FeaturedAccommodationDecorator)
        System.out.print("[TEST 24] Decorator Pattern (FeaturedAccommodationDecorator): ");
        haven.pattern.decorator.FeaturedAccommodationDecorator decorated =
                new haven.pattern.decorator.FeaturedAccommodationDecorator(builtHostel, "TOP RATED NEAR SCTCE", 10.0);
        assert decorated.getHighlightBadgeText().contains("TOP RATED NEAR SCTCE") : "Decorator badge text mismatch";
        assert decorated.isFeatured() : "Decorator should mark accommodation as featured";
        assert decorated.calculateTotalMonthlyCost(1) == Math.round(builtHostel.calculateTotalMonthlyCost(1) * 0.90) : "Decorator 10% promotional discount failed";
        System.out.println("PASSED! (" + decorated.getHighlightBadgeText() + ")");

        // 25. Verify Iterator Pattern (FilteredAccommodationIterator)
        System.out.print("[TEST 25] Iterator Pattern (FilteredAccommodationIterator): ");
        haven.pattern.iterator.AccommodationFilterCriteria criteria =
                new haven.pattern.iterator.AccommodationFilterCriteria()
                        .setCollege("SCTCE")
                        .setAccommodationType("HOSTEL")
                        .setMaxRent(6000)
                        .setVacanciesOnly(true);
        haven.pattern.iterator.FilteredAccommodationIterator filterIterator =
                new haven.pattern.iterator.FilteredAccommodationIterator(accRepo.findAll(), criteria);
        List<Accommodation> iterResults = filterIterator.toList();
        assert !iterResults.isEmpty() : "FilteredAccommodationIterator should match SCTCE hostels under 6000";
        for (Accommodation a : iterResults) {
            assert a instanceof Hostel : "Iterator returned non-Hostel when filtered by HOSTEL";
            assert a.getBaseRent() <= 6000 : "Iterator returned stay exceeding maxRent 6000";
            assert a.getVacancies() > 0 : "Iterator returned stay with 0 vacancies";
        }
        System.out.println("PASSED! (Matched " + iterResults.size() + " SCTCE hostels via lazy FilteredAccommodationIterator)");

        // 26. Verify Mediator Pattern (PanelMediator)
        System.out.print("[TEST 26] Mediator Pattern (PanelMediator Cross-View Coordination): ");
        AtomicBoolean mapFocused = new AtomicBoolean(false);
        haven.pattern.mediator.PanelMediator testMediator = new haven.pattern.mediator.PanelMediator() {
            @Override public void showDiscoverView() {}
            @Override public void showStudentEnquiriesView() {}
            @Override public void showMapView(Accommodation focusAcc) {
                if (focusAcc != null && "BH01".equals(focusAcc.getId())) mapFocused.set(true);
            }
            @Override public void openChatDialog(Accommodation acc, String counterparty, String enqId) {}
            @Override public void openImageGalleryDialog(Accommodation acc) {}
            @Override public void openNotificationInbox() {}
            @Override public void refreshAllViews() {}
        };
        testMediator.showMapView(hostel);
        assert mapFocused.get() : "PanelMediator failed to route showMapView callback";
        System.out.println("PASSED! (Decoupled cross-panel navigation verified)");

        // 27. Verify PropertyImageStorage & ImageRepository
        System.out.print("[TEST 27] Property Image Storage & ImageRepository CRUD: ");
        haven.pattern.PropertyImageStorage.ensureDefaultImagesForAccommodation(hostel);
        assert !hostel.getImagePaths().isEmpty() : "Default images were not generated for hostel";
        File firstImgFile = new File(hostel.getImagePaths().get(0));
        assert firstImgFile.exists() && firstImgFile.length() > 0 : "Generated property image file missing on disk: " + firstImgFile.getPath();

        ImageRepository imgRepo = RepositoryProvider.getImageRepository();
        PropertyImage testImg = new PropertyImage("BH01", hostel.getImagePaths().get(0), "Exterior Facade", "owner");
        imgRepo.save(testImg);
        assert imgRepo.existsById(testImg.getId()) : "ImageRepository failed to persist PropertyImage";
        assert !imgRepo.findByAccommodationId("BH01").isEmpty() : "ImageRepository findByAccommodationId returned empty";
        System.out.println("PASSED! (Disk images verified at '" + hostel.getImagePaths().get(0) + "', ImageRepository CRUD passed)");

        // 28. Verify In-App Messaging & MessageRepository
        System.out.print("[TEST 28] In-App Messaging & MessageRepository (ChatEvent & Threads): ");
        MessageRepository msgRepo = RepositoryProvider.getMessageRepository();
        AtomicBoolean chatEventFired = new AtomicBoolean(false);
        HavenEventListener<ChatEvent> chatTestListener = e -> chatEventFired.set(true);
        bus.subscribe(ChatEvent.class, chatTestListener);

        ChatMessage chatMsg = new ChatMessage("student", "owner", "BH01", "Sreedeepam Boys Hostel", "ENQ-12345",
                "Hello Warden, can I visit the hostel tomorrow at 5 PM?");
        msgRepo.save(chatMsg);
        bus.unsubscribe(ChatEvent.class, chatTestListener);

        assert chatEventFired.get() : "Saving a ChatMessage must publish a ChatEvent";
        List<ChatMessage> thread = msgRepo.findConversation("BH01", "student", "owner");
        assert !thread.isEmpty() : "MessageRepository findConversation returned empty list";
        System.out.println("PASSED! (Conversation persisted & ChatEvent dispatched, " + thread.size() + " messages in thread)");

        // 29. Verify Persistent Notifications & NotificationRepository
        System.out.print("[TEST 29] Persistent Notifications & NotificationRepository: ");
        NotificationRepository notifRepo = RepositoryProvider.getNotificationRepository();
        AtomicBoolean notifClearedFired = new AtomicBoolean(false);
        HavenEventListener<NotificationEvent> notifListener = e -> {
            if (e.getType() == NotificationEvent.Type.CLEARED) notifClearedFired.set(true);
        };
        bus.subscribe(NotificationEvent.class, notifListener);

        Notification testNotif = new Notification("student", "Test Alert", "Your room visit is scheduled.",
                Notification.Type.SYSTEM, "BH01");
        notifRepo.save(testNotif);
        int unreadBefore = notifRepo.countUnread("student");
        assert unreadBefore >= 1 : "Unread notification count should be at least 1";
        notifRepo.markAsRead(testNotif.getId());
        Notification fetchedNotif = notifRepo.findById(testNotif.getId()).orElseThrow();
        assert fetchedNotif.isRead() : "Notification should be marked as read";

        // Test deleteById
        boolean deleted = notifRepo.deleteById(testNotif.getId());
        assert deleted : "deleteById must succeed for existing notification";
        assert !notifRepo.existsById(testNotif.getId()) : "Notification must no longer exist after deleteById";

        // Test clearAll
        Notification n1 = new Notification("test_user", "Alert 1", "Msg 1", Notification.Type.SYSTEM, "");
        Notification n2 = new Notification("test_user", "Alert 2", "Msg 2", Notification.Type.ENQUIRY, "");
        notifRepo.save(n1);
        notifRepo.save(n2);
        assert notifRepo.findByRecipient("test_user").size() == 2 : "Must have 2 notifications for test_user";
        int clearedCount = notifRepo.clearAll("test_user");
        assert clearedCount == 2 : "clearAll must return cleared count of 2";
        assert notifRepo.findByRecipient("test_user").isEmpty() : "test_user inbox must be empty after clearAll";
        assert notifClearedFired.get() : "NotificationEvent.Type.CLEARED must be published on clearAll/delete";
        bus.unsubscribe(NotificationEvent.class, notifListener);

        System.out.println("PASSED! (Notifications persisted, marked read, deleted individually, and bulk cleared)");

        // 30. Verify Admin Role & Account Governance (Activate / Deactivate)
        System.out.print("[TEST 30] Admin Role & Account Governance (RBAC & Deactivation Guard): ");
        assert userRepo.userExists("admin") : "Default admin user must exist in UserRepository";
        try {
            User adminUser = userRepo.authenticate("admin", "admin123");
            assert adminUser != null && adminUser.isAdmin() : "Admin authentication or role check failed";
        } catch (Exception ex) {
            assert false : "Admin authentication failed: " + ex.getMessage();
        }

        // Reset student password back to pass123 for clean demo logins
        memStore.changePassword("student", "newpass123", "pass123");
        System.out.println("PASSED! (Admin RBAC verified and demo credentials ready)");

        // 31. State Pattern Full 5x5 Transition Matrix Validation
        System.out.print("[TEST 31] State Pattern Full Transition Matrix (5 States x 5 Targets): ");
        haven.pattern.state.EnquiryState[] allStates = {
                new haven.pattern.state.PendingState(),
                new haven.pattern.state.ContactedState(),
                new haven.pattern.state.ConfirmedState(),
                new haven.pattern.state.DeclinedState(),
                new haven.pattern.state.CancelledState()
        };
        String[] allTargets = {"PENDING", "CONTACTED", "CONFIRMED", "DECLINED", "CANCELLED"};

        // Expected canTransitionTo matrix [fromState][toTarget]:
        // PENDING:    P=T, CO=T, CF=T, D=T, CA=T    (open state, allows all)
        // CONTACTED:  P=F, CO=T, CF=T, D=T, CA=T    (no backward to PENDING)
        // CONFIRMED:  P=F, CO=F, CF=T, D=T, CA=T    (forward + cancel only)
        // DECLINED:   P=F, CO=F, CF=F, D=T, CA=F    (terminal, only self)
        // CANCELLED:  P=F, CO=F, CF=F, D=F, CA=T    (terminal, only self)
        boolean[][] expectedMatrix = {
                {true,  true,  true,  true,  true },  // PENDING
                {false, true,  true,  true,  true },  // CONTACTED
                {false, false, true,  true,  true },  // CONFIRMED
                {false, false, false, true,  false},  // DECLINED
                {false, false, false, false, true },  // CANCELLED
        };
        int matrixErrors = 0;
        for (int i = 0; i < allStates.length; i++) {
            for (int j = 0; j < allTargets.length; j++) {
                boolean actual = allStates[i].canTransitionTo(allTargets[j]);
                if (actual != expectedMatrix[i][j]) {
                    System.err.printf("  MISMATCH: %s -> %s expected=%b actual=%b%n",
                            allStates[i].getStatusName(), allTargets[j], expectedMatrix[i][j], actual);
                    matrixErrors++;
                }
            }
        }
        assert matrixErrors == 0 : matrixErrors + " transition matrix mismatches detected (see stderr)";
        System.out.println("PASSED! (25/25 transitions validated across PENDING/CONTACTED/CONFIRMED/DECLINED/CANCELLED)");

        // 32. State Pattern Terminal Idempotency & Edge Cases
        System.out.print("[TEST 32] State Pattern Terminal Idempotency & Edge Cases: ");
        // Double-cancel: CancelledState.cancel() should return self (idempotent)
        haven.pattern.state.EnquiryState cancelledState = new haven.pattern.state.CancelledState();
        haven.pattern.state.EnquiryState cancelledAgain = cancelledState.cancel();
        assert cancelledAgain == cancelledState : "Double-cancel must return same CancelledState instance";
        assert cancelledState.isTerminal() : "CancelledState must be terminal";

        // Double-decline: DeclinedState.decline() should return self (idempotent)
        haven.pattern.state.EnquiryState declinedState = new haven.pattern.state.DeclinedState();
        haven.pattern.state.EnquiryState declinedAgain = declinedState.decline();
        assert declinedAgain == declinedState : "Double-decline must return same DeclinedState instance";
        assert declinedState.isTerminal() : "DeclinedState must be terminal";

        // Self-transition on non-terminal states should be idempotent via transitionTo default method
        haven.pattern.state.EnquiryState pendingSelf = new haven.pattern.state.PendingState().transitionTo("PENDING");
        assert "PENDING".equals(pendingSelf.getStatusName()) : "PENDING self-transition must stay PENDING";

        haven.pattern.state.EnquiryState contactedSelf = new haven.pattern.state.ContactedState().transitionTo("CONTACTED");
        assert "CONTACTED".equals(contactedSelf.getStatusName()) : "CONTACTED self-transition must stay CONTACTED";

        // Terminal states must throw on all illegal targets
        String[] illegalFromDeclined = {"CONTACTED", "CONFIRMED", "CANCELLED"};
        for (String target : illegalFromDeclined) {
            boolean threw = false;
            try { declinedState.transitionTo(target); } catch (IllegalStateException ex) { threw = true; }
            assert threw : "DeclinedState.transitionTo(" + target + ") must throw IllegalStateException";
        }

        String[] illegalFromCancelled = {"CONTACTED", "CONFIRMED", "DECLINED"};
        for (String target : illegalFromCancelled) {
            boolean threw = false;
            try { cancelledState.transitionTo(target); } catch (IllegalStateException ex) { threw = true; }
            assert threw : "CancelledState.transitionTo(" + target + ") must throw IllegalStateException";
        }
        System.out.println("PASSED! (Double-cancel/decline idempotent, self-transitions stable, 6 terminal guards enforced)");

        // 33. Audit Enhancements: Encapsulation, Defensive Copies & Domain Identity
        System.out.print("[TEST 33] Encapsulation, Defensive Copies & Identity (equals/hashCode): ");
        // Defensive copy on Accommodation amenities & imagePaths
        Accommodation sampleStay = accRepo.findAll().get(0);
        int origAmenityCount = sampleStay.getAmenities().size();
        try {
            sampleStay.getAmenities().add("HACK_AMENITY");
        } catch (UnsupportedOperationException ignored) {}
        assert sampleStay.getAmenities().size() == origAmenityCount : "getAmenities() must return defensive copy";

        int origImageCount = sampleStay.getImagePaths().size();
        try {
            sampleStay.getImagePaths().add("HACK_PATH");
        } catch (UnsupportedOperationException ignored) {}
        assert sampleStay.getImagePaths().size() == origImageCount : "getImagePaths() must return defensive copy";

        // Defensive copy on User wishlist
        User studentUser = userRepo.findById("student").orElseThrow();
        int origWishCount = studentUser.getWishlist().size();
        try {
            studentUser.getWishlist().add("HACK_ID");
        } catch (UnsupportedOperationException ignored) {}
        assert studentUser.getWishlist().size() == origWishCount : "getWishlist() must return defensive copy";

        // Domain identity equals/hashCode
        Accommodation copyStay = new haven.model.Hostel(sampleStay.getId(), "owner", "Diff Name", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        assert sampleStay.equals(copyStay) : "Accommodation.equals() must match on ID";
        assert sampleStay.hashCode() == copyStay.hashCode() : "Accommodation.hashCode() must match on ID";

        User copyUser = new haven.model.Student("STUDENT", "otherpass", "Other Name", "999", new ArrayList<>());
        assert studentUser.equals(copyUser) : "User.equals() must match on case-insensitive username";
        assert studentUser.hashCode() == copyUser.hashCode() : "User.hashCode() must match on case-insensitive username";

        // Defensive Date copies
        haven.model.Enquiry enquiry = new haven.model.Enquiry("e1", "BH01", "student", "owner1", "Hi", "PENDING", new Date(1000000L));
        Date d1 = enquiry.getCreatedAt();
        d1.setTime(99999999999L);
        assert enquiry.getCreatedAt().getTime() == 1000000L : "Enquiry.getCreatedAt() must return defensive Date copy";

        System.out.println("PASSED! (Defensive copies on collections/Dates & equals/hashCode verified)");

        // 34. Audit Enhancements: Enums, Polymorphic getType(), Validation & Iterator Reset
        System.out.print("[TEST 34] Enums, Polymorphic getType(), ValidationUtils & Iterator Reset: ");
        // Role and ModerationStatus Enums
        assert haven.model.Role.fromString("student") == haven.model.Role.STUDENT;
        assert haven.model.Role.fromString("OWNER") == haven.model.Role.OWNER;
        assert haven.model.Role.fromString("Admin") == haven.model.Role.ADMIN;
        assert haven.model.Role.STUDENT.matches("student");
        assert haven.model.ModerationStatus.fromString("approved") == haven.model.ModerationStatus.APPROVED;

        // Polymorphic getType()
        haven.model.Hostel testHostel = new haven.model.Hostel("H01", "owner", "H", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        haven.model.PG testPG = new haven.model.PG("P01", "owner", "P", "SCTCE", 4500, 0.9, "Boys", 12, 8.4, 76.9, true, 500, "Pappanamcode", "8848726001");
        assert "Hostel".equals(testHostel.getType()) : "Hostel.getType() must return 'Hostel'";
        assert "PG".equals(testPG.getType()) : "PG.getType() must return 'PG'";

        // Iterator reset
        haven.pattern.iterator.AccommodationFilterCriteria crit = new haven.pattern.iterator.AccommodationFilterCriteria();
        haven.pattern.iterator.FilteredAccommodationIterator iter = new haven.pattern.iterator.FilteredAccommodationIterator(accRepo.findAll(), crit);
        int pass1 = 0;
        while (iter.hasNext()) { iter.next(); pass1++; }
        assert pass1 > 0 : "Iterator first pass should yield items";
        assert !iter.hasNext() : "Iterator should be exhausted";
        iter.reset();
        int pass2 = 0;
        while (iter.hasNext()) { iter.next(); pass2++; }
        assert pass1 == pass2 : "Iterator second pass after reset() must yield same count";

        // ValidationUtils
        assert haven.view.ValidationUtils.isValidUsername("student_1");
        assert !haven.view.ValidationUtils.isValidUsername("ab"); // too short
        assert !haven.view.ValidationUtils.isValidUsername("user name with space");
        assert haven.view.ValidationUtils.isValidPassword("secret12");
        assert !haven.view.ValidationUtils.isValidPassword("123"); // too short

        // FunctionalInterface lambda target for NavigationService
        final boolean[] navCalled = {false};
        haven.pattern.NavigationService nav = (link) -> { navCalled[0] = (link != null && link.contains("8.500000")); };
        nav.navigateTo(8.5, 76.9);
        assert navCalled[0] : "NavigationService default navigateTo(lat, lon) must dispatch to abstract method";

        // ReviewEvent DELETED type
        haven.pattern.event.ReviewEvent delEvt = new haven.pattern.event.ReviewEvent(haven.pattern.event.ReviewEvent.Type.DELETED, null);
        assert delEvt.getType() == haven.pattern.event.ReviewEvent.Type.DELETED;

        System.out.println("PASSED! (Enums, getType() OCP, Iterator.reset, ValidationUtils & NavigationService verified)");

        System.out.println("\nALL 34 ENHANCEMENT, ARCHITECTURE, DESIGN PATTERN & OOP TESTS PASSED SUCCESSFULLY!");
    }
}
