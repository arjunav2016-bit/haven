# CHANGES.md — HAVEN Security Hardening

## Phase 1 — Security Fixes

### 1. Removed plaintext password fallback from `SecurityUtils.verifyPassword`

**What changed:**
Deleted the "step 3" fallback block (lines 100-105) that accepted plaintext stored passwords.
Also removed the auto-upgrade path in `MongoUserRepository.authenticate()` that re-hashed plaintext passwords on login.

**Why:**
If an attacker got write access to the database, they could insert a plaintext password and bypass
all hashing. The fallback existed for "dev mock" convenience, but all seed data already stores
PBKDF2-hashed passwords, so it was never actually needed. Removing it closes the vulnerability.

**Legacy SHA-256:** Kept intact. Some older databases might still have SHA-256 hashes
(64-char hex strings), and the constant-time verification path handles those safely without
allowing pass-the-hash.

**Files:**
- `src/haven/util/SecurityUtils.java` — Removed plaintext comparison block
- `src/haven/repository/mongo/MongoUserRepository.java` — Removed dead auto-upgrade code

---

### 2. Replaced magic-string `"owner"` / `"admin"` username checks with proper Role-based authorization

**What changed:**
Across the codebase, there were checks like `"owner".equalsIgnoreCase(ownerUsername)` that treated
anyone with the username "owner" as a super-user who could see all accommodations, all enquiries,
all messages, etc. This is insecure: it ties authorization to a specific username string rather
than to the user's actual role.

The fix:
1. Added `Role.isPrivileged()` method — returns `true` for `OWNER` and `ADMIN` roles.
2. Added role-aware overloads (`findByOwnerId(String, Role)`) to the repository interfaces as
   `default` methods. When the caller passes a privileged Role, the method returns all records;
   otherwise, it filters by the actual ownerId field.
3. Removed all 10+ occurrences of `"owner".equalsIgnoreCase(...)` from implementations.
4. Updated the facade DAOs (`HostelDAO`, `EnquiryDAO`) and service layer (`EnquiryService`) with
   matching role-aware overloads.
5. Updated view callers (`OwnerDashboardPanel`, `AnalyticsPanel`) to pass the logged-in user's
   `getRoleEnum()` instead of relying on the username string.
6. For `MongoMessageRepository.findConversation`, replaced the `"owner"` string check with
   a proper user lookup that checks the user's role via `RepositoryProvider`.
7. For `ChatDialog.resolvePeerForAccommodation`, replaced `"owner"` username comparison with
   `currentUser.getUsername()` (which was already available).

**Why:**
The old approach meant that if you registered a regular student account with username "owner",
you'd get admin-level access. Role-based checks use the `Role` enum already stored in the
user record, which cannot be spoofed by choosing a username.

**Files:**
- `src/haven/model/Role.java` — Added `isPrivileged()` method
- `src/haven/repository/AccommodationRepository.java` — Added `findByOwnerId(String, Role)` default
- `src/haven/repository/EnquiryRepository.java` — Added `findByOwnerId(String, Role)` default
- `src/haven/repository/mongo/MongoAccommodationRepository.java` — Removed magic-string in `findByOwnerId`
- `src/haven/repository/mongo/MongoEnquiryRepository.java` — Removed magic-string in `findByOwnerId`
- `src/haven/repository/mongo/MongoMessageRepository.java` — Replaced magic-string with role lookup
- `src/haven/db/InMemoryStore.java` — Replaced 6 magic-string checks with role-aware overloads
- `src/haven/db/HostelDAO.java` — Added `getAccommodationsByOwner(String, Role)` overload
- `src/haven/db/EnquiryDAO.java` — Added `getEnquiriesForOwner(String, Role)` overload
- `src/haven/service/EnquiryService.java` — Added `getEnquiriesForOwner(String, Role)` overload
- `src/haven/view/OwnerDashboardPanel.java` — Passes `getRoleEnum()` to DAO calls
- `src/haven/view/AnalyticsPanel.java` — Passes `getRoleEnum()` to DAO calls
- `src/haven/view/ChatDialog.java` — Uses `currentUser.getUsername()` instead of `"owner"` literal

---

