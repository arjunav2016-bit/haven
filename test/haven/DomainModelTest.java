package haven.test;

import haven.model.*;
import haven.service.map.NavigationService;
import haven.event.ReviewEvent;
import haven.service.filter.AccommodationFilterCriteria;
import haven.service.filter.FilteredAccommodationIterator;
import haven.repository.AccommodationRepository;
import haven.repository.RepositoryProvider;
import haven.repository.UserRepository;
import haven.service.AccommodationService;
import haven.service.ServiceProvider;
import haven.view.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Model & Core OOP Tests")
public class DomainModelTest extends TestBase {

    @Test
    @DisplayName("[TEST 1] Review star display & ratings")
    public void test01_ReviewStarDisplayAndRatings() {
        Review r1 = new Review("BH01", "student", "Minod", 5, "Excellent hostel!");
        Review r2 = new Review("BH01", "student2", "Arjun", 4, "Good facilities.");
        assertEquals("★★★★★", r1.getStarsDisplay(), "Star display failed for 5 stars");
        assertEquals("★★★★☆", r2.getStarsDisplay(), "Star display failed for 4 stars");
    }

    @Test
    @DisplayName("[TEST 2] Amenity management & matching")
    public void test02_AmenityManagementAndMatching() {
        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        hostel.addAmenity("WiFi");
        hostel.addAmenity("Food / Mess");
        hostel.addAmenity("CCTV Security");

        assertTrue(hostel.hasAmenity("WiFi"), "WiFi check failed");
        assertTrue(hostel.hasAmenity("food / mess"), "Case-insensitive amenity check failed");
        assertFalse(hostel.hasAmenity("Swimming Pool"), "Should not have Swimming Pool");
        assertTrue(hostel.hasAllAmenities(List.of("WiFi", "Food / Mess")), "hasAllAmenities failed");
        assertFalse(hostel.hasAllAmenities(List.of("WiFi", "Swimming Pool")), "hasAllAmenities negative check failed");
    }

    @Test
    @DisplayName("[TEST 4] Side-by-Side comparison metrics & Polymorphism")
    public void test04_SideBySideComparisonMetrics() {
        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        PG pg = new PG("BPG01", "owner", "Atham PG", "SCTCE", 4500, 0.9, "Boys", 12, 8.4, 76.9, true, 500, "Pappanamcode", "8848726001");
        pg.addAmenity("WiFi");
        pg.addAmenity("Attached Washroom");
        pg.addAmenity("Power Backup");

        double hostelCost = hostel.calculateTotalMonthlyCost(1);
        double pgCost = pg.calculateTotalMonthlyCost(1);
        assertEquals(4500 + 2500, hostelCost, "Hostel polymorphic cost calculation failed");
        assertEquals(4500 + 500 + 2200, pgCost, "PG polymorphic cost calculation failed");
    }

    @Test
    @DisplayName("[TEST 7] Domain invariants validation (baseRent > 0, vacancies >= 0)")
    public void test07_DomainInvariantsValidation() {
        assertThrows(IllegalArgumentException.class, () ->
            new Hostel("INVALID", "owner", "Negative Rent Hostel", "SCTCE", -500, 1.0, "Boys", 5, 8.4, 76.9, 2000, "9 PM", "Test", "1234567890"),
            "Constructing accommodation with negative rent should throw IllegalArgumentException"
        );

        Hostel testStay = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        assertThrows(IllegalArgumentException.class, () -> testStay.setVacancies(-2),
            "Setting negative vacancies should throw IllegalArgumentException"
        );
    }

