package haven.repository.inmemory;

import haven.exception.AuthenticationException;
import haven.model.*;
import haven.util.PropertyImageStorage;
import haven.util.SecurityUtils;
import haven.repository.inmemory.FilePersistenceManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static haven.model.Role.*;

/**
 * InMemoryStore provides a zero-configuration in-memory fallback
 * repository when MongoDB is not running on the host system.
 * Transparently backed by 'data/haven_store.json' for offline persistence across restarts.
 */
public class InMemoryStore {
    private static volatile InMemoryStore instance;

    private final List<Accommodation> accommodations = new CopyOnWriteArrayList<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> passwords = new ConcurrentHashMap<>();
    private final Map<String, List<String>> wishlists = new ConcurrentHashMap<>();
    private final List<Enquiry> enquiries = new CopyOnWriteArrayList<>();
    private final List<Review> reviews = new CopyOnWriteArrayList<>();
    private final List<PropertyImage> images = new CopyOnWriteArrayList<>();
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    private InMemoryStore() {
        initStore();
    }

    public static InMemoryStore getInstance() {
        if (instance == null) {
            synchronized (InMemoryStore.class) {
                if (instance == null) {
                    instance = new InMemoryStore();
                }
            }
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        instance = null;
    }

    private synchronized void initStore() {
        FilePersistenceManager.PersistedData loaded = FilePersistenceManager.load();
        if (loaded != null && !loaded.accommodations.isEmpty()) {
            accommodations.clear();
            users.clear();
            passwords.clear();
            wishlists.clear();
            enquiries.clear();
            reviews.clear();
            images.clear();
            messages.clear();
            notifications.clear();

            accommodations.addAll(loaded.accommodations);
            users.putAll(loaded.users);
            passwords.putAll(loaded.passwords);
            wishlists.putAll(loaded.wishlists);
            enquiries.addAll(loaded.enquiries);
            reviews.addAll(loaded.reviews);
            images.addAll(loaded.images);
            messages.addAll(loaded.messages);
            notifications.addAll(loaded.notifications);

            // Ensure default admin account exists even if loaded from an older store file
            if (!users.containsKey("admin")) {
                Admin admin = new Admin("admin", null, "Platform Administrator", "9000000001");
                users.put("admin", admin);
                passwords.put("admin", SecurityUtils.hashPassword("admin123"));
                wishlists.put("admin", new ArrayList<>());
            }
            // Sync verified coordinates from SeedData
            Map<String, Accommodation> seedCoords = new HashMap<>();
            for (Accommodation s : SeedData.getSCTCEApprovedHostels()) {
                seedCoords.put(s.getId(), s);
            }
            for (Accommodation acc : accommodations) {
                if (seedCoords.containsKey(acc.getId())) {
                    Accommodation s = seedCoords.get(acc.getId());
                    acc.setLatitude(s.getLatitude());
                    acc.setLongitude(s.getLongitude());
                    acc.setLocationLink("https://www.google.com/maps/search/?api=1&query=" + s.getLatitude() + "," + s.getLongitude());
                }
                PropertyImageStorage.ensureDefaultImagesForAccommodation(acc);
            }
            if (images.isEmpty()) {
                seedDefaultImagesFromAccommodations();
            }
            persist();
        } else {
            seedDefaultStore();
        }
    }

    public synchronized void resetToDefaultSeed() {
        accommodations.clear();
        users.clear();
        passwords.clear();
        wishlists.clear();
        enquiries.clear();
        reviews.clear();
        images.clear();
        messages.clear();
        notifications.clear();
        seedDefaultStore();
    }

    private void seedDefaultImagesFromAccommodations() {
        for (Accommodation acc : accommodations) {
            List<String> paths = acc.getImagePaths();
            for (int i = 0; i < paths.size(); i++) {
                String p = paths.get(i);
                String caption = (i == 0) ? acc.getName() + " - Exterior View" : acc.getName() + " - Furnished Student Room";
                images.add(new PropertyImage(null, acc.getId(), p, caption, acc.getOwnerId(), System.currentTimeMillis(), i == 0));
            }
        }
    }

    private void seedDefaultStore() {
        // 1. Seed Accommodations
        accommodations.addAll(SeedData.getSCTCEApprovedHostels());
        seedDefaultImagesFromAccommodations();

        // 2. Seed Default Accounts (Student, Owner, Admin)
        String hashed = SecurityUtils.hashPassword("pass123");
        String adminHashed = SecurityUtils.hashPassword("admin123");
        Student student = new Student("student", null, "Demo Student", "9876543210", "SCTCE", "Any", 8000.0, new ArrayList<>());
        Owner owner = new Owner("owner", null, "Property Warden", "9123456780", new ArrayList<>(), new ArrayList<>());
        Admin admin = new Admin("admin", null, "Platform Administrator", "9000000001");

        users.put("student", student);
        users.put("owner", owner);
        users.put("admin", admin);
        passwords.put("student", hashed);
        passwords.put("owner", hashed);
        passwords.put("admin", adminHashed);
        wishlists.put("student", new ArrayList<>());
        wishlists.put("owner", new ArrayList<>());
        wishlists.put("admin", new ArrayList<>());

        // 3. Seed Initial Reviews
        reviews.add(new Review("BH01", "student", "Rahul Krishna (SCTCE)", 5,
                "Decent rooms, warden is helpful. Food is alright most days. Good option near campus."));
        reviews.add(new Review("BH01", "arun_m", "Arun Mohan", 4,
                "Close to college. Wifi works well."));
        reviews.add(new Review("BH02", "vivek_s", "Vivek S", 4,
                "Pretty quiet spot, good for study during exams. Curfew is strictly 9:30 though."));
        reviews.add(new Review("BPG01", "dev_k", "Devanand K", 5,
                "Good PG. Power backup works, AC is good. Bit pricier but worth it."));
        reviews.add(new Review("GH01", "anjali_r", "Anjali R (SCTCE)", 5,
                "Very safe and bus stop is right across the road. Matron is strict about curfews."));
        reviews.add(new Review("GH01", "sneha_p", "Sneha P", 4,
                "Food is okay, walking distance to SCTCE. Rooms are a bit small."));
        reviews.add(new Review("GH06", "pooja_n", "Pooja Nair", 5,
                "Affordable place, good for the price."));

        // 4. Seed Initial Enquiries
        enquiries.add(new Enquiry("ENQ-74821", "BH01", "Sreedeepam Boys Hostel", "owner",
                "student", "9876543210", "Hi, I am interested in visiting for a 2-sharing room.", "CONFIRMED",
                new Date(System.currentTimeMillis() - 86400000), "Visit welcomed anytime after 4 PM!"));

        // 5. Seed Initial Chat Messages
        messages.add(new ChatMessage("MSG-10001", "student", "owner", "BH01", "Sreedeepam Boys Hostel", "ENQ-74821",
                "Hello Warden, is the 2-sharing room on the first floor available from next month?",
                System.currentTimeMillis() - 7200000, true));
        messages.add(new ChatMessage("MSG-10002", "owner", "student", "BH01", "Sreedeepam Boys Hostel", "ENQ-74821",
                "Yes! Your booking is confirmed. You can inspect the room anytime after 4 PM today.",
                System.currentTimeMillis() - 3600000, false));

        // 6. Seed Initial Notifications
        notifications.add(new Notification("NTF-10001", "student", "Booking Confirmed!",
                "Your enquiry ENQ-74821 for Sreedeepam Boys Hostel has been CONFIRMED by the warden.",
                Notification.Type.ENQUIRY, "ENQ-74821", System.currentTimeMillis() - 3500000, false));
        notifications.add(new Notification("NTF-10002", "student", "New Message from Warden",
                "Warden replied regarding Sreedeepam Boys Hostel: 'Yes! Your booking is confirmed...'",
                Notification.Type.CHAT, "BH01", System.currentTimeMillis() - 3400000, false));
        notifications.add(new Notification("NTF-10003", "owner", "New Student Enquiry",
                "Student @student submitted a booking enquiry for Sreedeepam Boys Hostel.",
                Notification.Type.ENQUIRY, "ENQ-74821", System.currentTimeMillis() - 86400000, true));

        persist();
    }

    public synchronized void persist() {
        FilePersistenceManager.PersistedData data = new FilePersistenceManager.PersistedData();
        data.accommodations = new ArrayList<>(this.accommodations);
        data.users = new HashMap<>(this.users);
        data.passwords = new HashMap<>(this.passwords);
        data.wishlists = new HashMap<>(this.wishlists);
        data.enquiries = new ArrayList<>(this.enquiries);
        data.reviews = new ArrayList<>(this.reviews);
        data.images = new ArrayList<>(this.images);
        data.messages = new ArrayList<>(this.messages);
        data.notifications = new ArrayList<>(this.notifications);
        FilePersistenceManager.save(data);
    }

    // Accommodation operations
    public List<Accommodation> getAllAccommodations() {
        return new ArrayList<>(accommodations);
    }

    public List<Accommodation> getAccommodationsByOwner(String ownerUsername) {
        return accommodations.stream().filter(a -> ownerUsername.equalsIgnoreCase(a.getOwnerId())).collect(Collectors.toList());
    }

    /** Role-aware overload: privileged roles see all accommodations. */
    public List<Accommodation> getAccommodationsByOwner(String ownerUsername, Role role) {
        if (role != null && role.isPrivileged()) {
            return new ArrayList<>(accommodations);
        }
        return getAccommodationsByOwner(ownerUsername);
    }

    public List<Accommodation> filterAccommodations(String college, String gender, double maxRent) {
        return accommodations.stream().filter(a -> {
            if (a.isRejected()) return false;
            if (college != null && !college.equalsIgnoreCase("All") && !college.trim().isEmpty()) {
                if (!a.getNearbyCollege().toLowerCase().contains(college.toLowerCase())) return false;
            }
            if (gender != null && !gender.equalsIgnoreCase("Any")) {
                if (!a.getGenderAllowed().equalsIgnoreCase(gender) && !a.getGenderAllowed().equalsIgnoreCase("Both")) return false;
            }
            if (maxRent > 0 && a.getBaseRent() > maxRent) return false;
            return true;
        }).collect(Collectors.toList());
    }

    public Accommodation getAccommodationById(String id) {
        if (id == null) return null;
        return accommodations.stream().filter(a -> id.equalsIgnoreCase(a.getId())).findFirst().orElse(null);
    }

    public synchronized void insertAccommodation(Accommodation acc) {
        if (acc != null) {
            PropertyImageStorage.ensureDefaultImagesForAccommodation(acc);
            accommodations.add(acc);
            persist();
        }
    }

    public synchronized void updateAccommodation(Accommodation acc) {
        if (acc == null) return;
        for (int i = 0; i < accommodations.size(); i++) {
            if (accommodations.get(i).getId().equalsIgnoreCase(acc.getId())) {
                accommodations.set(i, acc);
                persist();
                return;
            }
        }
    }

    public synchronized void deleteAccommodation(String id) {
        accommodations.removeIf(a -> a.getId().equalsIgnoreCase(id));
        persist();
    }

    public synchronized void updateVacancy(String id, int newVac) {
        Accommodation a = getAccommodationById(id);
        if (a != null) {
            a.setVacancies(newVac);
            persist();
        }
    }

    public synchronized boolean decrementVacancy(String id) {
        Accommodation a = getAccommodationById(id);
        if (a != null && a.getVacancies() > 0) {
            a.setVacancies(a.getVacancies() - 1);
            persist();
            return true;
        }
        return false;
    }

    public synchronized void incrementVacancy(String id) {
        Accommodation a = getAccommodationById(id);
        if (a != null) {
            a.setVacancies(a.getVacancies() + 1);
            persist();
        }
    }

    // User & Auth operations
    public synchronized boolean userExists(String username) {
        if (username == null) return false;
        return users.containsKey(username.trim().toLowerCase());
    }

    public synchronized User getUser(String username) {
        if (username == null) return null;
        return users.get(username.trim().toLowerCase());
    }

    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public synchronized boolean deleteUser(String username) {
        if (username == null) return false;
        String key = username.trim().toLowerCase();
        if ("admin".equals(key)) return false;
        User removed = users.remove(key);
        passwords.remove(key);
        wishlists.remove(key);
        if (removed != null) {
            persist();
            return true;
        }
        return false;
    }

    public synchronized void restoreUser(User user, String passwordHash, List<String> wishlist) {
        if (user == null || user.getUsername() == null) return;
        String key = user.getUsername().trim().toLowerCase();
        users.put(key, user);
        if (passwordHash != null) {
            passwords.put(key, passwordHash);
        }
        wishlists.put(key, wishlist != null ? new ArrayList<>(wishlist) : new ArrayList<>());
        persist();
    }

    public synchronized String getPasswordHash(String username) {
        if (username == null) return null;
        return passwords.get(username.trim().toLowerCase());
    }

    public synchronized boolean setUserActive(String username, boolean active) {
        User u = getUser(username);
        if (u != null) {
            u.setActive(active);
            persist();
            return true;
        }
        return false;
    }

    public synchronized User registerUser(String username, String password, String role, String fullName, String phone,
                             String collegeName, String genderPreference, double maxBudget) throws Exception {
        String key = username.trim().toLowerCase();
        if (users.containsKey(key)) {
            throw new IllegalArgumentException("Username '" + username + "' is already registered.");
        }
        String hashed = SecurityUtils.hashPassword(password.trim());
        User u;
        if ("OWNER".equalsIgnoreCase(role)) {
            u = new Owner(username.trim(), null, fullName, phone, new ArrayList<>(), new ArrayList<>());
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            u = new Admin(username.trim(), null, fullName, phone);
        } else {
            u = new Student(username.trim(), null, fullName, phone, collegeName, genderPreference, maxBudget, new ArrayList<>());
        }
        users.put(key, u);
        passwords.put(key, hashed);
        wishlists.put(key, new ArrayList<>());
        persist();
        return u;
    }

    public synchronized User authenticate(String username, String password) throws AuthenticationException {
        String key = (username != null) ? username.trim().toLowerCase() : "";
        if (!users.containsKey(key)) {
            throw new AuthenticationException("Invalid username or password credentials.");
        }
        User u = users.get(key);
        if (u != null && !u.isActive()) {
            throw new AuthenticationException("This account has been deactivated by an administrator.");
        }
        String stored = passwords.get(key);
        if (!SecurityUtils.verifyPassword(password, stored)) {
            throw new AuthenticationException("Invalid username or password credentials.");
        }
        return u;
    }

    public synchronized void updateProfile(String username, String fullName, String phone, String college, String gender, double budget) {
        String key = username.trim().toLowerCase();
        User u = users.get(key);
        if (u != null) {
            if (fullName != null && !fullName.trim().isEmpty()) {
                u.setFullName(fullName.trim());
            }
            if (phone != null && !phone.trim().isEmpty()) {
                u.setPhone(phone.trim());
            }
            if (u instanceof Student) {
                Student s = (Student) u;
                if (college != null && !college.trim().isEmpty()) s.setCollegeName(college.trim());
                if (gender != null && !gender.trim().isEmpty()) s.setGenderPreference(gender.trim());
                if (budget > 0) s.setMaxBudget(budget);
            }
            persist();
        }
    }

    public synchronized boolean changePassword(String username, String oldPassword, String newPassword) {
        String key = username.trim().toLowerCase();
        String stored = passwords.get(key);
        if (stored == null || !SecurityUtils.verifyPassword(oldPassword, stored)) {
            return false;
        }
        String newHash = SecurityUtils.hashPassword(newPassword.trim());
        passwords.put(key, newHash);
        persist();
        return true;
    }

    public synchronized List<String> getWishlist(String username) {
        String key = (username != null) ? username.trim().toLowerCase() : "";
        return wishlists.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public synchronized void toggleWishlist(String username, String accId) {
        List<String> list = getWishlist(username);
        if (list.contains(accId)) {
            list.remove(accId);
        } else {
            list.add(accId);
        }
        persist();
    }

    // Enquiry operations
    public synchronized Enquiry saveEnquiry(Enquiry entity) {
        if (entity == null) return null;
        String eqId = (entity.getEnquiryId() != null && !entity.getEnquiryId().trim().isEmpty())
                ? entity.getEnquiryId()
                : "ENQ-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        Enquiry saved = new Enquiry(eqId, entity.getAccommodationId(), entity.getAccommodationName(),
                entity.getOwnerId(), entity.getStudentUsername(), entity.getStudentPhone(), entity.getMessage(),
                entity.getStatus() != null ? entity.getStatus() : "PENDING",
                entity.getCreatedAt() != null ? entity.getCreatedAt() : new Date(),
                entity.getOwnerResponse() != null ? entity.getOwnerResponse() : "");

        boolean updated = false;
        for (int i = 0; i < enquiries.size(); i++) {
            if (enquiries.get(i).getEnquiryId().equalsIgnoreCase(eqId)) {
                enquiries.set(i, saved);
                updated = true;
                break;
            }
        }
        if (!updated) {
            enquiries.add(0, saved);
        }
        persist();
        return saved;
    }

    public synchronized void saveEnquiry(String accId, String accName, String ownerId, String studentUsername, String studentPhone, String message) {
        saveEnquiry(new Enquiry(null, accId, accName, ownerId, studentUsername, studentPhone, message, new Date()));
    }

    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiries);
    }

    public List<Enquiry> getEnquiriesForStudent(String studentUsername) {
        return enquiries.stream().filter(e -> studentUsername.equalsIgnoreCase(e.getStudentUsername())).collect(Collectors.toList());
    }

    public List<Enquiry> getEnquiriesForOwner(String ownerUsername) {
        return enquiries.stream().filter(e -> ownerUsername.equalsIgnoreCase(e.getOwnerId())).collect(Collectors.toList());
    }

    /** Role-aware overload: privileged roles see all enquiries. */
    public List<Enquiry> getEnquiriesForOwner(String ownerUsername, Role role) {
        if (role != null && role.isPrivileged()) {
            return new ArrayList<>(enquiries);
        }
        return getEnquiriesForOwner(ownerUsername);
    }

    public synchronized boolean updateEnquiryStatus(String enquiryId, String status) {
        return updateEnquiryStatus(enquiryId, status, "");
    }

    public synchronized boolean updateEnquiryStatus(String enquiryId, String status, String ownerResponse) {
        if (enquiryId == null || status == null) return false;
        String normalizedStatus = status.trim().toUpperCase();
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry eq = enquiries.get(i);
            if (eq.getEnquiryId().equalsIgnoreCase(enquiryId.trim())) {
                if (!eq.canTransitionTo(normalizedStatus)) {
                    return false;
                }
                String resp = (ownerResponse != null && !ownerResponse.trim().isEmpty()) ? ownerResponse : eq.getOwnerResponse();
                enquiries.set(i, new Enquiry(eq.getEnquiryId(), eq.getAccommodationId(), eq.getAccommodationName(),
                        eq.getOwnerId(), eq.getStudentUsername(), eq.getStudentPhone(), eq.getMessage(),
                        normalizedStatus, eq.getCreatedAt(), resp));
                persist();
                return true;
            }
        }
        return false;
    }

    public synchronized void deleteEnquiry(String enquiryId) {
        enquiries.removeIf(e -> e.getEnquiryId().equalsIgnoreCase(enquiryId));
        persist();
    }

    // Review operations
    public synchronized void addReview(Review review) {
        if (review != null) {
            reviews.removeIf(r -> r.getId() != null && r.getId().equalsIgnoreCase(review.getId()));
            reviews.add(0, review);
            persist();
        }
    }

    public synchronized boolean deleteReview(String reviewId) {
        if (reviewId == null) return false;
        boolean removed = reviews.removeIf(r -> r.getId() != null && r.getId().equalsIgnoreCase(reviewId));
        if (removed) persist();
        return removed;
    }

    public Optional<Review> getReviewById(String reviewId) {
        if (reviewId == null) return Optional.empty();
        return reviews.stream().filter(r -> r.getId() != null && r.getId().equalsIgnoreCase(reviewId)).findFirst();
    }

    public List<Review> getAllReviews() {
        return new ArrayList<>(reviews);
    }

    public List<Review> getReviewsForAccommodation(String accId) {
        return reviews.stream().filter(r -> accId.equalsIgnoreCase(r.getAccommodationId())).collect(Collectors.toList());
    }

    public double getAverageRating(String accId) {
        List<Review> list = getReviewsForAccommodation(accId);
        if (list.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Review r : list) sum += r.getRating();
        return sum / list.size();
    }

    public int getReviewCount(String accId) {
        return getReviewsForAccommodation(accId).size();
    }

    public boolean hasUserReviewed(String accId, String username) {
        if (accId == null || username == null) return false;
        return reviews.stream().anyMatch(r -> accId.equalsIgnoreCase(r.getAccommodationId()) && username.equalsIgnoreCase(r.getUsername()));
    }

    // Property Image operations
    public synchronized PropertyImage saveImage(PropertyImage img) {
        if (img == null) return null;
        images.removeIf(i -> i.getId().equalsIgnoreCase(img.getId()));
        images.add(img);
        Accommodation acc = getAccommodationById(img.getAccommodationId());
        if (acc != null) {
            acc.addImagePath(img.getFilePath());
        }
        persist();
        return img;
    }

    public List<PropertyImage> getAllImages() {
        return new ArrayList<>(images);
    }

    public List<PropertyImage> getImagesByAccommodation(String accId) {
        if (accId == null) return new ArrayList<>();
        return images.stream().filter(i -> accId.equalsIgnoreCase(i.getAccommodationId())).collect(Collectors.toList());
    }

    public synchronized boolean deleteImage(String imageId) {
        if (imageId == null) return false;
        boolean removed = images.removeIf(i -> imageId.equalsIgnoreCase(i.getId()));
        if (removed) persist();
        return removed;
    }

    // Chat Message operations
    public synchronized ChatMessage saveMessage(ChatMessage msg) {
        if (msg == null) return null;
        messages.removeIf(m -> m.getId().equalsIgnoreCase(msg.getId()));
        messages.add(msg);
        persist();
        return msg;
    }

    public List<ChatMessage> getAllMessages() {
        return new ArrayList<>(messages);
    }

    public List<ChatMessage> getMessagesByEnquiryId(String enquiryId) {
        if (enquiryId == null || enquiryId.trim().isEmpty()) return new ArrayList<>();
        return messages.stream()
                .filter(m -> enquiryId.equalsIgnoreCase(m.getEnquiryId()))
                .sorted(Comparator.comparingLong(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<ChatMessage> getConversation(String accommodationId, String userA, String userB) {
        return getConversation(accommodationId, userA, userB, null);
    }

    /** Role-aware overload: privileged callers see all messages for an accommodation. */
    public List<ChatMessage> getConversation(String accommodationId, String userA, String userB, Role callerRole) {
        return messages.stream()
                .filter(m -> {
                    if (accommodationId != null && !accommodationId.isEmpty() && !accommodationId.equalsIgnoreCase(m.getAccommodationId())) {
                        return false;
                    }
                    if (userA == null || userB == null || userA.isEmpty() || userB.isEmpty()) return true;
                    boolean ab = userA.equalsIgnoreCase(m.getSenderUsername()) && userB.equalsIgnoreCase(m.getReceiverUsername());
                    boolean ba = userB.equalsIgnoreCase(m.getSenderUsername()) && userA.equalsIgnoreCase(m.getReceiverUsername());
                    // Privileged role alias matching: owner/admin can see messages sent to any property ownerId
                    if (!ab && !ba && callerRole != null && callerRole.isPrivileged()) {
                        String otherParty = userA.equalsIgnoreCase(m.getSenderUsername()) ? userA : userB;
                        String studentParty = otherParty.equals(userA) ? userB : userA;
                        return studentParty.equalsIgnoreCase(m.getSenderUsername()) || studentParty.equalsIgnoreCase(m.getReceiverUsername());
                    }
                    return ab || ba;
                })
                .sorted(Comparator.comparingLong(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<ChatMessage> getMessagesByParticipant(String username) {
        return getMessagesByParticipant(username, null);
    }

    /** Role-aware overload: privileged roles see all messages. */
    public List<ChatMessage> getMessagesByParticipant(String username, Role callerRole) {
        if (username == null) return new ArrayList<>();
        return messages.stream()
                .filter(m -> username.equalsIgnoreCase(m.getSenderUsername())
                        || username.equalsIgnoreCase(m.getReceiverUsername())
                        || (callerRole != null && callerRole.isPrivileged()))
                .sorted(Comparator.comparingLong(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    public synchronized int markConversationAsRead(String accommodationId, String senderUsername, String receiverUsername) {
        int count = 0;
        for (ChatMessage m : messages) {
            if (!m.isRead() && (accommodationId == null || accommodationId.isEmpty() || accommodationId.equalsIgnoreCase(m.getAccommodationId()))) {
                if (receiverUsername != null && receiverUsername.equalsIgnoreCase(m.getReceiverUsername())) {
                    m.setRead(true);
                    count++;
                }
            }
        }
        if (count > 0) persist();
        return count;
    }

    // Notification operations
    public synchronized Notification saveNotification(Notification n) {
        if (n == null) return null;
        notifications.removeIf(existing -> existing.getId().equalsIgnoreCase(n.getId()));
        notifications.add(0, n);
        persist();
        return n;
    }

    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    public List<Notification> getNotificationsForUser(String username) {
        return getNotificationsForUser(username, null);
    }

    /** Role-aware overload: privileged roles see owner-targeted notifications too. */
    public List<Notification> getNotificationsForUser(String username, Role callerRole) {
        if (username == null) return new ArrayList<>();
        return notifications.stream()
                .filter(n -> username.equalsIgnoreCase(n.getRecipientUsername())
                        || (callerRole != null && callerRole.isPrivileged()
                            && !STUDENT.matches(n.getRecipientUsername())
                            && !ADMIN.matches(n.getRecipientUsername())))
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }

    public synchronized boolean markNotificationRead(String notificationId) {
        for (Notification n : notifications) {
            if (n.getId().equalsIgnoreCase(notificationId)) {
                n.setRead(true);
                persist();
                return true;
            }
        }
        return false;
    }

    public synchronized void markAllNotificationsRead(String username) {
        boolean changed = false;
        for (Notification n : getNotificationsForUser(username)) {
            if (!n.isRead()) {
                n.setRead(true);
                changed = true;
            }
        }
        if (changed) persist();
    }

    public synchronized boolean deleteNotification(String id) {
        boolean removed = notifications.removeIf(n -> n.getId().equalsIgnoreCase(id));
        if (removed) persist();
        return removed;
    }

    public synchronized int clearNotificationsForUser(String username) {
        if (username == null) return 0;
        List<Notification> userNotifs = getNotificationsForUser(username);
        int count = userNotifs.size();
        if (count > 0) {
            notifications.removeAll(userNotifs);
            persist();
        }
        return count;
    }
}