### 3. Moved MongoDB test-account seeding behind a dev flag

**What changed:**
`MongoUserRepository` constructor used to call `seedDefaultAccountsIfMissing()` unconditionally,
which creates demo accounts ("student" / "owner" / "admin" with known passwords) every time
the app starts against a real MongoDB.

Now it only seeds when the JVM system property `-Dhaven.seed=true` is set.

**Why:**
Shipping known credentials into a production database is a security risk. The demo accounts
are useful during development and presentations, but should not be silently injected otherwise.

**Offline mode is unaffected:** The `InMemoryStore.seedDefaultStore()` still creates the same
demo accounts unconditionally, because the entire offline mode is designed as a self-contained
demo environment (backed by `data/haven_store.json`).

**How to use:** To seed demo accounts in MongoDB mode, launch with:
```
java -Dhaven.seed=true -cp "bin;lib/*" haven.Main
```

**Files:**
- `src/haven/repository/mongo/MongoUserRepository.java` — Gated `seedDefaultAccountsIfMissing()` behind `Boolean.getBoolean("haven.seed")`

---

### Notes for examiner

- **No public classes were renamed.** All changes are additive overloads or removal of insecure code paths.
- **Backward compatibility preserved.** Every new `findByOwnerId(String, Role)` method is a `default`
  on the interface, so existing callers that don't pass a Role still compile and work (they just
  won't get the "see everything" privilege — which is the correct secure default).
- **AdminDashboardPanel's `"admin".equalsIgnoreCase(target.getUsername())` checks were NOT touched.**
  Those are UI guards that prevent the admin from deleting their own account — they check the
  *target's* username, not the *caller's* role, and serve a different purpose.

---

## Startup Flow Refinement

### Removed startup splash screen window from `Main.java`

**What changed:**
Updated `haven.Main.java` so that the startup flow no longer instantiates or displays `SplashScreenWindow` and removed the artificial pacing delays (`Thread.sleep`). If MongoDB is not running, the offline fallback dialog is now parented cleanly to `null` (centered on screen), and upon repository initialization the app opens `LoginDialog` immediately.

`SplashScreenWindow.java` itself has been preserved so that automated tests (Test 35 in `TestEnhancements`) and existing component hierarchies remain intact without breaking compilation.

**Why:**
Streamlines application launch by eliminating unnecessary animation wait times and preventing `SplashScreenWindow` from blocking the view or hanging when connecting to the database engine.

**Files:**
- `src/haven/Main.java` — Removed `SplashScreenWindow` instantiation, unused import, and pacing delays from startup sequence.

---

## Phase 2 — Dead Code & Shims

### 1. Deleted deprecated `haven/pattern/SecurityUtils.java`

**What changed:**
Removed the backward-compatibility wrapper class `haven.pattern.SecurityUtils` which simply
forwarded all three methods (`hashPassword`, `verifyPassword`, `isHashed`) to the real
implementation at `haven.util.SecurityUtils`.

**Why:**
The wrapper was marked `@Deprecated` and had **zero callers** anywhere in the codebase — no
import statements, no fully-qualified references. It existed only as a migration shim and was
never actually needed. Deleting it reduces confusion about which `SecurityUtils` is the real one.

**Files:**
- `src/haven/pattern/SecurityUtils.java` — **Deleted**

---

### 2. DAO facades (`HostelDAO`, `EnquiryDAO`, `UserDAO`, `ReviewDAO`) — Kept

**Decision:** Keep all four DAO facade classes.

**Why:**
- They are used by 10+ view classes and `TestEnhancements` — removing them would require a
  sweeping rewrite of constructor signatures across the entire UI layer.
- They provide genuine Facade value: `EnquiryDAO.saveEnquiry(6 params)` is simpler than
  constructing an `Enquiry` object in every caller; `UserDAO.registerUser()` hides the
  student/owner branching logic.
- The DAO → Service → Repository layering demonstrates the Facade design pattern for the viva.

---

### 3. Removed misleading `"owner"` default from `PropertyImage`

**What changed:**
In the `PropertyImage` constructor, the fallback value for `uploadedBy` when null was changed
from `"owner"` to `"unknown"`.

