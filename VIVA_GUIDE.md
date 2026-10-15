# HAVEN - OOP Project Viva Defense & Oral Examination Guide

> **Prepared for Semester 3 Object-Oriented Programming (Java) Laboratory Project Evaluation**  
> *Comprehensive Q&A and defense guide covering OOP pillars, all 14 GoF/Enterprise Design Patterns, and live demonstration steps.*

---

## Top 20 Expected Viva Questions & Model Answers

### 1. "Can you demonstrate how the 4 pillars of OOP are implemented in HAVEN?"
* **Encapsulation**:
  - In [`Accommodation.java`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/Accommodation.java) and [`User.java`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/User.java), all fields (`baseRent`, `vacancies`, `password`, `amenities`, `imagePaths`) are `private`.
  - Setters enforce domain invariants (`baseRent > 0`, `vacancies >= 0`), and collections return defensive copies (`new ArrayList<>(amenities)`).
  - Passwords are encrypted using SHA-256 in [`SecurityUtils.java`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/SecurityUtils.java).
* **Abstraction**:
  - Generic base interface [`Repository<T, ID>`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/repository/Repository.java), abstract base classes [`Accommodation`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/Accommodation.java), [`User`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/User.java), and [`ReportGenerator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/report/ReportGenerator.java), and behavioral interfaces [`EnquiryState`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/state/EnquiryState.java), [`AdminCommand`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/AdminCommand.java), and [`NavigationService`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/NavigationService.java).
* **Inheritance**:
  - `Accommodation` is extended by [`Hostel`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/Hostel.java), [`PG`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/PG.java), and [`AccommodationDecorator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/decorator/AccommodationDecorator.java) $\rightarrow$ [`FeaturedAccommodationDecorator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/decorator/FeaturedAccommodationDecorator.java).
  - `User` is extended by [`Student`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/Student.java), [`Owner`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/Owner.java), and [`Admin`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/Admin.java).
* **Polymorphism**:
  - `calculateTotalMonthlyCost(int sharingRooms)` is overridden differently by `Hostel`, `PG`, and `FeaturedAccommodationDecorator` and dispatched dynamically at runtime.
  - `RepositoryFactory` is implemented polymorphically by `MongoRepositoryFactory` and `InMemoryRepositoryFactory`.

---

### 2. "What 14 Design Patterns did you implement and where are they located?"
1. **Abstract Factory (`haven.repository`)**: [`RepositoryFactory`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/repository/RepositoryFactory.java) creates families of 7 domain repositories (`MongoRepositoryFactory` vs `InMemoryRepositoryFactory`).
2. **Builder (`haven.pattern.builder`)**: [`AccommodationBuilder`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/builder/AccommodationBuilder.java), [`HostelBuilder`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/builder/HostelBuilder.java), and [`PGBuilder`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/builder/PGBuilder.java) construct complex `Hostel` and `PG` objects step-by-step with fluent method chaining.
3. **Singleton (`haven.pattern`, `haven.db`)**: [`MongoDBConnection`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/MongoDBConnection.java), [`InMemoryStore`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/db/InMemoryStore.java), and [`HavenEventBus`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/event/HavenEventBus.java).
4. **Adapter (`haven.pattern`)**: [`GoogleMapsAdapter`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/GoogleMapsAdapter.java) and [`OpenStreetMapAdapter`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/OpenStreetMapAdapter.java) adapt browser navigation and `JXMapViewer2` OpenStreetMap tile rendering to [`NavigationService`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/NavigationService.java).
5. **Decorator (`haven.pattern.decorator`)**: [`FeaturedAccommodationDecorator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/decorator/FeaturedAccommodationDecorator.java) wraps an `Accommodation` dynamically to attach promotional badges (`★ FEATURED STAY`) and optional rent discounts.
6. **Facade (`haven.db`)**: `HostelDAO`, `UserDAO`, `EnquiryDAO`, and `ReviewDAO` provide simplified facades over the repository subsystem.
7. **Repository (`haven.repository`)**: Generic [`Repository<T, ID>`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/repository/Repository.java) with 7 specialized interfaces (`AccommodationRepository`, `UserRepository`, `EnquiryRepository`, `ReviewRepository`, `ImageRepository`, `MessageRepository`, `NotificationRepository`).
8. **Observer (`haven.pattern.event`)**: [`HavenEventBus`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/event/HavenEventBus.java) dispatches typed events (`AccommodationEvent`, `EnquiryEvent`, `ReviewEvent`, `ChatEvent`, `NotificationEvent`) to subscribed Swing views.
9. **State (`haven.pattern.state`)**: [`EnquiryState`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/state/EnquiryState.java) and its 5 concrete states (`PendingState`, `ContactedState`, `ConfirmedState`, `DeclinedState`, `CancelledState`) enforce legal enquiry status transitions.
10. **Command (`haven.pattern.command`)**: [`AdminCommand`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/AdminCommand.java) and [`CommandHistory`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/CommandHistory.java) encapsulate admin moderation actions with full **Undo / Redo** support.
11. **Template Method (`haven.pattern.report`)**: [`ReportGenerator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/report/ReportGenerator.java) defines the `final generate()` skeleton implemented by `BookingReceiptReport`, `ApplicantRosterReport`, and `SystemAnalyticsReport`.
12. **Iterator (`haven.pattern.iterator`)**: [`FilteredAccommodationIterator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/iterator/FilteredAccommodationIterator.java) lazily traverses accommodations matching [`AccommodationFilterCriteria`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/iterator/AccommodationFilterCriteria.java).
13. **Mediator (`haven.pattern.mediator`)**: [`PanelMediator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/mediator/PanelMediator.java) implemented by [`MainFrame`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/MainFrame.java) decouples cross-view navigation (`showMapView`, `openChatDialog`, `openImageGalleryDialog`, `openNotificationInbox`).
14. **Strategy**: Pluggable `Comparator<Accommodation>` sorting strategies and polymorphic pricing strategies.

---

### 3. "How does the State Pattern prevent illegal enquiry transitions?"
* **Explanation**:
  - Instead of scattered `if-else` string checks on `enquiry.getStatus()`, [`Enquiry`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/model/Enquiry.java) holds an [`EnquiryState`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/state/EnquiryState.java) reference (`PendingState`, `ContactedState`, `ConfirmedState`, `DeclinedState`, `CancelledState`).
  - Calling `enquiry.transitionTo("CONFIRMED")` delegates to `state.confirm()`.
  - Terminal states (`DeclinedState`, `CancelledState`) return `isTerminal() == true` and throw `IllegalStateException` if a caller attempts to transition them to `CONFIRMED` or `CONTACTED`.

---

### 4. "How does the Command Pattern enable Undo and Redo in the Admin Dashboard?"
* **Explanation**:
  - Every moderation operation in [`AdminDashboardPanel`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/AdminDashboardPanel.java) (such as [`ModerateListingCommand`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/ModerateListingCommand.java), [`ToggleUserActiveCommand`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/ToggleUserActiveCommand.java), [`DeleteUserCommand`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/DeleteUserCommand.java), [`DeleteReviewCommand`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/DeleteReviewCommand.java)) implements `AdminCommand` with `execute()` and `undo()`.
  - Before mutating the repository, each command snapshots the previous entity state.
  - [`CommandHistory`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/command/CommandHistory.java) maintains an `undoStack` and `redoStack` (`ArrayDeque<AdminCommand>`), allowing the Admin to undo or redo any moderation action with a single click.

---

### 5. "How does the Builder Pattern improve object creation for Hostel and PG?"
* **Explanation**:
  - `Hostel` and `PG` have 15+ attributes (ID, owner, name, college, rent, mess fee/utility charges, distance, coordinates, address, phone, amenities, images, moderation status, featured flag).
  - [`AccommodationBuilder<B>`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/builder/AccommodationBuilder.java) uses a self-bounded generic type (`AccommodationBuilder<B extends AccommodationBuilder<B>>`) so subclasses [`HostelBuilder`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/builder/HostelBuilder.java) and [`PGBuilder`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/builder/PGBuilder.java) inherit fluent setters without losing subtype return types when chaining `.id(...).name(...).fixedMessFee(...).build()`.

---

### 6. "How does the Template Method Pattern work in your Report Generator?"
* **Explanation**:
  - [`ReportGenerator`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/report/ReportGenerator.java) declares a `public final String generate()` method that defines the fixed algorithm skeleton:
    1. `buildHeaderBanner()` (hook with default implementation)
    2. `buildMetadataSection()` (abstract primitive operation)
    3. `buildBodySection()` (abstract primitive operation)
    4. `buildFooterSection()` (hook with default implementation)
  - Concrete subclasses ([`BookingReceiptReport`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/report/BookingReceiptReport.java), [`ApplicantRosterReport`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/report/ApplicantRosterReport.java), [`SystemAnalyticsReport`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/report/SystemAnalyticsReport.java)) supply only the specialized metadata and body content while inheriting consistent headers, timestamps, and formatting.

---

### 7. "How does the Embedded Map View work inside Swing?"
* **Explanation**:
  - [`MapPanel`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/MapPanel.java) and [`OpenStreetMapAdapter`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/pattern/OpenStreetMapAdapter.java) integrate `JXMapViewer2` with OpenStreetMap tile servers (`https://tile.openstreetmap.org`).
  - A custom `Painter<JXMapViewer>` renders interactive pin badges over each accommodation's geographic coordinates (`GeoPosition(lat, lon)`), color-coded by vacancy and featured status.
  - Clicking a pin selects the stay in the right-hand detail drawer, where students can open the photo gallery, chat with the warden, or jump back to the card list via `PanelMediator`.

---

## Quick Viva Walkthrough Sequence (3 Roles)

1. **Launch & Splash Screen**:
   - Run `.\run.bat` to show the branded [`SplashScreenWindow`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/SplashScreenWindow.java) initializing the repository engine and image storage.
2. **Student Role (`Demo Student` -> `student` / `pass123`)**:
   - Show the **2-Tier Advanced Filter Bar** (Type filter, Min/Max Rent, Distance `km`, Rating `4★+`, Amenities, and Quick Budget chips).
   - Click a property thumbnail on a card to open the **Photo Gallery Carousel** ([`ImageGalleryDialog`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/ImageGalleryDialog.java)).
   - Click **"Map View"** in the top navigation bar (or **"Map Pin"** on a card) to demonstrate the interactive **OpenStreetMap (`JXMapViewer2`)** view with price/vacancy pins and college quick-jump buttons.
   - Click **"Chat with Warden"** to send a live message in [`ChatDialog`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/ChatDialog.java).
   - Switch to **"My Bookings & History"** to filter enquiries by status, open an enquiry chat thread, or download a formatted booking receipt.
   - Click **"Notifications (N)"** in the header to view the persistent [`NotificationInboxDialog`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/NotificationInboxDialog.java).
3. **Owner Role (`Demo Owner` -> `owner` / `pass123`)**:
   - Show the **Manage Listings** table, click **"Manage Photos"** to upload a new property image via `JFileChooser`, or click **"+ Add New Listing"** to demonstrate inline validation ([`ValidationUtils`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/ValidationUtils.java)).
   - Switch to **"Student Enquiries"** to transition an enquiry state (`CONTACTED` / `CONFIRMED`) or click **"Message Student"** to reply to the student's chat thread.
   - Show the **"Analytics & Revenue"** tab with custom `Graphics2D` charts.
4. **Admin Role (`Demo Admin` -> `admin` / `admin123`)**:
   - Open the **Admin Governance Portal** ([`AdminDashboardPanel`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/AdminDashboardPanel.java)).
   - In **Property Moderation**, click **"Toggle Featured (★)"** or **"Flag Listing"**, then click **"Undo"** and **"Redo"** in the header bar to demonstrate the **Command Pattern** live.
   - In **System Analytics & Data**, click **"Export System Analytics Report (.txt)"** to demonstrate the **Template Method Pattern**.
5. **Run the 30-Test Verification Suite**:
   - Run `java -ea -cp "bin;lib/*" haven.TestEnhancements` to show all 30 automated assertion tests passing.
