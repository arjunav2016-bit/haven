# HAVEN - User Manual & Role Guide

Welcome to **HAVEN** (*Hostel & Accommodation Verification & Enrollment Network*). This manual walks through all features available to **Students**, **Property Owners**, and **Platform Administrators**.

---

## 1. Launching HAVEN & Demo Accounts

### Starting the Application
- **Windows One-Click**: Double-click `run.bat` in the project root directory.
- **Command Line**:
  ```powershell
  java -Dfile.encoding=UTF-8 -cp "bin;lib/*" haven.Main
  ```
- During startup, the **HAVEN Splash Screen** initializes the database engine (connecting to MongoDB on `localhost:27017` or automatically falling back to `data/haven_store.json` in Offline Mode) and verifies disk-backed property images in `images/`.

### Quick-Fill Demo Credentials
On the **Sign In** dialog, click any of the three quick-fill buttons at the bottom to populate credentials automatically:

| Quick-Fill Button | Username | Password | Role |
| :--- | :--- | :--- | :--- |
| **Demo Student** | `student` | `pass123` | Student Discovery, Map, Chat, & Bookings |
| **Demo Owner** | `owner` | `pass123` | Property CRUD, Photo Upload, Enquiries, & Analytics |
| **Demo Admin** | `admin` | `admin123` | User/Listing/Review Moderation, Undo/Redo, & System Reports |

---

## 2. Student Guide (`STUDENT`)

### 2.1 Discovering & Filtering Stays
Use the **2-Tier Advanced Filter Bar** at the top of the **Discover Stays** tab:
- **Search Box**: Type any hostel name, road, or landmark (debounced live filtering).
- **Near College**: Filter by `SCTCE`, `CET`, `GEC Barton Hill`, `MBCET`, or `LBS`.
- **Gender & Stay Type**: Filter by `Boys`, `Girls`, or `Co-ed`, and choose `All Types`, `Hostels Only`, or `PGs Only`.
- **Sort Order**: Sort by *Featured First*, *Lowest Rent*, *Highest Rating*, *Nearest Distance*, or *Most Vacancies*.
- **Tier 2 Range & Amenity Filters**:
  - Enter **Min ₹**, **Max ₹**, and **Max km** bounds, or click a quick budget chip (`< ₹6,000`, `< ₹8,000`, `< ₹10,000`).
  - Select **Min Rating** (`3★+`, `4★+`, `4.5★+`) and toggle **Vacancies Only** or **Saved Only**.
  - Check any required amenities (`WiFi`, `Food / Mess`, `Laundry`, `Power Backup`, `CCTV Security`, `Parking`, `Attached Bath`).

### 2.2 Viewing Property Photo Galleries
- Every listing card displays a property thumbnail with a photo count badge (`2 photos`).
- Click the **thumbnail image** (or the **Photos** button in the Map drawer) to open the **Property Photo Gallery** carousel (`< Prev` / `Next >`).

### 2.3 Interactive Map View (`JXMapViewer2` + OpenStreetMap)
- Click **Map View** in the top navigation bar to switch to the embedded OpenStreetMap canvas, or click **Map Pin** on any listing card to center the map directly on that stay.
- **Pan & Zoom**: Drag with the mouse to pan; scroll the mouse wheel or use the zoom slider to zoom.
- **Interactive Pins**: Click any property pin on the map to inspect its rent, distance, vacancies, and rating in the right-hand detail panel.
- **College Quick-Jump**: Use the top toolbar buttons (`SCTCE`, `CET`, `GECBH`, `MBCET`, `LBS`, `Fit All`) to jump directly to any campus.

### 2.4 In-App Messaging & Notifications
- Click **Chat with Warden** on any listing card (or **Chat with Owner** in the **My Bookings & History** tab) to open a real-time message thread with the property owner.
- Click **Notifications** in the top navigation bar to view persistent alerts for booking confirmations, enquiry status updates, and incoming chat messages.

### 2.5 Booking History & Receipts
- Open the **My Bookings & History** tab to view all your submitted enquiries.
- Filter by status (`PENDING`, `CONTACTED`, `CONFIRMED`, `DECLINED`, `CANCELLED`) or sort by newest/oldest date.
- Select an enquiry to **Chat with Owner**, **Download Slip (.txt)**, or **Withdraw Enquiry** (which transitions the enquiry to `CANCELLED` and automatically restores the room vacancy).

---

## 3. Property Owner Guide (`OWNER`)

### 3.1 Managing Listings & Uploading Photos
- In the **Owner Portal**, the **Manage Listings & Vacancies** tab displays all properties owned by your account.
- Click **+ Add New Listing** or **Edit Selected** to configure property details, GPS coordinates, and amenities with live inline form validation.
- Click **Manage Photos** on a selected property to open the **Property Photo Gallery**, where you can upload new `JPG`/`PNG`/`GIF` images from your computer (`+ Upload Photo`) or delete existing photos.

### 3.2 Managing Student Enquiries & Chatting with Applicants
- Open the **Student Enquiries** tab to process incoming student applications.
- Use **Confirm Booking**, **Mark Contacted**, or **Decline & Restore Vacancy** to transition the enquiry through its lifecycle state machine (automatically sending a persistent notification to the student).
- Click **Message Student** to open the in-app chat thread with the selected applicant, or click **Export Applicants Roster** to save a formatted `.txt` report.

---

## 4. Administrator Guide (`ADMIN`)

Sign in with `admin` / `admin123` (or click **Switch Role** in the header bar during demo sessions) to access the **Admin Governance Portal**:

1. **User Management Tab**:
   - View all registered `STUDENT`, `OWNER`, and `ADMIN` accounts and their `ACTIVE` / `DEACTIVATED` status.
   - Click **Toggle Active / Deactivate** to suspend or restore login access, or **Delete User** to remove an account.
2. **Property Moderation Tab**:
   - Inspect all Hostels and PGs across the platform.
   - Click **Approve Listing**, **Flag Listing**, or **Reject Listing** (rejected listings are hidden from student discovery).
   - Click **Toggle Featured (★)** to promote or unpromote a listing with a gold border and featured badge.
   - Click **Manage Photos** to curate photos for any property.
3. **Review Moderation Tab**:
   - Inspect student reviews across all stays, **Toggle Flag**, or **Delete Review**.
4. **Undo / Redo Command Toolbar**:
   - All moderation actions in the Admin Portal use the **Command Pattern**. Click **Undo** or **Redo** in the top-right Admin header bar at any time to reverse or replay administrative changes.
5. **System Analytics & Data Tab**:
   - View platform-wide KPI cards and charts, export the **System Analytics Report (`.txt`)**, export a **JSON Snapshot (`.json`)**, or **Reset to Default Seed Data**.
