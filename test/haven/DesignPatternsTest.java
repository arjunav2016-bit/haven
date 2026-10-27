package haven.test;

import haven.db.EnquiryDAO;
import haven.db.HostelDAO;
import haven.db.ReviewDAO;
import haven.model.*;
import haven.pattern.*;
import haven.pattern.command.CommandHistory;
import haven.pattern.command.ModerateListingCommand;
import haven.pattern.decorator.FeaturedAccommodationDecorator;
import haven.pattern.event.*;
import haven.pattern.iterator.AccommodationFilterCriteria;
import haven.pattern.iterator.FilteredAccommodationIterator;
import haven.pattern.mediator.PanelMediator;
import haven.pattern.report.ApplicantRosterReport;
import haven.pattern.report.BookingReceiptReport;
import haven.pattern.report.SystemAnalyticsReport;
import haven.pattern.state.*;
import haven.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GoF Design Pattern Verifications")
public class DesignPatternsTest extends TestBase {

    @Test
    @DisplayName("[TEST 3] Receipt & Roster generation")
    public void test03_ReceiptAndRosterGeneration() {
        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        hostel.addAmenity("WiFi");
        hostel.addAmenity("Food / Mess");
        hostel.addAmenity("CCTV Security");

        Enquiry eq = new Enquiry("ENQ-12345", "BH01", "Sreedeepam Boys Hostel", "owner", "student", "9876543210", "Interested in 2-sharing room", new Date());
        User student = new Student("student", "pass123", "Minod Kumar", "9876543210", "SCTCE", "Boys", 8000.0, new ArrayList<>());

        String slip = ReceiptGenerator.generateEnquirySlipText(eq, hostel, student);
        assertTrue(slip.contains("ENQ-12345"), "Slip missing enquiry ID");
        assertTrue(slip.contains("Sreedeepam Boys Hostel"), "Slip missing hostel name");
        assertTrue(slip.contains("Minod Kumar"), "Slip missing student name");
        assertTrue(slip.contains("WiFi, Food / Mess, CCTV Security"), "Slip missing amenities");

        List<Enquiry> rosterList = new ArrayList<>();
        rosterList.add(eq);
        String roster = ReceiptGenerator.generateOwnerEnquiryRoster("owner", rosterList);
        assertTrue(roster.contains("ENQ-12345"), "Roster missing enquiry ID");
        assertTrue(roster.contains("Minod Kumar") || roster.contains("student"), "Roster missing student handle");
    }

    @Test
    @DisplayName("[TEST 10] Observer Pattern & HavenEventBus")
    public void test10_ObserverPatternAndHavenEventBus() {
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

        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        Enquiry eq = new Enquiry("ENQ-12345", "BH01", "Sreedeepam Boys Hostel", "owner", "student", "9876543210", "Interested in 2-sharing room", new Date());
        Review r1 = new Review("BH01", "student", "Minod", 5, "Excellent hostel!");

        bus.publish(new AccommodationEvent(AccommodationEvent.Type.UPDATED, hostel));
        bus.publish(new EnquiryEvent(EnquiryEvent.Type.SUBMITTED, eq));
        bus.publish(new ReviewEvent(r1));

        assertTrue(accommodationEventReceived.get(), "AccommodationEvent was not dispatched");
        assertTrue(enquiryEventReceived.get(), "EnquiryEvent was not dispatched");
        assertTrue(reviewEventReceived.get(), "ReviewEvent was not dispatched");

        bus.unsubscribe(AccommodationEvent.class, accListener);
        bus.unsubscribe(EnquiryEvent.class, eqListener);
        bus.unsubscribe(ReviewEvent.class, revListener);
    }

