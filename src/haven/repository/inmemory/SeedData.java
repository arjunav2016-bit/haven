package haven.repository.inmemory;

import haven.model.Accommodation;
import haven.model.Hostel;
import haven.model.PG;
import haven.repository.mongo.MongoDBConnection;
import haven.util.PropertyImageStorage;
import haven.repository.AccommodationRepository;
import haven.repository.RepositoryProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Initializes default SCTCE 2026 college-approved hostels and PGs.
 */
public class SeedData {
    private static final Logger LOGGER = Logger.getLogger(SeedData.class.getName());

    private static void applyAmenities(Accommodation acc, boolean cctv, boolean study) {
        acc.addAmenity("WiFi");
        if (acc instanceof Hostel || (acc instanceof PG && ((PG) acc).isFoodIncluded())) {
            acc.addAmenity("Food / Mess");
        }
        acc.addAmenity("Attached Washroom");
        acc.addAmenity("Power Backup");
        if (cctv) {
            acc.addAmenity("CCTV Security");
        }
        if (study) {
            acc.addAmenity("Study Room");
        }
        PropertyImageStorage.ensureDefaultImagesForAccommodation(acc);
    }

    public static List<Accommodation> getSCTCEApprovedHostels() {
        List<Accommodation> list = new ArrayList<>();

        // 1. Sreedeepam Boys Hostel
        Hostel h1 = Hostel.builder()
                .id("BH01")
                .ownerId("Sri. Sreeju N V")
                .name("Sreedeepam Boys Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(0.8)
                .genderAllowed("Boys")
                .vacancies(47)
                .coordinates(8.4645, 76.9796)
                .fixedMessFee(2500.0)
                .curfewTime("9:30 PM")
                .address("Sreedepam, TC 54/2036 Kairali Garden, Viswambharan Road, Pappanamcode")
                .contactPhone("9446684604")
                .featured(true)
                .build();
        applyAmenities(h1, true, true);
        list.add(h1);

        // 2. Vipas Hostel
        Hostel h2 = Hostel.builder()
                .id("BH02")
                .ownerId("Sri. Anil Kumar")
                .name("Vipas Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(5200.0)
                .distanceKm(0.8)
                .genderAllowed("Boys")
                .vacancies(5)
                .coordinates(8.4650, 76.9793)
                .fixedMessFee(2800.0)
                .curfewTime("9:30 PM")
                .address("VIPAS HOUSE, IRA-76, Kairali Garden, Viswambharan Road, Pappanamcode")
                .contactPhone("7012380210")
                .build();
        applyAmenities(h2, true, true);
        list.add(h2);

        // 3. Akshay Hostel
        Hostel h3 = Hostel.builder()
                .id("BH03")
                .ownerId("Smt. Bindhu")
                .name("Akshay Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(0.7)
                .genderAllowed("Boys")
                .vacancies(20)
                .coordinates(8.4762, 76.9757)
                .fixedMessFee(2500.0)
                .curfewTime("9:30 PM")
                .address("Amritha Lane, Kaimanam, Pappanamcode")
                .contactPhone("8129795560, 8848567459")
                .build();
        applyAmenities(h3, false, true);
        list.add(h3);

        // 4. Castello Boys Hostel
        Hostel h4 = Hostel.builder()
                .id("BH04")
                .ownerId("Smt. Leena")
                .name("Castello Boys Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4800.0)
                .distanceKm(0.6)
                .genderAllowed("Boys")
                .vacancies(6)
                .coordinates(8.4741, 76.9761)
                .fixedMessFee(2700.0)
                .curfewTime("9:30 PM")
                .address("TC 52/1904, Kaimanam, Thiruvananthapuram-40")
                .contactPhone("8281412050")
                .build();
        applyAmenities(h4, true, false);
        list.add(h4);

        // 5. Atham PG
        PG pg1 = PG.builder()
                .id("BPG01")
                .ownerId("S. Nagarajan")
                .name("Atham PG")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(0.9)
                .genderAllowed("Boys")
                .vacancies(12)
                .coordinates(8.4720, 76.9835)
                .foodIncluded(true)
                .utilityCharges(500.0)
                .address("SivaLathikam, TC 52/3245, Inchippulluvila, Pappanamcode")
                .contactPhone("8848726001")
                .featured(true)
                .build();
        applyAmenities(pg1, false, false);
        list.add(pg1);

        // 6. Niaz Hostel
        Hostel h6 = Hostel.builder()
                .id("BH06")
                .ownerId("Sri. M K Nazar")
                .name("Niaz Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(1.4)
                .genderAllowed("Boys")
                .vacancies(40)
                .coordinates(8.4645, 76.9901)
                .fixedMessFee(2500.0)
                .curfewTime("9:30 PM")
                .address("Niyas Building, Karakkamandapam, Nemom P O")
                .contactPhone("9446384740, 8848096564")
                .build();
        applyAmenities(h6, false, true);
        list.add(h6);

        // 7. Panchami Boys Hostel
        Hostel h7 = Hostel.builder()
                .id("BH07")
                .ownerId("Sajeev K S")
                .name("Panchami Boys Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4000.0)
                .distanceKm(0.5)
                .genderAllowed("Boys")
                .vacancies(11)
                .coordinates(8.4728, 76.9906)
                .fixedMessFee(2500.0)
                .curfewTime("9:30 PM")
                .address("Panchami, Estate Road, Pappanamcode")
                .contactPhone("9633100600")
                .build();
        applyAmenities(h7, true, false);
        list.add(h7);

        // 8. Deepa Prabha Ladies Hostel
        Hostel gh1 = Hostel.builder()
                .id("GH01")
                .ownerId("Smt. Linsa")
                .name("Deepa Prabha Ladies Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(0.5)
                .genderAllowed("Girls")
                .vacancies(7)
                .coordinates(8.4697, 76.9818)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("Deepa Prabha, Opposite Aster Lab, Near Whitedammar, NH Road side, Pappanamcode")
                .contactPhone("9633391299, 9995460253")
                .featured(true)
                .build();
        applyAmenities(gh1, true, true);
        list.add(gh1);

        // 9. Sai Hostel
        Hostel gh2 = Hostel.builder()
                .id("GH02")
                .ownerId("Smt. Sreeja Chandran")
                .name("Sai Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4000.0)
                .distanceKm(0.6)
                .genderAllowed("Girls")
                .vacancies(20)
                .coordinates(8.4697, 76.9844)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("TC 53/1965, Parthipuri, Pappanamcode, Opposite Cavaliar India Coaching Centre")
                .contactPhone("9497853750, 7306842191")
                .build();
        applyAmenities(gh2, true, false);
        list.add(gh2);

        // 10. Chinnus Hostel
        Hostel gh3 = Hostel.builder()
                .id("GH03")
                .ownerId("Smt. Usha Rajkumar")
                .name("Chinnus Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4200.0)
                .distanceKm(0.7)
                .genderAllowed("Girls")
                .vacancies(15)
                .coordinates(8.4724, 76.9797)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("TC 53/110, Palace Lane, Opposite KSRTC Central Work, Pappanamcode")
                .contactPhone("9496260318, 6238153429")
                .build();
        applyAmenities(gh3, true, true);
        list.add(gh3);

        // 11. Arafa BETA Girls Hostel
        Hostel gh4 = Hostel.builder()
                .id("GH04")
                .ownerId("Smt. Naja.R S")
                .name("Arafa BETA Girls Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(0.4)
                .genderAllowed("Girls")
                .vacancies(14)
                .coordinates(8.4697, 76.9825)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("ARAFA, Anugraha, Near Pappanamcode market")
                .contactPhone("8137983167, 8848790950")
                .build();
        applyAmenities(gh4, true, true);
        list.add(gh4);

        // 12. Sudarsanam Ladies Hostel
        Hostel gh5 = Hostel.builder()
                .id("GH05")
                .ownerId("Smt. Suja Rani")
                .name("Sudarsanam Ladies Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4000.0)
                .distanceKm(0.9)
                .genderAllowed("Girls")
                .vacancies(25)
                .coordinates(8.4709, 76.9775)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("KNRA-90, Vivek Nagar, NSS Karayogam Road, Kaimanam")
                .contactPhone("9846724455")
                .build();
        applyAmenities(gh5, true, false);
        list.add(gh5);

        // 13. Sree Sakthi Ladies Hostel
        Hostel gh6 = Hostel.builder()
                .id("GH06")
                .ownerId("Smt. Bindhu")
                .name("Sree Sakthi Ladies Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4200.0)
                .distanceKm(0.8)
                .genderAllowed("Girls")
                .vacancies(50)
                .coordinates(8.4712, 76.9770)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("VS Apartments, Chirakara Lane, Kaimanam, Pappanamcode")
                .contactPhone("8129795560, 8848567459")
                .build();
        applyAmenities(gh6, true, true);
        list.add(gh6);

        // 14. GBR Hostel
        Hostel gh7 = Hostel.builder()
                .id("GH07")
                .ownerId("Smt. Rema A")
                .name("GBR Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(0.8)
                .genderAllowed("Girls")
                .vacancies(13)
                .coordinates(8.4710, 76.9772)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("TC 52/46, Padmanabham, Chirakara Lane, Kaimanam, Pappanamcode")
                .contactPhone("9497268740, 6282991466")
                .build();
        applyAmenities(gh7, true, false);
        list.add(gh7);

        // 15. Aashiyana Hostel
        Hostel gh8 = Hostel.builder()
                .id("GH08")
                .ownerId("Smt. Reetha")
                .name("Aashiyana Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4500.0)
                .distanceKm(1.4)
                .genderAllowed("Girls")
                .vacancies(34)
                .coordinates(8.4643, 76.9906)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("Karakkamandapam junction, Nemom")
                .contactPhone("8137872437, 9447229223")
                .build();
        applyAmenities(gh8, true, false);
        list.add(gh8);

        // 16. Panchami Girls Hostel
        Hostel gh9 = Hostel.builder()
                .id("GH09")
                .ownerId("Sajeev K S")
                .name("Panchami Girls Hostel")
                .nearbyCollege("SCTCE")
                .baseRent(4000.0)
                .distanceKm(0.5)
                .genderAllowed("Girls")
                .vacancies(16)
                .coordinates(8.4728, 76.9906)
                .fixedMessFee(2500.0)
                .curfewTime("8:30 PM")
                .address("Panchami, Estate Road, Pappanamcode")
                .contactPhone("9633100600")
                .build();
        applyAmenities(gh9, true, false);
        list.add(gh9);

        return list;
    }

    public static void main(String[] args) {
        LOGGER.info("Connecting to MongoDB and seeding SCTCE 2026 approved hostels data...");
        AccommodationRepository repo = RepositoryProvider.getAccommodationRepository();

        MongoDBConnection.getInstance().getDatabase().getCollection("hostels").drop();

        List<Accommodation> approvedListings = getSCTCEApprovedHostels();
        for (Accommodation acc : approvedListings) {
            repo.save(acc);
        }

        LOGGER.info(() -> "Database successfully seeded with " + approvedListings.size() + " SCTCE approved accommodations (7 Boys + 9 Girls)!");
        MongoDBConnection.getInstance().close();
    }
}