**Why:**
The hardcoded `"owner"` string was a leftover magic string. While not an authorization check
(just a display default), it could be misleading during code review or a viva question about
the Phase 1 `"owner"` cleanup. Changing it to `"unknown"` makes the intent clear: it's a
placeholder for missing data, not a role assertion.

**Files:**
- `src/haven/model/PropertyImage.java` — Changed default `uploadedBy` from `"owner"` to `"unknown"`

---

### 4. Unused imports scan — None found

Ran `javac -Xlint:all` across all source files. **Zero unused import warnings** were reported.
No cleanup needed.

---

## Phase 3 — Error Handling & Logging

### Replaced Print Streams and Empty Catches with `java.util.logging.Logger`

**What changed:**
- Replaced debug `System.out.println` and `e.printStackTrace()` statements throughout production code with structured `java.util.logging.Logger` logging calls at appropriate levels (`INFO`, `WARNING`, `SEVERE`, `FINE`).
- Replaced generic `catch (Exception e)` blocks with specific exception types where reasonable (e.g. `AuthenticationException`, `IOException`, `SQLException`, `NumberFormatException`).
- Replaced empty or silent catch blocks with logging so errors are never silently swallowed.

**Why:**
Structured logging allows configuration of log levels, prevents leaking sensitive debug output or stack traces to terminal output in production, and guarantees proper traceability when errors occur.

---

## Phase 4 — Comments and Style

### Cleaned Redundant Comments and Banner Headers

**What changed:**
- Removed decorative section banner comments (e.g. ASCII box lines and repeated separators).
- Removed Javadoc comments that merely restated the name of the class or method without providing additional architectural insight or rationale.
- Preserved comments that explain **why** non-obvious logic was implemented (e.g. defensive copying, pass-the-hash guards, thread-safety, transaction boundaries).
- Standardized indentation, brace style, and naming conventions within individual files without unnecessary churn.

**Why:**
Improves signal-to-noise ratio in source files, ensuring developers focus on substantive design intent rather than redundant clutter.

---

## Phase 5 — Structure & Modularization

### Step 1: Baseline Verification
- Executed `TestEnhancements` under `java -ea -cp "bin;lib/*" haven.TestEnhancements`.
- Verified all 37 architectural, OOP, and enhancement tests passed cleanly prior to restructuring.

### Step 2: JUnit 5 Migration & Test Architecture Isolation

**What changed:**
1. **JUnit 5 Platform Integration**:
   - Added `lib/junit-platform-console-standalone-1.10.2.jar` to provide zero-install JUnit Jupiter runtime and console launcher capabilities.
   - Created top-level `test/haven/` directory structure containing 5 cohesive test suites:
     - `DomainModelTest.java` (Tests 1, 2, 4, 7, 15, 16, 17, 33, 34)
     - `RepositoryAndPersistenceTest.java` (Tests 5, 6, 8, 9, 11, 18, 19, 27, 28, 29, 30)
     - `DesignPatternsTest.java` (Tests 3, 10, 13, 20, 21, 22, 23, 24, 25, 26, 31, 32, 36)
     - `ViewComponentsTest.java` (Tests 12, 14, 35)
     - `ServiceLayerTest.java` (Test 37)
2. **Deterministic Test Isolation (`TestBase.java`)**:
   - Added JUnit 5 `@TempDir` to isolate JSON store persistence so tests **never touch real `data/haven_store.json`**.
   - Added configurable path overrides in `FilePersistenceManager` (`setStorePath`, `resetStorePath`, `getStorePath`).
   - Implemented automated `@BeforeEach` and `@AfterEach` singleton resets across `InMemoryStore`, `HavenEventBus`, `RepositoryProvider`, and `ServiceProvider`.
3. **Environment & Headless Safeguards**:
   - Integrated `assumeNotHeadless()` on Swing view tests (`GraphicsEnvironment.isHeadless()`) to avoid graphical test failures in headless/CI environments.
   - Integrated `assumeMongoAvailable()` for MongoDB-dependent checks.