    @Test
    @DisplayName("[TEST 13] EventBus Auto-Publishing from DAO Facades (Deduplicated)")
    public void test13_EventBusAutoPublishingFromDaoFacades() {
        HavenEventBus bus = HavenEventBus.getInstance();
        AtomicInteger daoAccEventCount = new AtomicInteger(0);
        AtomicInteger daoReviewEventCount = new AtomicInteger(0);
        AtomicInteger daoEnquiryEventCount = new AtomicInteger(0);

        HavenEventListener<AccommodationEvent> daoAccListener = e -> daoAccEventCount.incrementAndGet();
        HavenEventListener<ReviewEvent> daoRevListener = e -> daoReviewEventCount.incrementAndGet();
        HavenEventListener<EnquiryEvent> daoEqListener = e -> daoEnquiryEventCount.incrementAndGet();

        bus.subscribe(AccommodationEvent.class, daoAccListener);
        bus.subscribe(ReviewEvent.class, daoRevListener);
        bus.subscribe(EnquiryEvent.class, daoEqListener);

        HostelDAO testHostelDAO = new HostelDAO();
        testHostelDAO.updateVacancy("BH01", 10);
        assertEquals(1, daoAccEventCount.get(), "HostelDAO.updateVacancy should fire exactly 1 AccommodationEvent");

        ReviewDAO testReviewDAO = new ReviewDAO();
        Review testReview = new Review("BH01", "test_auto", "Test Auto", 5, "Auto-publish test review");
        testReviewDAO.addReview(testReview);
        assertEquals(1, daoReviewEventCount.get(), "ReviewDAO.addReview should fire exactly 1 ReviewEvent");

        EnquiryDAO testEnquiryDAO = new EnquiryDAO();
        testEnquiryDAO.saveEnquiry("BH01", "Sreedeepam", "owner", "test_student", "9999999999", "EventBus auto-test");
        assertEquals(1, daoEnquiryEventCount.get(), "EnquiryDAO.saveEnquiry should fire exactly 1 EnquiryEvent");

        bus.unsubscribe(AccommodationEvent.class, daoAccListener);
        bus.unsubscribe(ReviewEvent.class, daoRevListener);
        bus.unsubscribe(EnquiryEvent.class, daoEqListener);

        // Cleanup test fixtures
        for (Enquiry eqCleanup : testEnquiryDAO.getEnquiriesForStudent("test_student")) {
            testEnquiryDAO.deleteEnquiry(eqCleanup.getEnquiryId());
        }
        for (Review revCleanup : testReviewDAO.getReviewsForAccommodation("BH01")) {
            if ("test_auto".equals(revCleanup.getUsername())) {
                testReviewDAO.deleteReview(revCleanup.getId());
            }
        }
    }

    @Test
    @DisplayName("[TEST 20] Builder Pattern (HostelBuilder & PGBuilder)")
    public void test20_BuilderPatternHostelAndPg() {
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

        assertEquals("HB-TEST", builtHostel.getId(), "HostelBuilder ID mismatch");
        assertTrue(builtHostel.isFeatured(), "HostelBuilder featured flag not set");
        assertEquals(7600.0, builtHostel.calculateTotalMonthlyCost(1), "HostelBuilder total monthly cost mismatch");

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

        assertEquals("PGB-TEST", builtPG.getId(), "PGBuilder ID mismatch");
        assertTrue(builtPG.isFoodIncluded(), "PGBuilder foodIncluded flag not set");
    }

    @Test
    @DisplayName("[TEST 21] State Pattern (EnquiryState Lifecycle & Terminal Guards)")
    public void test21_StatePatternLifecycleAndTerminalGuards() {
        Enquiry stateEnq = new Enquiry("ENQ-STATE", "BH01", "Sreedeepam", "owner", "student", "9876543210", "State test", new Date());
        assertEquals("PENDING", stateEnq.getStatus(), "Initial state should be PENDING");
        assertTrue(stateEnq.canTransitionTo("CONTACTED"), "PENDING should allow transition to CONTACTED");

        stateEnq.transitionTo("CONTACTED");
        assertEquals("CONTACTED", stateEnq.getStatus(), "Status should be CONTACTED");
        assertTrue(stateEnq.canTransitionTo("CONFIRMED"), "CONTACTED should allow transition to CONFIRMED");

        stateEnq.transitionTo("CONFIRMED");
        assertEquals("CONFIRMED", stateEnq.getStatus(), "Status should be CONFIRMED");
        assertFalse(stateEnq.canTransitionTo("CONTACTED"), "CONFIRMED state must reject backward transition to CONTACTED");

        stateEnq.transitionTo("CANCELLED");
        assertEquals("CANCELLED", stateEnq.getStatus(), "Status should be CANCELLED");
        assertTrue(stateEnq.getState().isTerminal(), "CANCELLED must be a terminal state");
        assertFalse(stateEnq.canTransitionTo("CONFIRMED"), "Terminal CANCELLED state must reject transition to CONFIRMED");

        assertThrows(IllegalStateException.class, () -> stateEnq.transitionTo("CONFIRMED"),
                "Invalid state transition from terminal CANCELLED to CONFIRMED must throw IllegalStateException");
        assertEquals("CANCELLED", stateEnq.getStatus(), "Status must remain CANCELLED");
    }

