package haven.test;

import haven.db.InMemoryStore;
import haven.db.UserDAO;
import haven.model.*;
import haven.pattern.PropertyImageStorage;
import haven.pattern.event.ChatEvent;
import haven.pattern.event.HavenEventBus;
import haven.pattern.event.HavenEventListener;
import haven.pattern.event.NotificationEvent;
import haven.repository.*;
import haven.repository.inmemory.FilePersistenceManager;
import haven.repository.inmemory.InMemoryRepositoryFactory;
import haven.service.ServiceProvider;
import haven.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Repository & Persistence Engine Tests")
public class RepositoryAndPersistenceTest extends TestBase {

    @Test
    @DisplayName("[TEST 5] Offline In-Memory store & Profile operations")
    public void test05_OfflineInMemoryStoreAndProfileOperations() throws Exception {
        InMemoryStore memStore = InMemoryStore.getInstance();
        memStore.resetToDefaultSeed();
        assertTrue(memStore.getAllAccommodations().size() > 0, "InMemoryStore failed to seed accommodations");
        assertTrue(memStore.userExists("student"), "Default student user missing in InMemoryStore");

        User auth = memStore.authenticate("student", "pass123");
        assertNotNull(auth, "InMemoryStore authentication failed");
        assertNull(auth.getPassword(), "Password was not stripped from in-memory session user");

        // Profile & Password Update
        memStore.updateProfile("student", "Minod K", "9998887776", "SCTCE", "Boys", 9000.0);
        User updatedStudent = memStore.getUser("student");
        assertEquals("Minod K", updatedStudent.getFullName(), "InMemoryStore failed to update user fullName");
        assertEquals("9998887776", updatedStudent.getPhone(), "InMemoryStore failed to update user phone");

        boolean pwdChanged = memStore.changePassword("student", "pass123", "newpass123");
        assertTrue(pwdChanged, "changePassword failed with valid credentials");

        User reAuth = memStore.authenticate("student", "newpass123");
        assertNotNull(reAuth, "Authentication with new password failed");
        assertNull(reAuth.getPassword(), "Password was not stripped from in-memory re-authenticated user");
    }

    @Test
    @DisplayName("[TEST 6] Atomic vacancy decrement & zero-vacancy protection")
    public void test06_AtomicVacancyDecrementAndZeroVacancyProtection() {
        InMemoryStore memStore = InMemoryStore.getInstance();
        memStore.resetToDefaultSeed();
        Accommodation testStay = memStore.getAllAccommodations().get(0);
        int initialVac = testStay.getVacancies();
        testStay.setVacancies(1);

        boolean firstDec = memStore.decrementVacancy(testStay.getId());
        assertTrue(firstDec, "First decrement should succeed for vacancy = 1");
        assertEquals(0, testStay.getVacancies(), "Vacancy should now be 0");

        boolean secondDec = memStore.decrementVacancy(testStay.getId());
        assertFalse(secondDec, "Second decrement should fail and return false when vacancies = 0");
        assertEquals(0, testStay.getVacancies(), "Vacancy should remain 0");

        testStay.setVacancies(initialVac);
    }

    @Test
    @DisplayName("[TEST 8] Generic Repository Pattern & Domain Contracts")
    public void test08_GenericRepositoryPatternAndDomainContracts() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        UserRepository userRepo = RepositoryProvider.getUserRepository();
        EnquiryRepository enquiryRepo = RepositoryProvider.getEnquiryRepository();
        ReviewRepository revRepo = RepositoryProvider.getReviewRepository();