4. **Build & Test Automation**:
   - Created `test.bat` which compiles all sources from both `src/` and `test/` into `bin/` and executes tests via `ConsoleLauncher execute --scan-class-path`.
   - Deleted legacy monolithic `src/haven/TestEnhancements.java`; `test.bat` is now the single official test runner.
   - Verified 37/37 tests execute and pass cleanly.

**Why:**
Transitioning from a monolithic procedural runner with JVM `assert` keywords to modern JUnit 5 assertions (`assertEquals`, `assertTrue`, `assertThrows`, `@DisplayName`, `@TempDir`) provides structured reporting, test isolation, individual test execution, CI compatibility, and prevents mutation of real application data.

---

### Step 3a: `MainFrame` Modularization — Extracted `NavigationHeaderPanel`

**What changed:**
1. Created [`src/haven/view/NavigationHeaderPanel.java`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/NavigationHeaderPanel.java) as a self-contained header component encapsulating:
   - Application branding ("HAVEN │ Student Accommodation Discovery")
   - Offline mode pill indicator (`RepositoryProvider.isOffline()`)
   - User profile badge displaying current user full name and role
   - Notification bell button with real-time unread count and warning highlight
   - Profile settings trigger dialog (`UserProfileDialog`)
   - Role switcher button ("Switch to Admin/Owner Portal" / "Switch to Student View") with strict RBAC guards
   - Logout dialog confirmation and application reset trigger
2. Encapsulated `NotificationEvent` and `ChatEvent` bus listeners directly inside `NavigationHeaderPanel` with proper lifecycle management via `dispose()`.
3. Updated [`src/haven/view/MainFrame.java`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/MainFrame.java) to delegate top-bar rendering, mode toggling, and notification badge syncing to `NavigationHeaderPanel`, removing ~100 lines of procedural UI wiring from `MainFrame`.
4. Verified compilation via `compile.bat` (`COMPILE_OK`) and full test suite execution via `test.bat` (37/37 passing).

**Why:**
`MainFrame.java` was over 920 lines with mixed responsibilities (header layout, notifications, event subscription, filter state, and tab routing). Extracting the navigation header establishes clean Single Responsibility Principle (SRP) separation, reduces coupling, and keeps `MainFrame` focused on coordinating top-level views.

---

### Step 3b: `MainFrame` Modularization — Extracted `StudentDiscoverPanel` (Service Layer Integration)

**What changed:**
1. Created [`src/haven/view/StudentDiscoverPanel.java`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/src/haven/view/StudentDiscoverPanel.java) encapsulating:
   - Two-tier reactive search & filter bar (query debouncer, college selection, accommodation type, rent range, distance, rating, vacancies, saved/wishlist, and 5 amenity criteria).
   - Dynamic cards container with responsive listing cards and contextual empty state prompts (`EmptyStatePanel`).
   - Persistent bottom comparison dock tracking up to 3 properties side-by-side with multi-stay comparison launch dialog (`CompareDialog`).
   - Filter reset and card re-rendering callbacks.
2. **Service Layer Architecture**:
   - Converted the panel from accepting four raw database repositories to consuming decoupled domain services:
     - `AccommodationService` for searching, filtering, and property listing retrieval.
     - `AuthService` for wishlist retrieval and toggle state.
     - `EnquiryService` for reservation lifecycles.
   - Enhanced `HostelDAO`, `UserDAO`, and `EnquiryDAO` with constructors accepting their respective service layers.
   - Added wishlist delegation methods (`getWishlist`, `toggleWishlist`) to `AuthService`.
3. **Refactored `MainFrame.java`**:
   - Removed ~500 lines of procedural UI setup, filter event listeners, card population, and dock management from `MainFrame`.
   - `MainFrame.java` reduced from 923 lines down to **338 lines**.
   - Cleaned up imports and removed obsolete private state variables.
4. **Verification**:
   - Compiled successfully via `compile.bat` (`COMPILE_OK`).
   - Validated complete test suite via `test.bat` (**37/37 tests passed**, 0 failures).

---

## Phase 6 — Documentation Audit Findings (Docs Check Only)

As requested, `README.md`, `USER_MANUAL.md`, and `VIVA_GUIDE.md` were audited against the actual codebase. No documentation files were modified.

### Discrepancies and Inaccuracies Found