    @Test
    @DisplayName("[TEST 22] Command Pattern & CommandHistory (Execute / Undo / Redo)")
    public void test22_CommandPatternAndCommandHistory() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        CommandHistory cmdHistory = new CommandHistory();
        Accommodation modTarget = accRepo.findById("BH01").orElseThrow();
        String origStatus = modTarget.getModerationStatus();
        boolean origFeatured = modTarget.isFeatured();

        ModerateListingCommand modCmd = new ModerateListingCommand(accRepo, "BH01", "FLAGGED", !origFeatured);
        cmdHistory.executeCommand(modCmd);

        Accommodation afterExec = accRepo.findById("BH01").orElseThrow();
        assertEquals("FLAGGED", afterExec.getModerationStatus(), "Moderation status not updated to FLAGGED");
        assertEquals(!origFeatured, afterExec.isFeatured(), "Featured flag not toggled by command");

        assertTrue(cmdHistory.canUndo(), "CommandHistory should allow undo");
        cmdHistory.undo();
        Accommodation afterUndo = accRepo.findById("BH01").orElseThrow();
        assertEquals(origStatus, afterUndo.getModerationStatus(), "Undo failed to restore original moderationStatus");
        assertEquals(origFeatured, afterUndo.isFeatured(), "Undo failed to restore original featured state");

        assertTrue(cmdHistory.canRedo(), "CommandHistory should allow redo");
        cmdHistory.redo();
        Accommodation afterRedo = accRepo.findById("BH01").orElseThrow();
        assertEquals("FLAGGED", afterRedo.getModerationStatus(), "Redo failed to reapply FLAGGED status");