    @Test
    @DisplayName("[TEST 15] Geographic Coordinates Preservation & Mutation")
    public void test15_GeographicCoordinatesPreservationAndMutation() {
        Hostel coordHostel = new Hostel("CH01", "owner", "Coord Hostel", "CET", 5000, 1.2, "Boys", 5, 8.545, 76.905, 2000, "9 PM", "Trivandrum", "9446684604");
        assertEquals(8.545, coordHostel.getLatitude(), 0.0001, "Hostel constructor coordinate latitude failed");
        assertEquals(76.905, coordHostel.getLongitude(), 0.0001, "Hostel constructor coordinate longitude failed");
        coordHostel.setCoordinates(8.550, 76.910);
        assertEquals(8.550, coordHostel.getLatitude(), 0.0001, "Hostel setCoordinates latitude failed");
        assertEquals(76.910, coordHostel.getLongitude(), 0.0001, "Hostel setCoordinates longitude failed");

        PG coordPG = new PG("CPG01", "owner", "Coord PG", "CET", 4800, 1.0, "Girls", 4, 8.540, 76.900, true, 300, "Trivandrum", "8848726001");
        assertEquals(8.540, coordPG.getLatitude(), 0.0001, "PG constructor coordinate latitude failed");
        assertEquals(76.900, coordPG.getLongitude(), 0.0001, "PG constructor coordinate longitude failed");
        coordPG.setCoordinates(8.542, 76.902);
        assertEquals(8.542, coordPG.getLatitude(), 0.0001, "PG setCoordinates latitude failed");
        assertEquals(76.902, coordPG.getLongitude(), 0.0001, "PG setCoordinates longitude failed");
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("[TEST 16] Polymorphic Cost Calculation & Alias")
    public void test16_PolymorphicCostCalculationAndAlias() {
        Hostel hostel = new Hostel("BH01", "owner", "Sreedeepam Boys Hostel", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        PG pg = new PG("BPG01", "owner", "Atham PG", "SCTCE", 4500, 0.9, "Boys", 12, 8.4, 76.9, true, 500, "Pappanamcode", "8848726001");

        double hCost1 = hostel.calculateTotalMontlyCost(1);
        double hCost2 = hostel.calculateTotalMonthlyCost(1);
        assertEquals(hCost1, hCost2, "calculateTotalMonthlyCost alias does not match calculateTotalMontlyCost for Hostel");

        double pgCost1 = pg.calculateTotalMontlyCost(1);
        double pgCost2 = pg.calculateTotalMonthlyCost(1);
        assertEquals(pgCost1, pgCost2, "calculateTotalMonthlyCost alias does not match calculateTotalMontlyCost for PG");
    }

    @Test
    @DisplayName("[TEST 17] Vacancy Decrement Single-Decrement Guarantee")
    public void test17_VacancyDecrementSingleDecrementGuarantee() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        AccommodationService accService = ServiceProvider.getAccommodationService();
        Accommodation targetAcc = accRepo.findAll().get(0);
        int beforeVac = targetAcc.getVacancies();
        if (beforeVac == 0) {
            accService.updateVacancies(targetAcc.getId(), 5);
            targetAcc = accRepo.findById(targetAcc.getId()).orElse(null);
            assertNotNull(targetAcc);
            beforeVac = targetAcc.getVacancies();
        }
        boolean decSuccess = accService.decrementVacancy(targetAcc.getId());
        assertTrue(decSuccess, "Decrementing vacancy failed");
        Accommodation updatedAcc = accRepo.findById(targetAcc.getId()).orElse(null);
        assertNotNull(updatedAcc, "Accommodation not found after decrement");
        int afterVac = updatedAcc.getVacancies();
        assertEquals(beforeVac - 1, afterVac, "Vacancy should decrease by exactly 1");
    }

    @Test
    @DisplayName("[TEST 33] Encapsulation, Defensive Copies & Domain Identity")
    public void test33_EncapsulationDefensiveCopiesAndIdentity() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        UserRepository userRepo = RepositoryProvider.getUserRepository();

        Accommodation sampleStay = accRepo.findAll().get(0);
        int origAmenityCount = sampleStay.getAmenities().size();
        try {
            sampleStay.getAmenities().add("HACK_AMENITY");
        } catch (UnsupportedOperationException ignored) {}
        assertEquals(origAmenityCount, sampleStay.getAmenities().size(), "getAmenities() must return defensive copy");

        int origImageCount = sampleStay.getImagePaths().size();
        try {
            sampleStay.getImagePaths().add("HACK_PATH");
        } catch (UnsupportedOperationException ignored) {}
        assertEquals(origImageCount, sampleStay.getImagePaths().size(), "getImagePaths() must return defensive copy");

        User studentUser = userRepo.findById("student").orElseThrow();
        int origWishCount = studentUser.getWishlist().size();
        try {
            studentUser.getWishlist().add("HACK_ID");
        } catch (UnsupportedOperationException ignored) {}
        assertEquals(origWishCount, studentUser.getWishlist().size(), "getWishlist() must return defensive copy");

        Accommodation copyStay = new Hostel(sampleStay.getId(), "owner", "Diff Name", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        assertEquals(sampleStay, copyStay, "Accommodation.equals() must match on ID");
        assertEquals(sampleStay.hashCode(), copyStay.hashCode(), "Accommodation.hashCode() must match on ID");

        User copyUser = new Student("STUDENT", "otherpass", "Other Name", "999", new ArrayList<>());
        assertEquals(studentUser, copyUser, "User.equals() must match on case-insensitive username");
        assertEquals(studentUser.hashCode(), copyUser.hashCode(), "User.hashCode() must match on case-insensitive username");

        Enquiry enquiry = new Enquiry("e1", "BH01", "student", "owner1", "Hi", "PENDING", new Date(1000000L));
        Date d1 = enquiry.getCreatedAt();
        d1.setTime(99999999999L);
        assertEquals(1000000L, enquiry.getCreatedAt().getTime(), "Enquiry.getCreatedAt() must return defensive Date copy");
    }

    @Test
    @DisplayName("[TEST 34] Enums, Polymorphic getType(), ValidationUtils & Iterator Reset")
    public void test34_EnumsPolymorphicGetTypeAndValidationUtils() {
        assertSame(Role.STUDENT, Role.fromString("student"));
        assertSame(Role.OWNER, Role.fromString("OWNER"));
        assertSame(Role.ADMIN, Role.fromString("Admin"));
        assertTrue(Role.STUDENT.matches("student"));
        assertSame(ModerationStatus.APPROVED, ModerationStatus.fromString("approved"));

        Hostel testHostel = new Hostel("H01", "owner", "H", "SCTCE", 4500, 0.8, "Boys", 10, 8.4, 76.9, 2500, "9:30 PM", "Pappanamcode", "9446684604");
        PG testPG = new PG("P01", "owner", "P", "SCTCE", 4500, 0.9, "Boys", 12, 8.4, 76.9, true, 500, "Pappanamcode", "8848726001");
        assertEquals("Hostel", testHostel.getType(), "Hostel.getType() must return 'Hostel'");
        assertEquals("PG", testPG.getType(), "PG.getType() must return 'PG'");

        AccommodationFilterCriteria crit = new AccommodationFilterCriteria();
        FilteredAccommodationIterator iter = new FilteredAccommodationIterator(RepositoryProvider.getAccommodationRepository().findAll(), crit);
        int pass1 = 0;
        while (iter.hasNext()) { iter.next(); pass1++; }
        assertTrue(pass1 > 0, "Iterator first pass should yield items");
        assertFalse(iter.hasNext(), "Iterator should be exhausted");
        iter.reset();
        int pass2 = 0;
        while (iter.hasNext()) { iter.next(); pass2++; }
        assertEquals(pass1, pass2, "Iterator second pass after reset() must yield same count");

        assertTrue(ValidationUtils.isValidUsername("student_1"));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("user name with space"));
        assertTrue(ValidationUtils.isValidPassword("secret12"));
        assertFalse(ValidationUtils.isValidPassword("123"));

        final boolean[] navCalled = {false};
        NavigationService nav = (link) -> { navCalled[0] = (link != null && link.contains("8.500000")); };
        nav.navigateTo(8.5, 76.9);
        assertTrue(navCalled[0], "NavigationService default navigateTo(lat, lon) must dispatch to abstract method");

        ReviewEvent delEvt = new ReviewEvent(ReviewEvent.Type.DELETED, null);
        assertEquals(ReviewEvent.Type.DELETED, delEvt.getType());
    }
}
