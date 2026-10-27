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