        // Restore clean state
        cmdHistory.undo();
    }

    @Test
    @DisplayName("[TEST 23] Template Method Pattern (ReportGenerator Hierarchy)")
    public void test23_TemplateMethodPatternReportGeneratorHierarchy() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        UserRepository userRepo = RepositoryProvider.getUserRepository();
        EnquiryRepository enquiryRepo = RepositoryProvider.getEnquiryRepository();
        ReviewRepository revRepo = RepositoryProvider.getReviewRepository();

        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        Enquiry eq = new Enquiry("ENQ-12345", "BH01", "Sreedeepam Boys Hostel", "owner", "student", "9876543210", "Interested in 2-sharing room", new Date());
        User student = new Student("student", "pass123", "Minod Kumar", "9876543210", "SCTCE", "Boys", 8000.0, new ArrayList<>());
        List<Enquiry> rosterList = List.of(eq);

        BookingReceiptReport receiptReport = new BookingReceiptReport(eq, hostel, student);
        ApplicantRosterReport rosterReport = new ApplicantRosterReport("owner", rosterList);
        SystemAnalyticsReport analyticsReport = new SystemAnalyticsReport(
                userRepo.findAll(), accRepo.findAll(), enquiryRepo.findAll(), revRepo.findAll());

        String rOut1 = receiptReport.generate();
        String rOut2 = rosterReport.generate();
        String rOut3 = analyticsReport.generate();

        assertTrue(rOut1.contains("ENQUIRY"), "BookingReceiptReport header missing");
        assertTrue(rOut2.contains("ROSTER"), "ApplicantRosterReport header missing");
        assertTrue(rOut3.contains("SYSTEM-WIDE ANALYTICS"), "SystemAnalyticsReport header missing");
    }

    @Test
    @DisplayName("[TEST 24] Decorator Pattern (FeaturedAccommodationDecorator)")
    public void test24_DecoratorPatternFeaturedAccommodation() throws Exception {
        Hostel builtHostel = Hostel.builder()
                .id("HB-DEC")
                .ownerId("owner")
                .name("Decorator Test Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(5000)
                .distanceKm(0.5)
                .genderAllowed("Boys")
                .vacancies(6)
                .coordinates(8.4712, 76.9788)
                .fixedMessFee(2000)
                .curfewTime("10:00 PM")
                .address("Pappanamcode")
                .contactPhone("9447112233")
                .addAmenity("WiFi")
                .build();

        FeaturedAccommodationDecorator decorated = new FeaturedAccommodationDecorator(builtHostel, "TOP RATED NEAR SCTCE", 10.0);
        assertTrue(decorated.getHighlightBadgeText().contains("TOP RATED NEAR SCTCE"), "Decorator badge text mismatch");
        assertTrue(decorated.isFeatured(), "Decorator should mark accommodation as featured");
        assertEquals(Math.round(builtHostel.calculateTotalMonthlyCost(1) * 0.90), decorated.calculateTotalMonthlyCost(1),
                "Decorator 10% promotional discount failed");
        assertEquals(Math.round(builtHostel.getBaseRent() * 0.90), decorated.getBaseRent(),
                "Decorator base rent discount failed");
        assertEquals(builtHostel.getBaseRent(), decorated.getOriginalBaseRent(),
                "Decorator original base rent mismatch");

        // Verify complete delegation to wrapped accommodation
        assertEquals(builtHostel.getVacancies(), decorated.getVacancies(), "Decorator getVacancies must delegate");
        assertTrue(decorated.hasAmenity("WiFi"), "Decorator hasAmenity must delegate");
        assertEquals(builtHostel.getAmenities(), decorated.getAmenities(), "Decorator getAmenities must delegate");

        int vacBeforeDec = builtHostel.getVacancies();
        decorated.decrementVacancy();
        assertEquals(vacBeforeDec - 1, builtHostel.getVacancies(), "decrementVacancy on decorator must mutate underlying accommodation");
        builtHostel.setVacancies(vacBeforeDec);
    }

    @Test
    @DisplayName("[TEST 25] Iterator Pattern (FilteredAccommodationIterator)")
    public void test25_IteratorPatternFilteredAccommodationIterator() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        AccommodationFilterCriteria criteria = new AccommodationFilterCriteria()
                .setCollege("SCTCE")
                .setAccommodationType("HOSTEL")
                .setMaxRent(6000)
                .setVacanciesOnly(true);

        FilteredAccommodationIterator filterIterator = new FilteredAccommodationIterator(accRepo.findAll(), criteria);
        List<Accommodation> iterResults = filterIterator.toList();
        assertFalse(iterResults.isEmpty(), "FilteredAccommodationIterator should match SCTCE hostels under 6000");

        for (Accommodation a : iterResults) {
            assertTrue(a instanceof Hostel, "Iterator returned non-Hostel when filtered by HOSTEL");
            assertTrue(a.getBaseRent() <= 6000, "Iterator returned stay exceeding maxRent 6000");
            assertTrue(a.getVacancies() > 0, "Iterator returned stay with 0 vacancies");
        }

        // Multi-pass Iterable support
        int iterPass1 = 0;
        for (Accommodation a : filterIterator) iterPass1++;
        int iterPass2 = 0;
        for (Accommodation a : filterIterator) iterPass2++;
        assertTrue(iterPass1 > 0 && iterPass1 == iterPass2,
                "FilteredAccommodationIterator must support multiple for-each passes via new Iterator");

        // Verify approvedOnly excludes PENDING and FLAGGED stays
        Hostel flaggedStay = new Hostel("FLG-TEST", "owner", "Pending Stay", "SCTCE", 4000, 1.0, "Boys", 5, 8.4, 76.9, 2000, "10 PM", "Addr", "12345");
        flaggedStay.setModerationStatus(ModerationStatus.FLAGGED);
        AccommodationFilterCriteria approvedCriteria = new AccommodationFilterCriteria().setApprovedOnly(true);
        FilteredAccommodationIterator approvedIterator = new FilteredAccommodationIterator(List.of(flaggedStay), approvedCriteria);
        assertFalse(approvedIterator.hasNext(), "setApprovedOnly(true) must exclude FLAGGED stays");

        // Canonical amenity matching
        Hostel powerStay = new Hostel("PWR-TEST", "owner", "Power Stay", "SCTCE", 4000, 1.0, "Boys", 5, 8.4, 76.9, 2000, "10 PM", "Addr", "12345");
        powerStay.addAmenity("Power Backup");
        assertFalse(powerStay.hasAmenity("AC"), "hasAmenity('AC') must NOT match 'Power Backup'");
        assertTrue(powerStay.hasAmenity("Power Backup"), "hasAmenity('Power Backup') must match");
        assertTrue(powerStay.hasAmenity("Power"), "hasAmenity('Power') canonical alias must match");
    }

    @Test
    @DisplayName("[TEST 26] Mediator Pattern (PanelMediator Cross-View Coordination)")
    public void test26_MediatorPatternPanelMediator() {
        AtomicBoolean mapFocused = new AtomicBoolean(false);
        PanelMediator testMediator = new PanelMediator() {
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
        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        testMediator.showMapView(hostel);
        assertTrue(mapFocused.get(), "PanelMediator failed to route showMapView callback");
    }

    @Test
    @DisplayName("[TEST 31] State Pattern Full Transition Matrix (5 States x 5 Targets)")
    public void test31_StatePatternFullTransitionMatrix() {
        EnquiryState[] allStates = {
                new PendingState(),
                new ContactedState(),
                new ConfirmedState(),
                new DeclinedState(),
                new CancelledState()
        };
        String[] allTargets = {"PENDING", "CONTACTED", "CONFIRMED", "DECLINED", "CANCELLED"};

        // Expected canTransitionTo matrix [fromState][toTarget]:
        // PENDING:    P=T, CO=T, CF=T, D=T, CA=T    (open state, allows all)
        // CONTACTED:  P=F, CO=T, CF=T, D=T, CA=T    (no backward to PENDING)
        // CONFIRMED:  P=F, CO=F, CF=T, D=F, CA=T    (self + cancel only)
        // DECLINED:   P=F, CO=F, CF=F, D=T, CA=F    (terminal, only self)
        // CANCELLED:  P=F, CO=F, CF=F, D=F, CA=T    (terminal, only self)
        boolean[][] expectedMatrix = {
                {true,  true,  true,  true,  true },  // PENDING
                {false, true,  true,  true,  true },  // CONTACTED
                {false, false, true,  false, true },  // CONFIRMED
                {false, false, false, true,  false},  // DECLINED
                {false, false, false, false, true },  // CANCELLED
        };

        for (int i = 0; i < allStates.length; i++) {
            for (int j = 0; j < allTargets.length; j++) {
                boolean actual = allStates[i].canTransitionTo(allTargets[j]);
                assertEquals(expectedMatrix[i][j], actual,
                        String.format("State matrix mismatch: %s -> %s", allStates[i].getStatusName(), allTargets[j]));
            }
        }
    }

    @Test
    @DisplayName("[TEST 32] State Pattern Terminal Idempotency & Edge Cases")
    public void test32_StatePatternTerminalIdempotencyAndEdgeCases() {
        // Double-cancel: CancelledState.cancel() should return self (idempotent)
        EnquiryState cancelledState = new CancelledState();
        EnquiryState cancelledAgain = cancelledState.cancel();
        assertSame(cancelledState, cancelledAgain, "Double-cancel must return same CancelledState instance");
        assertTrue(cancelledState.isTerminal(), "CancelledState must be terminal");

        // Double-decline: DeclinedState.decline() should return self (idempotent)
        EnquiryState declinedState = new DeclinedState();
        EnquiryState declinedAgain = declinedState.decline();
        assertSame(declinedState, declinedAgain, "Double-decline must return same DeclinedState instance");
        assertTrue(declinedState.isTerminal(), "DeclinedState must be terminal");

        // Self-transition on non-terminal states
        EnquiryState pendingSelf = new PendingState().transitionTo("PENDING");
        assertEquals("PENDING", pendingSelf.getStatusName(), "PENDING self-transition must stay PENDING");

        EnquiryState contactedSelf = new ContactedState().transitionTo("CONTACTED");
        assertEquals("CONTACTED", contactedSelf.getStatusName(), "CONTACTED self-transition must stay CONTACTED");

        // Terminal states must throw on all illegal targets
        for (String target : new String[]{"CONTACTED", "CONFIRMED", "CANCELLED"}) {
            assertThrows(IllegalStateException.class, () -> declinedState.transitionTo(target),
                    "DeclinedState.transitionTo(" + target + ") must throw IllegalStateException");
        }

        for (String target : new String[]{"CONTACTED", "CONFIRMED", "DECLINED"}) {
            assertThrows(IllegalStateException.class, () -> cancelledState.transitionTo(target),
                    "CancelledState.transitionTo(" + target + ") must throw IllegalStateException");
        }
    }

    @Test
    @DisplayName("[TEST 36] Adapter Pattern (GoogleMapsAdapter & OpenStreetMapAdapter)")
    public void test36_AdapterPatternGoogleMapsAndOpenStreetMap() {
        GoogleMapsAdapter gMapsAdapter = new GoogleMapsAdapter();
        assertTrue(gMapsAdapter instanceof NavigationService, "GoogleMapsAdapter must implement NavigationService");

        double[] osmCoordsDispatched = {0.0, 0.0};
        OpenStreetMapAdapter osmAdapter = new OpenStreetMapAdapter((lat, lon) -> {
            osmCoordsDispatched[0] = lat;
            osmCoordsDispatched[1] = lon;
        });
        assertTrue(osmAdapter instanceof NavigationService, "OpenStreetMapAdapter must implement NavigationService");
        osmAdapter.navigateTo(8.4708, 76.9804);
        assertEquals(8.4708, osmCoordsDispatched[0], 0.0001, "OpenStreetMapAdapter latitude dispatched failed");
        assertEquals(76.9804, osmCoordsDispatched[1], 0.0001, "OpenStreetMapAdapter longitude dispatched failed");
    }
}