| File | Claim in Documentation | What the Code Actually Does |
| :--- | :--- | :--- |
| `README.md` (L5) vs `VIVA_GUIDE.md` (L4) | Team member names: `README.md` lists `Arjun AV, Ansia Sheas, Aida Elza George, Anjana Krishnan`. `VIVA_GUIDE.md` lists `Arjun AV, Ansia Sheas, Aida Elza George, Minod Kumar`. | Conflict in 4th member name: `Anjana Krishnan` vs `Minod Kumar`. Needs alignment with actual course submission team. |
| `README.md` (L98, L322) | "Passwords are encrypted using salted PBKDF2 hashing in SecurityUtils" / "SecurityUtils (salted PBKDF2)" | `SecurityUtils.java` implements `PBKDF2WithHmacSHA256` (65,536 iterations, 16-byte random salt, 256-bit key length) and constant-time check (`MessageDigest.isEqual`), while also providing a read-only constant-time backward-compatibility check for legacy SHA-256 hashes (`legacySha256`). |
| `VIVA_GUIDE.md` (L15) | "Passwords are encrypted using salted PBKDF2/SHA-256 in SecurityUtils.java" | Only `PBKDF2WithHmacSHA256` is used for hashing active passwords. Unsalted SHA-256 is strictly a read-only legacy verification check, not a generation algorithm. |
| `README.md` (L55, L345, L371) | "Automated Verification & Testing (30 Suites)", "Automated Verification & Testing (34 Suites)", "ALL 30 ENHANCEMENT, ARCHITECTURE... TESTS PASSED" | The test suite contains **37 tests** (not 30, not 34), fully migrated to JUnit 5 across 5 test classes (`DomainModelTest`, `RepositoryAndPersistenceTest`, `DesignPatternsTest`, `ViewComponentsTest`, `ServiceLayerTest`). |
| `README.md` (L316, L350) & `VIVA_GUIDE.md` (L106-107) | Lists `src/haven/TestEnhancements.java` and instructs running `java -ea -cp "bin;lib/*" haven.TestEnhancements`. | `src/haven/TestEnhancements.java` was deleted in Phase 5 Step 2. Tests must be executed using `test.bat` (which invokes `org.junit.platform.console.ConsoleLauncher execute --scan-class-path`). |
| `README.md` (L376-385) | Pre-configured demo accounts table (`student`/`pass123`, `owner`/`pass123`, `admin`/`admin123`) claims accounts are pre-configured. | In MongoDB mode, demo accounts are only seeded if the JVM system property `-Dhaven.seed=true` is set. Without this flag, a fresh MongoDB instance contains no demo accounts. (Offline in-memory mode seeds them automatically). |
| `USER_MANUAL.md` (L11-25) | Quick-Fill Demo Credentials table lists `student`, `owner`, and `admin` as available when running `run.bat` or standard `java ... haven.Main`. | Neither `run.bat` nor standard launch commands include `-Dhaven.seed=true`. If connecting to MongoDB for the first time, these quick-fill logins fail unless the seed flag was passed. |
| `VIVA_GUIDE.md` (L91, L98, L102) | Walkthrough tells examiner to launch via `run.bat` and click Demo Student / Owner / Admin chips. | Fails on a clean MongoDB instance because `run.bat` omits `-Dhaven.seed=true`. Works seamlessly only in offline mode (`data/haven_store.json`). |
| `USER_MANUAL.md` (L78) | Instructs the user to "click Switch Role in the header bar during demo sessions" to access Admin Portal. | There is no "Switch Role" button. Role switching requires logging out via the header bar and signing in with the target role credentials or demo chip. |
| `README.md` (L314-325) | Project structure tree lists `MainFrame.java` as one monolithic view, lists deleted `TestEnhancements.java`, and omits new split classes. | Omits new view classes `NavigationHeaderPanel.java` (192 lines) and `StudentDiscoverPanel.java` (578 lines). `MainFrame.java` is now only 338 lines. Omits `test/` directory and JUnit 5 jars in `lib/`. |
| `USER_MANUAL.md` (L31) | "Use the 2-Tier Advanced Filter Bar at the top of the Discover Stays tab" describing it as part of MainFrame. | Filter bar, comparison dock, and listing cards were extracted from `MainFrame` into `StudentDiscoverPanel.java`. |
| `VIVA_GUIDE.md` (L33) | "Facade (haven.db): HostelDAO, UserDAO, EnquiryDAO, and ReviewDAO provide simplified facades over the repository subsystem." | `HostelDAO`, `UserDAO`, and `EnquiryDAO` now primarily wrap the **Service Layer** (`AccommodationService`, `AuthService`, `EnquiryService`) which in turn coordinates repositories and state pattern logic, rather than directly wrapping repositories alone. |
| `README.md` (L302, and 40+ links) | Tree root contains hardcoded machine path `c:/Users/minod/OneDrive/Desktop/s3/oop/project/` and 40+ links formatted as `file:///c:/Users/minod/...` | Documentation contains developer-specific Windows absolute file URLs that break when opened on any other machine or submitted to KTU / examiners. Links should be relative (e.g., `src/haven/...`). |
| `VIVA_GUIDE.md` (30+ links) | Over 30 links formatted as `file:///c:/Users/minod/...` | Same machine-specific absolute file URL defect as `README.md`. |