        assertNotNull(accRepo, "AccommodationRepository instance is null");
        assertTrue(accRepo.count() > 0, "AccommodationRepository should contain seeded stays");
        assertTrue(accRepo.findById("BH01").isPresent(), "BH01 missing from AccommodationRepository");
        assertTrue(userRepo.userExists("student"), "student user missing in UserRepository");
        assertFalse(revRepo.getReviewsForAccommodation("BH01").isEmpty(), "BH01 should have reviews");
    }

    @Test
    @DisplayName("[TEST 9] Abstract Factory Pattern (RepositoryFactory)")
    public void test09_AbstractFactoryPatternRepositoryFactory() {
        RepositoryFactory currentFactory = RepositoryProvider.getFactory();
        assertNotNull(currentFactory, "RepositoryFactory is null");
        assertNotNull(currentFactory.getAccommodationRepository(), "Factory failed to supply AccommodationRepository");
        assertNotNull(currentFactory.getUserRepository(), "Factory failed to supply UserRepository");
        assertNotNull(currentFactory.getEnquiryRepository(), "Factory failed to supply EnquiryRepository");
        assertNotNull(currentFactory.getReviewRepository(), "Factory failed to supply ReviewRepository");
    }

    @Test
    @DisplayName("[TEST 11] File-Backed Offline JSON Persistence Engine")
    public void test11_FileBackedOfflineJsonPersistenceEngine() {
        InMemoryStore memStore = InMemoryStore.getInstance();
        memStore.resetToDefaultSeed();
        memStore.persist();

        File storeFile = new File(FilePersistenceManager.getStorePath());
        assertTrue(storeFile.exists(), "Isolated test store file was not created: " + storeFile.getPath());
        assertTrue(storeFile.length() > 0, "Isolated test store file is empty");

        FilePersistenceManager.PersistedData loadedData = FilePersistenceManager.load();
        assertNotNull(loadedData, "FilePersistenceManager failed to load persisted data");
        assertFalse(loadedData.accommodations.isEmpty(), "Loaded accommodations is empty");
        assertTrue(loadedData.users.containsKey("student"), "Loaded users missing 'student'");
        Accommodation firstAcc = loadedData.accommodations.get(0);
        assertTrue(firstAcc.getLatitude() != 0.0 && firstAcc.getLongitude() != 0.0,
                "Coordinates were not preserved across JSON file persistence (got 0.0, 0.0)");
    }

    @Test
    @DisplayName("[TEST 18] User Profile Multi-Subclass Persistence (Student & Owner)")
    public void test18_UserProfileMultiSubclassPersistence() {
        UserRepository userRepo = RepositoryProvider.getUserRepository();
        UserDAO testUserDAO = new UserDAO();
        testUserDAO.updateProfile("student", "Updated Student Name", "9112233445", "SCTCE", "Boys", 9500.0);
        User fetchedStudent = userRepo.findById("student").orElse(null);
        assertNotNull(fetchedStudent, "Student user not found in repository");
        assertEquals("Updated Student Name", fetchedStudent.getFullName(), "Student fullName update failed in repository");
        assertEquals("9112233445", fetchedStudent.getPhone(), "Student phone update failed in repository");

        // Owner profile update
        User ownerUser = userRepo.findById("owner").orElse(null);
        assertNotNull(ownerUser, "Owner user not found in repository");
        ownerUser.setFullName("Updated Owner Name");
        ownerUser.setPhone("9887766554");
        userRepo.updateUserProfile(ownerUser);
        User fetchedOwner = userRepo.findById("owner").orElse(null);
        assertNotNull(fetchedOwner, "Owner user not found in repository after update");
        assertEquals("Updated Owner Name", fetchedOwner.getFullName(), "Owner fullName update failed in repository");
        assertEquals("9887766554", fetchedOwner.getPhone(), "Owner phone update failed in repository");
    }

    @Test
    @DisplayName("[TEST 19] Seamless Dual-Engine Switching (InMemory Factory)")
    public void test19_SeamlessDualEngineSwitching() {
        RepositoryFactory originalFactory = RepositoryProvider.getFactory();
        try {
            InMemoryRepositoryFactory offlineFactory = new InMemoryRepositoryFactory();
            RepositoryProvider.setFactory(offlineFactory);

            AccommodationRepository offAccRepo = RepositoryProvider.getAccommodationRepository();
            UserRepository offUserRepo = RepositoryProvider.getUserRepository();
            EnquiryRepository offEnqRepo = RepositoryProvider.getEnquiryRepository();
            ReviewRepository offRevRepo = RepositoryProvider.getReviewRepository();

            assertNotNull(offAccRepo, "Offline AccommodationRepository is null");
            assertNotNull(offUserRepo, "Offline UserRepository is null");
            assertNotNull(offEnqRepo, "Offline EnquiryRepository is null");
            assertNotNull(offRevRepo, "Offline ReviewRepository is null");

            assertTrue(offAccRepo.count() > 0, "Offline AccommodationRepository should have seeded records");
            assertTrue(offUserRepo.userExists("student"), "Offline UserRepository should contain student");
            assertFalse(offRevRepo.getReviewsForAccommodation("BH01").isEmpty(), "Offline ReviewRepository should have reviews");
        } finally {
            RepositoryProvider.setFactory(originalFactory);
        }
    }

    @Test
    @DisplayName("[TEST 27] Property Image Storage & ImageRepository CRUD")
    public void test27_PropertyImageStorageAndImageRepositoryCrud() {
        AccommodationRepository accRepo = RepositoryProvider.getAccommodationRepository();
        Accommodation hostel = accRepo.findById("BH01").orElseThrow();
        PropertyImageStorage.ensureDefaultImagesForAccommodation(hostel);
        assertFalse(hostel.getImagePaths().isEmpty(), "Default images were not generated for hostel");

        File firstImgFile = new File(hostel.getImagePaths().get(0));
        assertTrue(firstImgFile.exists() && firstImgFile.length() > 0,
                "Generated property image file missing on disk: " + firstImgFile.getPath());

        ImageRepository imgRepo = RepositoryProvider.getImageRepository();
        PropertyImage testImg = new PropertyImage("BH01", hostel.getImagePaths().get(0), "Exterior Facade", "owner");
        imgRepo.save(testImg);
        assertTrue(imgRepo.existsById(testImg.getId()), "ImageRepository failed to persist PropertyImage");
        assertFalse(imgRepo.findByAccommodationId("BH01").isEmpty(), "ImageRepository findByAccommodationId returned empty");
    }

    @Test
    @DisplayName("[TEST 28] In-App Messaging & MessageRepository (ChatEvent & Threads)")
    public void test28_InAppMessagingAndMessageRepository() {
        MessageRepository msgRepo = RepositoryProvider.getMessageRepository();
        HavenEventBus bus = HavenEventBus.getInstance();
        AtomicBoolean chatEventFired = new AtomicBoolean(false);
        HavenEventListener<ChatEvent> chatTestListener = e -> chatEventFired.set(true);
        bus.subscribe(ChatEvent.class, chatTestListener);

        try {
            ChatMessage chatMsg = new ChatMessage("student", "owner", "BH01", "Sreedeepam Boys Hostel", "ENQ-12345",
                    "Hello Warden, can I visit the hostel tomorrow at 5 PM?");
            msgRepo.save(chatMsg);
            assertTrue(chatEventFired.get(), "Saving a ChatMessage must publish a ChatEvent");
            List<ChatMessage> thread = msgRepo.findConversation("BH01", "student", "owner");
            assertFalse(thread.isEmpty(), "MessageRepository findConversation returned empty list");
        } finally {
            bus.unsubscribe(ChatEvent.class, chatTestListener);
        }
    }

    @Test
    @DisplayName("[TEST 29] Persistent Notifications & NotificationRepository")
    public void test29_PersistentNotificationsAndNotificationRepository() {
        NotificationRepository notifRepo = RepositoryProvider.getNotificationRepository();
        HavenEventBus bus = HavenEventBus.getInstance();
        AtomicBoolean notifClearedFired = new AtomicBoolean(false);
        HavenEventListener<NotificationEvent> notifListener = e -> {
            if (e.getType() == NotificationEvent.Type.CLEARED) notifClearedFired.set(true);
        };
        bus.subscribe(NotificationEvent.class, notifListener);

        try {
            Notification testNotif = new Notification("student", "Test Alert", "Your room visit is scheduled.",
                    Notification.Type.SYSTEM, "BH01");
            notifRepo.save(testNotif);
            int unreadBefore = notifRepo.countUnread("student");
            assertTrue(unreadBefore >= 1, "Unread notification count should be at least 1");

            notifRepo.markAsRead(testNotif.getId());
            Notification fetchedNotif = notifRepo.findById(testNotif.getId()).orElseThrow();
            assertTrue(fetchedNotif.isRead(), "Notification should be marked as read");

            // Test deleteById
            boolean deleted = notifRepo.deleteById(testNotif.getId());
            assertTrue(deleted, "deleteById must succeed for existing notification");
            assertFalse(notifRepo.existsById(testNotif.getId()), "Notification must no longer exist after deleteById");

            // Test clearAll
            Notification n1 = new Notification("test_user", "Alert 1", "Msg 1", Notification.Type.SYSTEM, "");
            Notification n2 = new Notification("test_user", "Alert 2", "Msg 2", Notification.Type.ENQUIRY, "");
            notifRepo.save(n1);
            notifRepo.save(n2);
            assertEquals(2, notifRepo.findByRecipient("test_user").size(), "Must have 2 notifications for test_user");

            int clearedCount = notifRepo.clearAll("test_user");
            assertEquals(2, clearedCount, "clearAll must return cleared count of 2");
            assertTrue(notifRepo.findByRecipient("test_user").isEmpty(), "test_user inbox must be empty after clearAll");
            assertTrue(notifClearedFired.get(), "NotificationEvent.Type.CLEARED must be published on clearAll/delete");
        } finally {
            bus.unsubscribe(NotificationEvent.class, notifListener);
        }
    }

    @Test
    @DisplayName("[TEST 30] Admin Role & Account Governance (RBAC & Deactivation Guard)")
    public void test30_AdminRoleAndAccountGovernance() throws Exception {
        UserRepository userRepo = RepositoryProvider.getUserRepository();
        assertTrue(userRepo.userExists("admin"), "Default admin user must exist in UserRepository");

        User adminUser = userRepo.authenticate("admin", "admin123");
        assertNotNull(adminUser, "Admin authentication returned null");
        assertTrue(adminUser.isAdmin(), "Admin user does not have admin role");

        // Salted PBKDF2 Cryptography & Pass-the-Hash Elimination
        String testPlain = "SecureStudentPass!99";
        String saltHash1 = SecurityUtils.hashPassword(testPlain);
        String saltHash2 = SecurityUtils.hashPassword(testPlain);
        assertNotEquals(saltHash1, saltHash2, "PBKDF2 hashes must use distinct random salts and cannot be equal");
        assertTrue(SecurityUtils.verifyPassword(testPlain, saltHash1), "Valid password must verify against PBKDF2 hash");
        assertFalse(SecurityUtils.verifyPassword("wrongpass", saltHash1), "Wrong password must fail");
        assertFalse(SecurityUtils.verifyPassword(saltHash1, saltHash1), "Pass-the-hash submission must be strictly rejected");

        // Service Layer instantiation
        assertNotNull(ServiceProvider.getAccommodationService(), "AccommodationService must be initialized");
        assertNotNull(ServiceProvider.getEnquiryService(), "EnquiryService must be initialized");
        assertNotNull(ServiceProvider.getAuthService(), "AuthService must be initialized");
    }
}
