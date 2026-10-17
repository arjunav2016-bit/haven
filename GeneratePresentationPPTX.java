import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GeneratePresentationPPTX {

    private static int shapeIdCounter = 10;

    public static void main(String[] args) {
        try {
            File outFile = new File("haven_presentation.pptx");
            System.out.println("Generating B.Tech Project Presentation: " + outFile.getAbsolutePath());

            List<String> slidesXml = buildAllSlides();

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile))) {
                addEntry(zos, "[Content_Types].xml", buildContentTypesXml(slidesXml.size()));
                addEntry(zos, "_rels/.rels", buildRootRelsXml());
                addEntry(zos, "ppt/_rels/presentation.xml.rels", buildPresentationRelsXml(slidesXml.size()));
                addEntry(zos, "ppt/presentation.xml", buildPresentationXml(slidesXml.size()));
                addEntry(zos, "ppt/slideMasters/_rels/slideMaster1.xml.rels", buildSlideMasterRelsXml());
                addEntry(zos, "ppt/slideMasters/slideMaster1.xml", buildSlideMasterXml());
                addEntry(zos, "ppt/slideLayouts/_rels/slideLayout1.xml.rels", buildSlideLayout1RelsXml());
                addEntry(zos, "ppt/slideLayouts/slideLayout1.xml", buildSlideLayout1Xml());
                addEntry(zos, "ppt/theme/theme1.xml", buildThemeXml());

                for (int i = 0; i < slidesXml.size(); i++) {
                    int slideNum = i + 1;
                    addEntry(zos, "ppt/slides/_rels/slide" + slideNum + ".xml.rels", buildSlideRelsXml());
                    addEntry(zos, "ppt/slides/slide" + slideNum + ".xml", slidesXml.get(i));
                }
            }

            System.out.println("SUCCESS! Generated " + slidesXml.size() + " slides in haven_presentation.pptx (" + outFile.length() + " bytes)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addEntry(ZipOutputStream zos, String name, String content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static List<String> buildAllSlides() {
        List<String> list = new ArrayList<>();
        int total = 16;

        // Slide 1: Title
        list.add(buildSlide1(1, total));
        // Slide 2: Abstract
        list.add(buildSlide2(2, total));
        // Slide 3: Background & Problem Statement
        list.add(buildSlide3(3, total));
        // Slide 4: Objectives & Scope
        list.add(buildSlide4(4, total));
        // Slide 5: System Architecture
        list.add(buildSlide5(5, total));
        // Slide 6: Technology Stack
        list.add(buildSlide6(6, total));
        // Slide 7: 4 Pillars of OOP
        list.add(buildSlide7(7, total));
        // Slide 8: 14 Design Patterns Master Table
        list.add(buildSlide8(8, total));
        // Slide 9: Creational Patterns
        list.add(buildSlide9(9, total));
        // Slide 10: Structural Patterns
        list.add(buildSlide10(10, total));
        // Slide 11: Behavioral Patterns
        list.add(buildSlide11(11, total));
        // Slide 12: Dual-Engine Persistence
        list.add(buildSlide12(12, total));
        // Slide 13: Core Application Workflows
        list.add(buildSlide13(13, total));
        // Slide 14: Automated Testing & Invariant Defense
        list.add(buildSlide14(14, total));
        // Slide 15: Conclusion & Future Scope
        list.add(buildSlide15(15, total));
        // Slide 16: Viva Defense Q&A
        list.add(buildSlide16(16, total));

        return list;
    }

    // ==========================================
    // Slide Builders
    // ==========================================

    private static String buildSlide1(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);

        // Academic Top Banner
        addBox(sb, 800000, 700000, 10592000, 600000, "EEF2FF", "C7D2FE", "CSL203 / CSL205 OBJECT ORIENTED PROGRAMMING (JAVA) • ACADEMIC EVALUATION", 1400, "4338CA", true, null);

        // Main Title
        addTextBlock(sb, 800000, 1500000, 10592000, 1300000,
            "HAVEN", 5200, "1E293B", true,
            "Campus Accommodation & Student Housing Platform", 2400, "4338CA", true);

        // Subtext
        addParagraph(sb, 800000, 2800000, 10592000, 600000,
            "A Production-Grade Desktop System implementing 14 Design Patterns, Subtype Cost Polymorphism, Embedded Geospatial Maps, and Offline-Resilient Dual-Engine Persistence.",
            1400, "475569", false);

        // 4 Metadata Cards
        long cardW = 2450000;
        long gap = 260000;
        long startX = 800000;
        long y = 3700000;
        long cardH = 1700000;

        addCard(sb, startX + 0 * (cardW + gap), y, cardW, cardH, "F8FAFC", "CBD5E1", "STUDENT / PRESENTER",
            Arrays.asList("Minod & Project Team", "B.Tech Computer Science", "Semester 3 (S3 OOP Lab)"));

        addCard(sb, startX + 1 * (cardW + gap), y, cardW, cardH, "F8FAFC", "CBD5E1", "INSTITUTION & LAB",
            Arrays.asList("SCT College of Engineering", "Dept. of Computer Science", "Trivandrum, Kerala"));

        addCard(sb, startX + 2 * (cardW + gap), y, cardW, cardH, "F8FAFC", "CBD5E1", "SOFTWARE ARCHITECTURE",
            Arrays.asList("14 GoF Design Patterns", "Dual MongoDB + JSON", "Pure Java Swing Desktop"));

        addCard(sb, startX + 3 * (cardW + gap), y, cardW, cardH, "F8FAFC", "CBD5E1", "VERIFICATION & PROOF",
            Arrays.asList("34/34 Automated Tests", "< 2s Offline Fallback", "100% Invariant Compliant"));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide2(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "EXECUTIVE SUMMARY", "Abstract & Project Overview", "Bridging Kerala engineering campus housing friction with clean OOP architecture");

        // Stat Callouts row
        long statW = 3300000;
        long statGap = 346000;
        long startX = 800000;
        long statY = 1750000;
        long statH = 950000;

        addBox(sb, startX + 0 * (statW + statGap), statY, statW, statH, "EEF2FF", "C7D2FE", "60%+ RELOCATION", 2400, "4338CA", true, "Students relocate yearly seeking housing near SCTCE/CET");
        addBox(sb, startX + 1 * (statW + statGap), statY, statW, statH, "ECFDF5", "A7F3D0", "14 DESIGN PATTERNS", 2400, "059669", true, "Rigorous software engineering design catalog applied");
        addBox(sb, startX + 2 * (statW + statGap), statY, statW, statH, "FFFBEB", "FDE68A", "< 2s OFFLINE FALLBACK", 2400, "D97706", true, "Instant automatic switch from MongoDB to JSON store");

        // Two Main Columns
        long colW = 5150000;
        long colH = 3300000;
        long colY = 2850000;

        addCard(sb, startX, colY, colW, colH, "FEF2F2", "FECACA", "🚨 The Real-World Problem Space",
            Arrays.asList(
                "Information Asymmetry: Hostels operate in fragmented silos with zero centralized verification.",
                "Opaque Fee Structures: Compulsory mess fees and utility bills hidden until after room check-in.",
                "Broker Exploitation: Third-party middlemen demand non-refundable commissions of ₹2,000 - ₹5,000.",
                "Phantom Vacancies: Paper-based rosters cause double-booking disputes and wasted travel hours.",
                "Presentation Fragility: Cloud-reliant web apps freeze during lab exams due to campus firewall rules."
            ));

        addCard(sb, startX + colW + 292000, colY, colW, colH, "F0FDF4", "BBF7D0", "💡 The HAVEN Engineering Solution",
            Arrays.asList(
                "Subtype Cost Polymorphism: Specialized pricing for Hostel (mess + curfew) vs PG (utilities + food).",
                "Embedded Geospatial Canvas: JXMapViewer2 OpenStreetMap tile viewer with color-coded vacancy pins.",
                "Dual-Engine Repository: Seamless automatic fallback to local JSON if MongoDB is unreachable.",
                "Guarded State Machine: 5-stage enquiry lifecycle strictly protecting booking transactions.",
                "Automated Invariant Verification: 34 JUnit-style automated tests proving domain correctness."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide3(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 1: INTRODUCTION", "Background & Problem Statement", "Analyzing the student accommodation crisis around engineering institutions");

        long cardW = 3350000;
        long gap = 270000;
        long startX = 800000;
        long y = 1800000;
        long cardH = 3400000;

        addCard(sb, startX + 0 * (cardW + gap), y, cardW, cardH, "F8FAFC", "E2E8F0", "1. Fragmented Registry",
            Arrays.asList(
                "Local Context:",
                "Around SCTCE Pappanamcode, Kaimanam, and Nemom, dozens of private hostels operate without digital discoverability.",
                "",
                "Consequence:",
                "Incoming freshmen and parents must walk door-to-door in unfamiliar neighborhoods under severe time constraints.",
                "",
                "Outcome: Complete lack of institutional verification or safety checks."
            ));

        addCard(sb, startX + 1 * (cardW + gap), y, cardW, cardH, "F8FAFC", "E2E8F0", "2. Financial Exploitation",
            Arrays.asList(
                "Middlemen Friction:",
                "Informal brokers exploit urgency, demanding up to one month's rent as non-refundable brokerage fees.",
                "",
                "Hidden Surcharges:",
                "Hostel wardens impose compulsory mess charges and electrical surcharges without prior written notice.",
                "",
                "Outcome: Heavy financial strain on out-of-district engineering students."
            ));

        addCard(sb, startX + 2 * (cardW + gap), y, cardW, cardH, "F8FAFC", "E2E8F0", "3. Unreliable Inventory",
            Arrays.asList(
                "Manual Rosters:",
                "Vacancy tracking relies on paper logbooks, whiteboard notices, or word-of-mouth updates.",
                "",
                "Stale Information:",
                "Vacancies reach zero while flyers remain posted on campus noticeboards, leading to overbooking disputes.",
                "",
                "Outcome: Frustrating search loops and zero transparency."
            ));

        addBox(sb, startX, 5350000, 10592000, 800000, "EEF2FF", "C7D2FE",
            "THE CORE CHALLENGE: Build an offline-resilient, type-safe Java desktop platform eliminating middlemen and ensuring 100% data integrity.",
            1350, "312E81", true, null);

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide4(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 1: PROJECT FORMULATION", "Objectives & Scope Boundaries", "Target deliverables and clearly defined engineering scope");

        long colW = 5150000;
        long colH = 4300000;
        long y = 1800000;
        long startX = 800000;

        addCard(sb, startX, y, colW, colH, "F8FAFC", "CBD5E1", "🎯 Primary Project Objectives",
            Arrays.asList(
                "Role-Based Access Control:",
                "Dedicated secure workspaces for Students (browse, filter, book), Wardens (manage rooms, respond), and Admins (moderate, audit).",
                "",
                "Polymorphic Pricing Calculation:",
                "Automated total monthly expenditure computation factoring sharing multipliers, food charges, and curfews.",
                "",
                "Geospatial Landmark Pinpointing:",
                "Interactive offline-ready map navigation centered on SCTCE, CET, and other major colleges.",
                "",
                "Network-Fault Resilience:",
                "Zero data loss or UI freezes through a dual MongoDB and JSON persistence engine.",
                "",
                "Academic Rigor & Invariant Safety:",
                "Guaranteed consistency validated through 34 automated unit test cases."
            ));

        addCard(sb, startX + colW + 292000, y, colW, colH, "F8FAFC", "CBD5E1", "📦 Implementation Scope Demarcation",
            Arrays.asList(
                "✅ Fully Implemented (In-Scope):",
                "• Dynamic multi-criteria filter (rent slider, distance, curfew, rating).",
                "• 5-State Enquiry Machine with terminal state transitions.",
                "• Admin Command Pattern with dual-stack Undo/Redo.",
                "• Booking receipt & applicant roster formatted report exports.",
                "• JXMapViewer2 local tile caching & fallback coordinate plotting.",
                "",
                "⏳ Future Enhancements (Out-of-Scope):",
                "• Real-time payment gateway integration (UPI / NetBanking).",
                "• Mobile companion applications (Flutter for Android/iOS).",
                "• Direct SMS/WhatsApp automated notification webhooks."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide5(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 3: METHODOLOGY", "System Architecture & Layered Design", "Clean 4-tier desktop architecture decoupled by an asynchronous Event Bus");

        // 4 Horizontal Tiers
        long cardW = 2450000;
        long gap = 260000;
        long startX = 800000;
        long y = 1800000;
        long cardH = 1500000;

        addCard(sb, startX + 0 * (cardW + gap), y, cardW, cardH, "EEF2FF", "C7D2FE", "TIER 1: PRESENTATION",
            Arrays.asList("Swing Desktop GUI", "MainFrame & Cards", "ListingCardPanel", "MapPanel (JXMapViewer2)"));

        addCard(sb, startX + 1 * (cardW + gap), y, cardW, cardH, "ECFDF5", "A7F3D0", "TIER 2: FACADE / BUS",
            Arrays.asList("DAO Service Facades", "HostelDAO, UserDAO", "EnquiryDAO", "HavenEventBus (Pub/Sub)"));

        addCard(sb, startX + 2 * (cardW + gap), y, cardW, cardH, "FFFBEB", "FDE68A", "TIER 3: DOMAIN MODELS",
            Arrays.asList("Encapsulated Entities", "Hostel & PG Models", "EnquiryState Machine", "Decorator Wrappers"));

        addCard(sb, startX + 3 * (cardW + gap), y, cardW, cardH, "FDF4FF", "F0ABFC", "TIER 4: REPOSITORIES",
            Arrays.asList("Abstract Repository", "MongoRepositoryFactory", "InMemoryFactory", "Dual Engine Fallback"));

        // Flow box
        long flowY = 3450000;
        long flowH = 2650000;
        addCard(sb, startX, flowY, 10592000, flowH, "F8FAFC", "CBD5E1", "🔄 Unidirectional Data Flow & Event Dispatch Sequence",
            Arrays.asList(
                "1. User Action: Student adjusts rent slider or submits an accommodation enquiry on the UI.",
                "2. Facade Delegation: View invokes type-safe methods on HostelDAO or EnquiryDAO (no direct DB access).",
                "3. Repository Routing: DAO communicates via Repository<T, ID> interface to active repository engine.",
                "4. Fault-Tolerant Persistence: Engine writes atomically to MongoDB; automatically falls back to JSON if socket fails.",
                "5. Asynchronous Event Dispatch: HavenEventBus publishes typed AccommodationEvent or EnquiryEvent.",
                "6. Reactive UI Refresh: Subscribed observer panels (ListingCard, WardenDashboard) refresh instantaneously."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide6(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 3: METHODOLOGY", "Technology Stack & Technical Justification", "Selection criteria ensuring academic rigor, deterministic performance, and portability");

        String[] headers = {"Layer / Concern", "Selected Technology", "Version", "Engineering Rationale & Justification"};
        String[][] rows = {
            {"Core Language", "Java SE (OpenJDK)", "17 / 21 LTS", "Strict static typing, OOP records, strong memory model, cross-platform portability."},
            {"GUI Toolkit", "Java Swing & Java2D", "JDK Standard", "Zero external dependencies, native desktop performance, custom pixel-painted map markers."},
            {"Geospatial Maps", "JXMapViewer2", "2.8", "Embedded OpenStreetMap tile client with local disk caching and offline coordinate plotting."},
            {"Primary Database", "MongoDB Java Driver", "3.12.14", "Document model naturally maps nested accommodation amenities, reviews, and geo coordinates."},
            {"Offline Persistence", "JSON File Engine", "Standard IO", "Embedded JSON store guarantees 100% functionality without internet or MongoDB service."},
            {"Security & Auth", "SHA-256 Hashing", "Java Crypto", "Irreversible salted cryptographic password hashing implemented in SecurityUtils."}
        };

        addTable(sb, 800000, 1800000, 10592000, 4300000, headers, rows, new long[]{1800000, 2400000, 1400000, 4992000});

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide7(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 3: OOP FOUNDATIONS", "The Four Pillars of OOP in HAVEN", "Concrete codebase anchors demonstrating core object-oriented software engineering principles");

        long cardW = 5150000;
        long cardH = 2050000;
        long startX = 800000;
        long col2X = startX + cardW + 292000;
        long row1Y = 1800000;
        long row2Y = 4000000;

        addCard(sb, startX, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "🔒 1. Encapsulation",
            Arrays.asList(
                "• All state variables in Accommodation, User, and Enquiry are strictly private.",
                "• Invariant setters: setBaseRent(rent) enforces rent > 0; setVacancies(v) enforces v >= 0.",
                "• Defensive copies: getAmenities() returns new ArrayList<>(amenities) preventing external mutation."
            ));

        addCard(sb, col2X, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "🧩 2. Abstraction",
            Arrays.asList(
                "• Generic interface Repository<T, ID> defines storage contracts without exposing DB details.",
                "• Abstract base class Accommodation declares abstract double calculateTotalMonthlyCost().",
                "• ReportGenerator defines the template skeleton while abstracting body content formatting."
            ));

        addCard(sb, startX, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "🌳 3. Inheritance",
            Arrays.asList(
                "• Concrete classes Hostel and PG extend the abstract Accommodation superclass.",
                "• Student, Owner, and Admin extend the User base class to reuse authentication identity.",
                "• AccommodationDecorator inherits Accommodation to wrap stays transparently."
            ));

        addCard(sb, col2X, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "⚡ 4. Polymorphism",
            Arrays.asList(
                "• Dynamic method dispatch: Accommodation.calculateTotalMonthlyCost() executed polymorphically.",
                "• Hostel computes base rent + fixed mess fee - sharing discount.",
                "• PG computes base rent + utility charges + optional food plan fees."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide8(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 3: DESIGN PATTERNS", "14 Design Patterns Master Catalog", "Full breakdown of GoF and Enterprise architectural patterns implemented in Haven");

        String[] headers = {"Pattern Name", "Type", "Core Class / Interface", "Architectural Role in Haven"};
        String[][] rows = {
            {"Singleton", "Creational", "MongoDBConnection, FilePersistenceManager", "Guarantees single shared connection pool and persistence manager."},
            {"Factory Method", "Creational", "RepositoryFactory.createRepository()", "Encapsulates repository creation based on active storage engine."},
            {"Abstract Factory", "Creational", "MongoRepositoryFactory, InMemoryFactory", "Creates complete families of DAO repositories interchangeably."},
            {"Builder", "Creational", "Accommodation.Builder", "Constructs complex accommodation objects with 15+ flexible parameters."},
            {"Decorator", "Structural", "AccommodationDecorator, ACRoomDecorator", "Dynamically attaches add-on amenities (AC, WiFi, Mess) with pricing."},
            {"Composite", "Structural", "AmenityComposite, LeafAmenity", "Treats individual and bundled accommodation amenities uniformly."},
            {"Adapter", "Structural", "GoogleMapsAdapter", "Bridges GeoPoint coordinates into browser and desktop navigation URLs."},
            {"Facade", "Structural", "HostelDAO, UserDAO, EnquiryDAO", "Simplifies complex multi-repository operations into high-level APIs."},
            {"Observer / Bus", "Behavioral", "HavenEventBus, AccommodationObserver", "Publishes real-time vacancy and enquiry events across decoupled views."},
            {"State", "Behavioral", "EnquiryState (Pending, Approved, Completed)", "Enforces strictly valid enquiry lifecycle transitions and terminal states."},
            {"Strategy", "Behavioral", "FilterStrategy, PriceFilter, AmenityFilter", "Pluggable algorithms for dynamic multi-criteria listing search."},
            {"Command", "Behavioral", "AdminCommand, DeleteListingCommand", "Encapsulates administrative actions with dual-stack Undo/Redo capability."},
            {"Template Method", "Behavioral", "ReportGenerator, BookingReceiptGenerator", "Defines invariant document skeleton with customizable content formatting."}
        };

        addTable(sb, 800000, 1800000, 10592000, 4300000, headers, rows, new long[]{1800000, 1400000, 3600000, 3792000});

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide9(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 3: DESIGN PATTERNS", "Creational Patterns Deep-Dive", "Encapsulating object creation, lifecycle control, and system configurability");

        long cardW = 5150000;
        long cardH = 2050000;
        long startX = 800000;
        long col2X = startX + cardW + 292000;
        long row1Y = 1800000;
        long row2Y = 4000000;

        addCard(sb, startX, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "1. Singleton Pattern",
            Arrays.asList(
                "Classes: MongoDBConnection, FilePersistenceManager",
                "• Ensures exactly one MongoClient connection pool exists across the application lifecycle.",
                "• Lazy thread-safe initialization with double-checked locking idiom.",
                "• 2-second fail-fast timeout preventing blocking EDT thread."
            ));

        addCard(sb, col2X, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "2. Factory Method Pattern",
            Arrays.asList(
                "Classes: RepositoryFactory",
                "• Defines factory method createAccommodationRepository(StorageMode).",
                "• Shields client classes from knowing whether storage is backed by Mongo or JSON.",
                "• Simplifies switching between testing and production runtime configurations."
            ));

        addCard(sb, startX, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "3. Abstract Factory Pattern",
            Arrays.asList(
                "Classes: RepositoryFactory (interface), MongoRepositoryFactory, InMemoryRepositoryFactory",
                "• Provides factory interfaces to produce complete sets of 7 repositories.",
                "• Guarantees Accommodation, User, and Enquiry repositories always use the same storage engine."
            ));

        addCard(sb, col2X, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "4. Builder Pattern",
            Arrays.asList(
                "Classes: Accommodation.Builder",
                "• Solves the telescoping constructor anti-pattern for 15+ property attributes.",
                "• Fluent method chaining: builder.name(\"Green Haven\").rent(6500).sharing(2).build().",
                "• Validates domain invariants (rent > 0, vacancies >= 0) before instantiating."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide10(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 3: DESIGN PATTERNS", "Structural Patterns Deep-Dive", "Composing clean object hierarchies and bridging external subsystems");

        long cardW = 5150000;
        long cardH = 2050000;
        long startX = 800000;
        long col2X = startX + cardW + 292000;
        long row1Y = 1800000;
        long row2Y = 4000000;

        addCard(sb, startX, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "5. Decorator Pattern",
            Arrays.asList(
                "Classes: AccommodationDecorator, ACRoomDecorator, MessPlanDecorator",
                "• Dynamically adds custom amenities and surcharges without modifying base classes.",
                "• Transparently wraps calculateTotalMonthlyCost() to accumulate surcharge fees.",
                "• Follows Open/Closed Principle: add new amenities without editing existing code."
            ));

        addCard(sb, col2X, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "6. Composite Pattern",
            Arrays.asList(
                "Classes: AmenityComponent, AmenityComposite, LeafAmenity",
                "• Allows grouping amenities into hierarchical bundles (e.g., Premium Comfort Pack).",
                "• Client views render individual and grouped amenities with a uniform interface.",
                "• Simplifies amenity validation and monthly fee computation."
            ));

        addCard(sb, startX, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "7. Adapter Pattern",
            Arrays.asList(
                "Classes: GoogleMapsAdapter, MapNavigationService",
                "• Converts internal GeoPoint(latitude, longitude) into external web navigation targets.",
                "• Automatically launches native system browser with exact direction queries.",
                "• Gracefully copies formatted coordinates to clipboard if browser launch is restricted."
            ));

        addCard(sb, col2X, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "8. Facade Pattern",
            Arrays.asList(
                "Classes: HostelDAO, UserDAO, EnquiryDAO",
                "• Unifies repository queries, invariant checks, and event dispatches behind single methods.",
                "• UI panels call hostelDAO.bookRoom(...) without dealing with repositories or events.",
                "• Dramatically reduces coupling between Swing presentation and storage tiers."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide11(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 3: DESIGN PATTERNS", "Behavioral Patterns Deep-Dive", "Orchestrating state transitions, event publication, and reversible user actions");

        long cardW = 3350000;
        long gap = 270000;
        long startX = 800000;
        long y = 1800000;
        long cardH = 4300000;

        addCard(sb, startX + 0 * (cardW + gap), y, cardW, cardH, "F8FAFC", "CBD5E1", "9 & 10. Observer & State",
            Arrays.asList(
                "Observer / HavenEventBus:",
                "• Decoupled Pub/Sub event bus.",
                "• Dispatches AccommodationEvent and EnquiryEvent asynchronously.",
                "• ListingCardPanel and WardenDashboard auto-refresh on new bookings.",
                "",
                "State Pattern (EnquiryState):",
                "• States: Pending -> Under Review -> Approved -> Completed.",
                "• Terminal states: Rejected and Cancelled.",
                "• Disallows invalid state transitions (cannot approve a cancelled enquiry)."
            ));

        addCard(sb, startX + 1 * (cardW + gap), y, cardW, cardH, "F8FAFC", "CBD5E1", "11 & 12. Strategy & Command",
            Arrays.asList(
                "Strategy (FilterStrategy):",
                "• Dynamic filtering algorithms applied interchangeably.",
                "• PriceRangeStrategy, CurfewStrategy, RatingStrategy, AmenityStrategy.",
                "• Composed dynamically as students adjust filter controls.",
                "",
                "Command (Admin Undo/Redo):",
                "• Admin actions encapsulated as Command objects (DeleteListingCommand).",
                "• Dual-stack history (Undo stack & Redo stack).",
                "• Reversible admin moderation protecting accidental deletions."
            ));

        addCard(sb, startX + 2 * (cardW + gap), y, cardW, cardH, "F8FAFC", "CBD5E1", "13. Template Method",
            Arrays.asList(
                "ReportGenerator Skeleton:",
                "• Base class defines invariant 5-step report document generation algorithm:",
                "  1. writeHeader()",
                "  2. writeMetadata()",
                "  3. writeBody() [Abstract]",
                "  4. writeFooter()",
                "  5. exportDocument()",
                "",
                "Subclasses:",
                "• BookingReceiptGenerator: Formats financial breakdown & mess dues.",
                "• RosterReportGenerator: Formats registered warden occupant lists."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide12(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 4: IMPLEMENTATION", "Dual-Engine Persistence & Offline Resilience", "Engineering high availability for viva defense and restricted campus Wi-Fi environments");

        long colW = 5150000;
        long colH = 4300000;
        long y = 1800000;
        long startX = 800000;

        addCard(sb, startX, y, colW, colH, "F8FAFC", "CBD5E1", "🔌 The Lab Network Risk & Mitigation",
            Arrays.asList(
                "Critical Blocker Identified:",
                "Standard MongoDB drivers block the Java Swing Event Dispatch Thread (EDT) for 30 seconds if MongoDB is offline or Wi-Fi is blocked.",
                "",
                "Engineering Fix Applied:",
                "1. Socket Timeouts: Configured serverSelectionTimeout(2000) and connectTimeout(2000).",
                "2. Automatic Fallback: Catches MongoTimeoutException and immediately activates FilePersistenceManager.",
                "3. Safe Type Casting: Added getDoubleSafe() to eliminate ClassCastException between BSON Integer and Double formats.",
                "4. Image Caching: PropertyImageStorage utilizes ConcurrentHashMap icon caching to eliminate EDT scroll stutter."
            ));

        addCard(sb, startX + colW + 292000, y, colW, colH, "F8FAFC", "CBD5E1", "🗺️ Geospatial Map Offline Architecture",
            Arrays.asList(
                "JXMapViewer2 Resilience Strategy:",
                "OpenStreetMap tile loading hangs indefinitely under college firewalls blocking generic Java user agents.",
                "",
                "Implementation Details:",
                "1. User-Agent Header: Set to \"Haven-Campus-Housing/1.0\" to prevent OSM server drop.",
                "2. Local Disk Cache: Uses FileBasedLocalCache in data/cache/tiles for pre-cached map viewing.",
                "3. Offline Grid Fallback: If network is unreachable, switches to EmptyTileFactory with a custom Java2D grid.",
                "4. Landmark Pins: Paints color-coded pins (Green = vacancies > 0, Red = full) with college reference markers."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide13(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 4: IMPLEMENTATION", "Core Application Modules & Workflows", "End-to-end user journeys supported across Student, Warden, and Administrator roles");

        long cardW = 5150000;
        long cardH = 2050000;
        long startX = 800000;
        long col2X = startX + cardW + 292000;
        long row1Y = 1800000;
        long row2Y = 4000000;

        addCard(sb, startX, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "1. Student Discovery & Filtering",
            Arrays.asList(
                "• Dynamic 3-tier filter bar: Rent slider, radius (km), curfew time, and minimum rating.",
                "• Dual-view toggle: Switch between rich card grid and interactive JXMapViewer2 map pins.",
                "• One-click enquiry modal with custom notes, sharing choices, and add-on selections."
            ));

        addCard(sb, col2X, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "2. Warden Listing & Vacancy Control",
            Arrays.asList(
                "• Complete listing lifecycle: Create, update room counts, and upload interior/exterior photos.",
                "• Real-time vacancy decrement upon confirmed booking (preventing double-booking).",
                "• Dedicated enquiries table: Review student requests, accept, or decline with feedback."
            ));

        addCard(sb, startX, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "3. Enquiry Negotiation & State Machine",
            Arrays.asList(
                "• State guards enforce terminal states (cannot accept already cancelled enquiry).",
                "• Automatic audit trail timestamping state transitions from PENDING to COMPLETED.",
                "• Instant notification dispatches to warden and student via HavenEventBus."
            ));

        addCard(sb, col2X, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "4. Admin Governance & Undo/Redo",
            Arrays.asList(
                "• System-wide listing moderation: Suspend spam hostels or modify fraudulent records.",
                "• Command Pattern Undo/Redo: Revert accidental deletions with one click.",
                "• Export module: Generates formatted booking receipts and warden applicant rosters."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide14(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 4: TESTING & RESULTS", "Automated Testing & Invariant Defense", "Comprehensive test suite proving behavioral correctness and edge-case resilience");

        String[] headers = {"Test Suite Category", "Test Count", "Key Invariants Validated", "Status"};
        String[][] rows = {
            {"Domain Invariants", "8 Tests", "Rent > 0, vacancies >= 0, defensive amenity copying, subtype calculation.", "PASSED (100%)"},
            {"Design Patterns", "6 Tests", "Singleton uniqueness, State transition guards, Decorator price math.", "PASSED (100%)"},
            {"Persistence & Fallback", "10 Tests", "Dual engine failover, MongoDB to JSON fallback, BSON safe casting.", "PASSED (100%)"},
            {"Security & Edge Cases", "10 Tests", "SHA-256 salt verification, vacancy exhaustion exception, null safety.", "PASSED (100%)"},
            {"TOTAL VERIFICATION", "34 Tests", "Zero failures, zero exceptions, deterministic suite execution.", "ALL PASSING"}
        };

        addTable(sb, 800000, 1800000, 10592000, 2400000, headers, rows, new long[]{2400000, 1400000, 5392000, 1400000});

        addCard(sb, 800000, 4400000, 10592000, 1700000, "F0FDF4", "BBF7D0", "🧪 1-Click Live Viva Demo: Vacancy Exhaustion Defense",
            Arrays.asList(
                "Demonstration Scenario: Hostel BH04 configured with exactly 1 vacancy.",
                "1. Student A books the final room -> Vacancy drops to 0; status automatically updates to FULL.",
                "2. Student B attempts concurrent booking -> System intercepts and throws VacancyExhaustedException.",
                "3. Result: UI presents friendly alert; paper-register double-booking problem definitively solved."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide15(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "CHAPTER 5: CONCLUSION", "Project Summary & Future Roadmap", "Summary of deliverables, academic learnings, and planned production enhancements");

        long colW = 5150000;
        long colH = 4300000;
        long y = 1800000;
        long startX = 800000;

        addCard(sb, startX, y, colW, colH, "F8FAFC", "CBD5E1", "🏆 Academic & Practical Achievements",
            Arrays.asList(
                "Key Project Takeaways:",
                "• Clean Object-Oriented Architecture: Applied all 4 core OOP pillars and 14 design patterns in a unified codebase.",
                "• Middleman Elimination: Directly connects students with verified wardens, saving ₹2,000 - ₹5,000 per admission.",
                "• Zero Demo Failure Risk: Dual-engine persistence and offline tile caching eliminate dependence on college Wi-Fi.",
                "• Strict Production Quality: 34 automated unit test cases ensuring 100% invariant compliance and zero regressions.",
                "• Real-World Impact: Ready for deployment across SCTCE Pappanamcode and neighboring engineering colleges."
            ));

        addCard(sb, startX + colW + 292000, y, colW, colH, "F8FAFC", "CBD5E1", "🚀 Future Enhancement Roadmap",
            Arrays.asList(
                "Planned Architectural Enhancements:",
                "• Mobile Companion App: Cross-platform Flutter client for Android and iOS devices.",
                "• Automated Payment Gateway: Direct UPI (Google Pay, PhonePe) and Razorpay integration with automated receipts.",
                "• Push Notifications: Direct WhatsApp and SMS transactional messaging via Twilio API.",
                "• Direct Review Moderation: Sentiment analysis to detect fraudulent or retaliatory reviews.",
                "• Multi-Campus Multi-Tenancy: Expanding schema to support CET, GEC Barton Hill, and Model Engineering College."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    private static String buildSlide16(int num, int total) {
        StringBuilder sb = new StringBuilder();
        startSlide(sb);
        addHeader(sb, "VIVA VOCE PREPARATION", "Viva Defense & Examiner Q&A Guide", "Anticipated technical questions and authoritative engineering responses");

        long cardW = 5150000;
        long cardH = 2050000;
        long startX = 800000;
        long col2X = startX + cardW + 292000;
        long row1Y = 1800000;
        long row2Y = 4000000;

        addCard(sb, startX, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "Q1: Why Java Swing over JavaFX / Web?",
            Arrays.asList(
                "Answer: Java Swing is built directly into the standard JDK without requiring external runtime modules.",
                "It enables pixel-perfect custom Java2D rendering for geospatial map overlays and launches instantly with zero local server overhead."
            ));

        addCard(sb, col2X, row1Y, cardW, cardH, "F8FAFC", "CBD5E1", "Q2: Difference between State & Strategy patterns?",
            Arrays.asList(
                "Answer: Strategy (FilterStrategy) defines interchangeable algorithms chosen by the client.",
                "State (EnquiryState) encapsulates internal transitions where the object automatically alters its behavior based on its current lifecycle phase."
            ));

        addCard(sb, startX, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "Q3: How do you handle NoSQL / SQL injection?",
            Arrays.asList(
                "Answer: We use strongly-typed BSON query filters (Filters.eq(), Filters.and()) and parameterized inputs.",
                "Input sanitization in SecurityUtils guarantees user strings are treated purely as literal data values."
            ));

        addCard(sb, col2X, row2Y, cardW, cardH, "F8FAFC", "CBD5E1", "Q4: What if MongoDB goes down during viva?",
            Arrays.asList(
                "Answer: Our Dual-Engine Repository catches MongoTimeoutException within 2 seconds and automatically activates FilePersistenceManager.",
                "The entire application continues reading and writing to haven_store.json with zero downtime or data loss."
            ));

        addFooter(sb, num, total);
        endSlide(sb);
        return sb.toString();
    }

    // ==========================================
    // OpenXML Helper Methods
    // ==========================================

    private static void startSlide(StringBuilder sb) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<p:sld xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">\n");
        sb.append("  <p:cSld>\n");
        sb.append("    <p:spTree>\n");
        sb.append("      <p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>\n");
        sb.append("      <p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/><a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr>\n");
    }

    private static void endSlide(StringBuilder sb) {
        sb.append("    </p:spTree>\n");
        sb.append("  </p:cSld>\n");
        sb.append("  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>\n");
        sb.append("</p:sld>");
    }

    private static void addHeader(StringBuilder sb, String category, String title, String subtitle) {
        int id = ++shapeIdCounter;
        sb.append("      <p:sp>\n");
        sb.append("        <p:nvSpPr><p:cNvPr id=\"").append(id).append("\" name=\"Header ").append(id).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>\n");
        sb.append("        <p:spPr><a:xfrm><a:off x=\"800000\" y=\"500000\"/><a:ext cx=\"10592000\" cy=\"1150000\"/></a:xfrm></p:spPr>\n");
        sb.append("        <p:txBody>\n");
        sb.append("          <a:bodyPr tIns=\"0\" bIns=\"0\" lIns=\"0\" rIns=\"0\"/>\n");
        sb.append("          <a:lstStyle/>\n");
        sb.append("          <a:p><a:r><a:rPr sz=\"1100\" b=\"1\"><a:solidFill><a:srgbClr val=\"4338CA\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>").append(escapeXml(category.toUpperCase())).append("</a:t></a:r></a:p>\n");
        sb.append("          <a:p><a:r><a:rPr sz=\"2600\" b=\"1\"><a:solidFill><a:srgbClr val=\"1E293B\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>").append(escapeXml(title)).append("</a:t></a:r></a:p>\n");
        sb.append("          <a:p><a:r><a:rPr sz=\"1350\"><a:solidFill><a:srgbClr val=\"64748B\"/></a:solidFill><a:latin typeface=\"Segoe UI\"/></a:rPr><a:t>").append(escapeXml(subtitle)).append("</a:t></a:r></a:p>\n");
        sb.append("        </p:txBody>\n");
        sb.append("      </p:sp>\n");
    }

    private static void addFooter(StringBuilder sb, int num, int total) {
        int id = ++shapeIdCounter;
        sb.append("      <p:sp>\n");
        sb.append("        <p:nvSpPr><p:cNvPr id=\"").append(id).append("\" name=\"Footer ").append(id).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>\n");
        sb.append("        <p:spPr><a:xfrm><a:off x=\"800000\" y=\"6300000\"/><a:ext cx=\"10592000\" cy=\"350000\"/></a:xfrm></p:spPr>\n");
        sb.append("        <p:txBody>\n");
        sb.append("          <a:bodyPr tIns=\"0\" bIns=\"0\" lIns=\"0\" rIns=\"0\"/>\n");
        sb.append("          <a:lstStyle/>\n");
        sb.append("          <a:p>\n");
        sb.append("            <a:pPr algn=\"l\"/>\n");
        sb.append("            <a:r><a:rPr sz=\"1050\"><a:solidFill><a:srgbClr val=\"94A3B8\"/></a:solidFill><a:latin typeface=\"Segoe UI\"/></a:rPr><a:t>HAVEN • B.Tech Computer Science (S3 OOP Project Presentation)  |  SCTCE</a:t></a:r>\n");
        sb.append("          </a:p>\n");
        sb.append("        </p:txBody>\n");
        sb.append("      </p:sp>\n");

        int id2 = ++shapeIdCounter;
        sb.append("      <p:sp>\n");
        sb.append("        <p:nvSpPr><p:cNvPr id=\"").append(id2).append("\" name=\"SlideNum ").append(id2).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>\n");
        sb.append("        <p:spPr><a:xfrm><a:off x=\"10000000\" y=\"6300000\"/><a:ext cx=\"1392000\" cy=\"350000\"/></a:xfrm></p:spPr>\n");
        sb.append("        <p:txBody>\n");
        sb.append("          <a:bodyPr tIns=\"0\" bIns=\"0\" lIns=\"0\" rIns=\"0\"/>\n");
        sb.append("          <a:lstStyle/>\n");
        sb.append("          <a:p>\n");
        sb.append("            <a:pPr algn=\"r\"/>\n");
        sb.append("            <a:r><a:rPr sz=\"1100\" b=\"1\"><a:solidFill><a:srgbClr val=\"4338CA\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>Slide ").append(num).append(" / ").append(total).append("</a:t></a:r>\n");
        sb.append("          </a:p>\n");
        sb.append("        </p:txBody>\n");
        sb.append("      </p:sp>\n");
    }

    private static void addCard(StringBuilder sb, long x, long y, long cx, long cy, String bgHex, String borderHex, String title, List<String> lines) {
        int id = ++shapeIdCounter;
        sb.append("      <p:sp>\n");
        sb.append("        <p:nvSpPr><p:cNvPr id=\"").append(id).append("\" name=\"Card ").append(id).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>\n");
        sb.append("        <p:spPr>\n");
        sb.append("          <a:xfrm><a:off x=\"").append(x).append("\" y=\"").append(y).append("\"/><a:ext cx=\"").append(cx).append("\" cy=\"").append(cy).append("\"/></a:xfrm>\n");
        sb.append("          <a:prstGeom prst=\"roundRect\"><a:avLst><a:gd name=\"adj\" fmla=\"val 2500\"/></a:avLst></a:prstGeom>\n");
        sb.append("          <a:solidFill><a:srgbClr val=\"").append(bgHex).append("\"/></a:solidFill>\n");
        sb.append("          <a:ln w=\"12700\"><a:solidFill><a:srgbClr val=\"").append(borderHex).append("\"/></a:solidFill></a:ln>\n");
        sb.append("        </p:spPr>\n");
        sb.append("        <p:txBody>\n");
        sb.append("          <a:bodyPr tIns=\"140000\" bIns=\"140000\" lIns=\"180000\" rIns=\"180000\"/>\n");
        sb.append("          <a:lstStyle/>\n");
        if (title != null && !title.isEmpty()) {
            sb.append("          <a:p><a:r><a:rPr sz=\"1400\" b=\"1\"><a:solidFill><a:srgbClr val=\"1E293B\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>").append(escapeXml(title)).append("</a:t></a:r></a:p>\n");
        }
        if (lines != null) {
            for (String line : lines) {
                if (line.isEmpty()) {
                    sb.append("          <a:p><a:pPr sz=\"600\"/></a:p>\n");
                } else if (line.startsWith("•") || line.startsWith("-") || line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") || line.startsWith("4.") || line.startsWith("5.") || line.startsWith("6.")) {
                    sb.append("          <a:p><a:r><a:rPr sz=\"1200\"><a:solidFill><a:srgbClr val=\"334155\"/></a:solidFill><a:latin typeface=\"Segoe UI\"/></a:rPr><a:t>").append(escapeXml(line)).append("</a:t></a:r></a:p>\n");
                } else {
                    sb.append("          <a:p><a:r><a:rPr sz=\"1200\" b=\"1\"><a:solidFill><a:srgbClr val=\"0F172A\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>").append(escapeXml(line)).append("</a:t></a:r></a:p>\n");
                }
            }
        }
        sb.append("        </p:txBody>\n");
        sb.append("      </p:sp>\n");
    }

    private static void addBox(StringBuilder sb, long x, long y, long cx, long cy, String bgHex, String borderHex, String text, int sz, String textHex, boolean bold, String subtext) {
        int id = ++shapeIdCounter;
        sb.append("      <p:sp>\n");
        sb.append("        <p:nvSpPr><p:cNvPr id=\"").append(id).append("\" name=\"Box ").append(id).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>\n");
        sb.append("        <p:spPr>\n");
        sb.append("          <a:xfrm><a:off x=\"").append(x).append("\" y=\"").append(y).append("\"/><a:ext cx=\"").append(cx).append("\" cy=\"").append(cy).append("\"/></a:xfrm>\n");
        sb.append("          <a:prstGeom prst=\"roundRect\"><a:avLst><a:gd name=\"adj\" fmla=\"val 2500\"/></a:avLst></a:prstGeom>\n");
        sb.append("          <a:solidFill><a:srgbClr val=\"").append(bgHex).append("\"/></a:solidFill>\n");
        sb.append("          <a:ln w=\"12700\"><a:solidFill><a:srgbClr val=\"").append(borderHex).append("\"/></a:solidFill></a:ln>\n");
        sb.append("        </p:spPr>\n");
        sb.append("        <p:txBody>\n");
        sb.append("          <a:bodyPr tIns=\"120000\" bIns=\"120000\" lIns=\"160000\" rIns=\"160000\"/>\n");
        sb.append("          <a:lstStyle/>\n");
        sb.append("          <a:p><a:r><a:rPr sz=\"").append(sz).append("\"").append(bold ? " b=\"1\"" : "").append("><a:solidFill><a:srgbClr val=\"").append(textHex).append("\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>").append(escapeXml(text)).append("</a:t></a:r></a:p>\n");
        if (subtext != null && !subtext.isEmpty()) {
            sb.append("          <a:p><a:r><a:rPr sz=\"1150\"><a:solidFill><a:srgbClr val=\"475569\"/></a:solidFill><a:latin typeface=\"Segoe UI\"/></a:rPr><a:t>").append(escapeXml(subtext)).append("</a:t></a:r></a:p>\n");
        }
        sb.append("        </p:txBody>\n");
        sb.append("      </p:sp>\n");
    }

    private static void addTextBlock(StringBuilder sb, long x, long y, long cx, long cy, String line1, int sz1, String hex1, boolean bold1, String line2, int sz2, String hex2, boolean bold2) {
        int id = ++shapeIdCounter;
        sb.append("      <p:sp>\n");
        sb.append("        <p:nvSpPr><p:cNvPr id=\"").append(id).append("\" name=\"Text ").append(id).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>\n");
        sb.append("        <p:spPr><a:xfrm><a:off x=\"").append(x).append("\" y=\"").append(y).append("\"/><a:ext cx=\"").append(cx).append("\" cy=\"").append(cy).append("\"/></a:xfrm></p:spPr>\n");
        sb.append("        <p:txBody>\n");
        sb.append("          <a:bodyPr tIns=\"0\" bIns=\"0\" lIns=\"0\" rIns=\"0\"/>\n");
        sb.append("          <a:lstStyle/>\n");
        sb.append("          <a:p><a:r><a:rPr sz=\"").append(sz1).append("\"").append(bold1 ? " b=\"1\"" : "").append("><a:solidFill><a:srgbClr val=\"").append(hex1).append("\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>").append(escapeXml(line1)).append("</a:t></a:r></a:p>\n");
        sb.append("          <a:p><a:r><a:rPr sz=\"").append(sz2).append("\"").append(bold2 ? " b=\"1\"" : "").append("><a:solidFill><a:srgbClr val=\"").append(hex2).append("\"/></a:solidFill><a:latin typeface=\"Segoe UI\"/></a:rPr><a:t>").append(escapeXml(line2)).append("</a:t></a:r></a:p>\n");
        sb.append("        </p:txBody>\n");
        sb.append("      </p:sp>\n");
    }

    private static void addParagraph(StringBuilder sb, long x, long y, long cx, long cy, String text, int sz, String hex, boolean bold) {
        int id = ++shapeIdCounter;
        sb.append("      <p:sp>\n");
        sb.append("        <p:nvSpPr><p:cNvPr id=\"").append(id).append("\" name=\"Para ").append(id).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>\n");
        sb.append("        <p:spPr><a:xfrm><a:off x=\"").append(x).append("\" y=\"").append(y).append("\"/><a:ext cx=\"").append(cx).append("\" cy=\"").append(cy).append("\"/></a:xfrm></p:spPr>\n");
        sb.append("        <p:txBody>\n");
        sb.append("          <a:bodyPr tIns=\"0\" bIns=\"0\" lIns=\"0\" rIns=\"0\"/>\n");
        sb.append("          <a:lstStyle/>\n");
        sb.append("          <a:p><a:r><a:rPr sz=\"").append(sz).append("\"").append(bold ? " b=\"1\"" : "").append("><a:solidFill><a:srgbClr val=\"").append(hex).append("\"/></a:solidFill><a:latin typeface=\"Segoe UI\"/></a:rPr><a:t>").append(escapeXml(text)).append("</a:t></a:r></a:p>\n");
        sb.append("        </p:txBody>\n");
        sb.append("      </p:sp>\n");
    }

    private static void addTable(StringBuilder sb, long x, long y, long cx, long cy, String[] headers, String[][] rows, long[] colWidths) {
        int id = ++shapeIdCounter;
        sb.append("      <p:graphicFrame>\n");
        sb.append("        <p:nvGraphicFramePr><p:cNvPr id=\"").append(id).append("\" name=\"Table ").append(id).append("\"/><p:cNvGraphicFramePr><a:graphicFrameLocks noGrp=\"1\"/></p:cNvGraphicFramePr><p:nvPr/></p:nvGraphicFramePr>\n");
        sb.append("        <p:xfrm><a:off x=\"").append(x).append("\" y=\"").append(y).append("\"/><a:ext cx=\"").append(cx).append("\" cy=\"").append(cy).append("\"/></p:xfrm>\n");
        sb.append("        <a:graphic>\n");
        sb.append("          <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/table\">\n");
        sb.append("            <a:tbl>\n");
        sb.append("              <a:tblPr/>\n");
        sb.append("              <a:tblGrid>\n");
        for (long w : colWidths) {
            sb.append("                <a:gridCol w=\"").append(w).append("\"/>\n");
        }
        sb.append("              </a:tblGrid>\n");

        // Header Row
        sb.append("              <a:tr h=\"380000\">\n");
        for (String h : headers) {
            sb.append("                <a:tc>\n");
            sb.append("                  <a:txBody>\n");
            sb.append("                    <a:bodyPr tIns=\"70000\" bIns=\"70000\" lIns=\"100000\" rIns=\"100000\"/>\n");
            sb.append("                    <a:lstStyle/>\n");
            sb.append("                    <a:p><a:r><a:rPr sz=\"1200\" b=\"1\"><a:solidFill><a:srgbClr val=\"FFFFFF\"/></a:solidFill><a:latin typeface=\"Segoe UI Semibold\"/></a:rPr><a:t>").append(escapeXml(h)).append("</a:t></a:r></a:p>\n");
            sb.append("                  </a:txBody>\n");
            sb.append("                  <a:tcPr><a:solidFill><a:srgbClr val=\"1E293B\"/></a:solidFill></a:tcPr>\n");
            sb.append("                </a:tc>\n");
        }
        sb.append("              </a:tr>\n");

        // Data Rows
        boolean alternate = false;
        for (String[] row : rows) {
            sb.append("              <a:tr h=\"300000\">\n");
            String bg = alternate ? "F8FAFC" : "FFFFFF";
            for (String cell : row) {
                sb.append("                <a:tc>\n");
                sb.append("                  <a:txBody>\n");
                sb.append("                    <a:bodyPr tIns=\"60000\" bIns=\"60000\" lIns=\"100000\" rIns=\"100000\"/>\n");
                sb.append("                    <a:lstStyle/>\n");
                sb.append("                    <a:p><a:r><a:rPr sz=\"1100\"><a:solidFill><a:srgbClr val=\"334155\"/></a:solidFill><a:latin typeface=\"Segoe UI\"/></a:rPr><a:t>").append(escapeXml(cell)).append("</a:t></a:r></a:p>\n");
                sb.append("                  </a:txBody>\n");
                sb.append("                  <a:tcPr><a:solidFill><a:srgbClr val=\"").append(bg).append("\"/></a:solidFill></a:tcPr>\n");
                sb.append("                </a:tc>\n");
            }
            sb.append("              </a:tr>\n");
            alternate = !alternate;
        }

        sb.append("            </a:tbl>\n");
        sb.append("          </a:graphicData>\n");
        sb.append("        </a:graphic>\n");
        sb.append("      </p:graphicFrame>\n");
    }

    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    // ==========================================
    // Package & Boilerplate XMLs
    // ==========================================

    private static String buildContentTypesXml(int slideCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n");
        sb.append("  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n");
        sb.append("  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n");
        sb.append("  <Override PartName=\"/ppt/presentation.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml\"/>\n");
        sb.append("  <Override PartName=\"/ppt/slideMasters/slideMaster1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml\"/>\n");
        sb.append("  <Override PartName=\"/ppt/slideLayouts/slideLayout1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml\"/>\n");
        sb.append("  <Override PartName=\"/ppt/theme/theme1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.theme+xml\"/>\n");
        for (int i = 1; i <= slideCount; i++) {
            sb.append("  <Override PartName=\"/ppt/slides/slide").append(i).append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>\n");
        }
        sb.append("</Types>");
        return sb.toString();
    }

    private static String buildRootRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
            "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"ppt/presentation.xml\"/>\n" +
            "</Relationships>";
    }

    private static String buildPresentationRelsXml(int slideCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n");
        sb.append("  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster\" Target=\"slideMasters/slideMaster1.xml\"/>\n");
        sb.append("  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme\" Target=\"theme/theme1.xml\"/>\n");
        for (int i = 1; i <= slideCount; i++) {
            sb.append("  <Relationship Id=\"rId").append(i + 2).append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide").append(i).append(".xml\"/>\n");
        }
        sb.append("</Relationships>");
        return sb.toString();
    }

    private static String buildPresentationXml(int slideCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<p:presentation xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">\n");
        sb.append("  <p:sldMasterIdLst>\n");
        sb.append("    <p:sldMasterId id=\"2147483648\" r:id=\"rId1\"/>\n");
        sb.append("  </p:sldMasterIdLst>\n");
        sb.append("  <p:sldIdLst>\n");
        for (int i = 1; i <= slideCount; i++) {
            sb.append("    <p:sldId id=\"").append(255 + i).append("\" r:id=\"rId").append(i + 2).append("\"/>\n");
        }
        sb.append("  </p:sldIdLst>\n");
        sb.append("  <p:sldSz cx=\"12192000\" cy=\"6858000\" type=\"screen16x9\"/>\n");
        sb.append("  <p:notesSz cx=\"6858000\" cy=\"9144000\"/>\n");
        sb.append("</p:presentation>");
        return sb.toString();
    }

    private static String buildSlideMasterRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
            "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout\" Target=\"../slideLayouts/slideLayout1.xml\"/>\n" +
            "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme\" Target=\"../theme/theme1.xml\"/>\n" +
            "</Relationships>";
    }

    private static String buildSlideMasterXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<p:sldMaster xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">\n" +
            "  <p:cSld>\n" +
            "    <p:spTree>\n" +
            "      <p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>\n" +
            "      <p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/><a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr>\n" +
            "    </p:spTree>\n" +
            "  </p:cSld>\n" +
            "  <p:clrMap bg1=\"lt1\" tx1=\"dk1\" bg2=\"lt2\" tx2=\"dk2\" accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" accent4=\"accent4\" accent5=\"accent5\" accent6=\"accent6\" hlink=\"hlink\" folHlink=\"folHlink\"/>\n" +
            "  <p:sldLayoutIdLst>\n" +
            "    <p:sldLayoutId id=\"2147483649\" r:id=\"rId1\"/>\n" +
            "  </p:sldLayoutIdLst>\n" +
            "</p:sldMaster>";
    }

    private static String buildSlideLayout1RelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
            "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster\" Target=\"../slideMasters/slideMaster1.xml\"/>\n" +
            "</Relationships>";
    }

    private static String buildSlideLayout1Xml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<p:sldLayout xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" type=\"blank\">\n" +
            "  <p:cSld name=\"Blank\">\n" +
            "    <p:spTree>\n" +
            "      <p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>\n" +
            "      <p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/><a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr>\n" +
            "    </p:spTree>\n" +
            "  </p:cSld>\n" +
            "  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>\n" +
            "</p:sldLayout>";
    }

    private static String buildThemeXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"Office Theme\">\n" +
            "  <a:themeElements>\n" +
            "    <a:clrScheme name=\"Office\">\n" +
            "      <a:dk1><a:srgbClr val=\"1E293B\"/></a:dk1>\n" +
            "      <a:lt1><a:srgbClr val=\"FFFFFF\"/></a:lt1>\n" +
            "      <a:dk2><a:srgbClr val=\"334155\"/></a:dk2>\n" +
            "      <a:lt2><a:srgbClr val=\"F8FAFC\"/></a:lt2>\n" +
            "      <a:accent1><a:srgbClr val=\"4338CA\"/></a:accent1>\n" +
            "      <a:accent2><a:srgbClr val=\"0284C7\"/></a:accent2>\n" +
            "      <a:accent3><a:srgbClr val=\"059669\"/></a:accent3>\n" +
            "      <a:accent4><a:srgbClr val=\"D97706\"/></a:accent4>\n" +
            "      <a:accent5><a:srgbClr val=\"E11D48\"/></a:accent5>\n" +
            "      <a:accent6><a:srgbClr val=\"7C3AED\"/></a:accent6>\n" +
            "      <a:hlink><a:srgbClr val=\"2563EB\"/></a:hlink>\n" +
            "      <a:folHlink><a:srgbClr val=\"9333EA\"/></a:folHlink>\n" +
            "    </a:clrScheme>\n" +
            "    <a:fontScheme name=\"Office\">\n" +
            "      <a:majorFont><a:latin typeface=\"Segoe UI Semibold\"/></a:majorFont>\n" +
            "      <a:minorFont><a:latin typeface=\"Segoe UI\"/></a:minorFont>\n" +
            "    </a:fontScheme>\n" +
            "    <a:fmtScheme name=\"Office\">\n" +
            "      <a:fillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:fillStyleLst>\n" +
            "      <a:lnStyleLst><a:ln w=\"9525\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln><a:ln w=\"25400\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln><a:ln w=\"38100\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln></a:lnStyleLst>\n" +
            "      <a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>\n" +
            "      <a:bgFillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:bgFillStyleLst>\n" +
            "    </a:fmtScheme>\n" +
            "  </a:themeElements>\n" +
            "</a:theme>";
    }

    private static String buildSlideRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
            "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout\" Target=\"../slideLayouts/slideLayout1.xml\"/>\n" +
            "</Relationships>";
    }
}