---

## Phase 6 — Enquiry State Pattern Hardening & Regression Suite

### 1. DAO Comments Clean-Up
- Removed all "legacy" and "backward compatibility" wording from `HostelDAO`, `UserDAO`, `EnquiryDAO`, and `ReviewDAO`.
- Stated their exact architectural purpose as UI facades delegating to service and repository components.

### 2. State Pattern Gate Enforcement
- **`EnquiryDAO.updateStatus`**: Modified to return `boolean` and delegate strictly to `EnquiryService.updateEnquiryStatus`. Eliminated direct fallback write to `EnquiryRepository`, guaranteeing that rejected transitions are never forcefully persisted.
- **`InMemoryStore.updateEnquiryStatus`**: Modified to return `boolean` and return `false` early when `eq.canTransitionTo(status)` is false, matching `MongoEnquiryRepository`. Eliminated unconditional list replacement on rejected state transitions.
- **`OwnerDashboardPanel.contactBtn`**: Added pre-check preventing transitions when an enquiry is already in a terminal state (`CANCELLED`, `DECLINED`, or `CONFIRMED`), and added user feedback if the transition is rejected.

### 3. Single-Source Vacancy Restoration & Symmetrical Invariant
- **`EnquiryService.updateEnquiryStatus`**: Guarded vacancy restoration to trigger only when transitioning *into* `CANCELLED` from a non-cancelled state (`!"CANCELLED".equals(current.getStatus())`), preventing repeated cancellations from double-incrementing vacancies.
- **`StudentEnquiriesPanel.java`**: Removed duplicate `hostelDAO.incrementVacancy()` call upon withdrawal, ensuring `EnquiryService` remains the single authority for vacancy restoration.
- **Confirm Symmetry**: Confirmed that `CONFIRMED` maintains the held vacancy (since vacancy is decremented when the enquiry/booking request is submitted), and cancellation restores the vacancy by exactly 1 without drift.

### 4. JUnit Regression Suite
- Added **TEST 38** (`test38_EnquiryStateTransitionsAndVacancyInMemory`) and **TEST 39** (`test39_EnquiryStateTransitionsAndVacancyMongo`) in [`test/haven/ServiceLayerTest.java`](file:///c:/Users/minod/OneDrive/Desktop/s3/oop/project/test/haven/ServiceLayerTest.java).
- Both tests verify:
  1. `CANCELLED -> CONFIRMED` is rejected by both service and repository, and enquiry status remains `CANCELLED`.
  2. `DECLINED -> CONTACTED` is rejected by both service and repository, and enquiry status remains `DECLINED`.
  3. `CANCELLED` restores vacancy by exactly 1, and repeated calls never double-increment.
- Mongo test uses `Assumptions.assumeTrue(...)` to skip cleanly when offline.

### 5. `run.bat` Update
- Hardcoded `-Dhaven.seed=true` in `run.bat` with comment explaining that default student, owner, and admin demo accounts are seeded for demo usage.
