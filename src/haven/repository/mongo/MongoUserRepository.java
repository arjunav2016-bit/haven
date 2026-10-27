package haven.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import haven.exception.AuthenticationException;
import haven.model.Admin;
import haven.model.Owner;
import haven.model.Student;
import haven.model.User;
import haven.repository.mongo.MongoDBConnection;
import haven.util.SecurityUtils;
import haven.repository.UserRepository;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB concrete implementation of UserRepository.
 */
public class MongoUserRepository implements UserRepository {
    private final MongoCollection<Document> collection;

    public MongoUserRepository() {
        this.collection = MongoDBConnection.getInstance().getDatabase().getCollection("users");
        // Only seed demo/test accounts when explicitly enabled via -Dhaven.seed=true
        if (Boolean.getBoolean("haven.seed")) {
            seedDefaultAccountsIfMissing();
        }
    }

    private void seedDefaultAccountsIfMissing() {
        if (!userExists("student")) {
            registerStudent("student", "pass123", "Demo Student", "9876543210", "SCTCE", "Any", 8000.0);
        }
        if (!userExists("owner")) {
            registerOwner("owner", "pass123", "Property Warden", "9123456780");
        }
        if (!userExists("admin")) {
            Document doc = new Document("username", "admin")
                    .append("password", SecurityUtils.hashPassword("admin123"))
                    .append("role", "ADMIN")
                    .append("fullName", "Platform Administrator")
                    .append("phone", "9000000001")
                    .append("department", "Platform Governance")
                    .append("accessLevel", "SUPER_ADMIN")
                    .append("active", true)
                    .append("wishlist", new ArrayList<String>());
            collection.insertOne(doc);
        }
    }

    @Override
    public User save(User entity) {
        if (entity == null) return null;
        updateUserProfile(entity);
        return entity;
    }

    @Override
    public Optional<User> findById(String username) {
        if (username == null) return Optional.empty();
        Document doc = collection.find(Filters.eq("username", username.trim())).first();
        return (doc != null) ? Optional.of(mapDocumentToUser(doc)) : Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        for (Document doc : collection.find()) {
            list.add(mapDocumentToUser(doc));
        }
        return list;
    }

    @Override
    public boolean deleteById(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return collection.deleteOne(Filters.eq("username", username.trim())).getDeletedCount() > 0;
    }

    @Override
    public long count() {
        return collection.countDocuments();
    }

    @Override
    public boolean existsById(String username) {
        return userExists(username);
    }

    @Override
    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return collection.countDocuments(Filters.eq("username", username.trim())) > 0;
    }

    @Override
    public User authenticate(String username, String rawPassword) throws AuthenticationException {
        if (username == null || rawPassword == null) {
            throw new AuthenticationException("Username and password are required.");
        }

        Document doc = collection.find(Filters.eq("username", username.trim())).first();
        if (doc == null) {
            throw new AuthenticationException("Invalid username or password credentials.");
        }

        Boolean active = doc.getBoolean("active");
        if (active != null && !active) {
            throw new AuthenticationException("Account '" + username.trim() + "' has been deactivated by an Administrator.");
        }

        String storedPassword = doc.getString("password");
        if (!SecurityUtils.verifyPassword(rawPassword, storedPassword)) {
            throw new AuthenticationException("Invalid username or password credentials.");
        }

        return mapDocumentToUser(doc);
    }

    @Override
    public void registerStudent(String username, String rawPassword, String fullName, String phone,
                                String collegeName, String genderPreference, double maxBudget) {
        if (userExists(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already registered.");
        }
        String hashedPassword = SecurityUtils.hashPassword(rawPassword.trim());
        Document doc = new Document("username", username.trim())
                .append("password", hashedPassword)
                .append("role", "STUDENT")
                .append("fullName", fullName != null ? fullName.trim() : "")
                .append("phone", phone != null ? phone.trim() : "")
                .append("collegeName", collegeName != null ? collegeName.trim() : "SCTCE")
                .append("genderPreference", genderPreference != null ? genderPreference.trim() : "Any")
                .append("maxBudget", maxBudget > 0 ? maxBudget : 8000.0)
                .append("active", true)
                .append("wishlist", new ArrayList<String>());
        collection.insertOne(doc);
    }

    @Override
    public void registerOwner(String username, String rawPassword, String fullName, String phone) {
        if (userExists(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already registered.");
        }
        String hashedPassword = SecurityUtils.hashPassword(rawPassword.trim());
        Document doc = new Document("username", username.trim())
                .append("password", hashedPassword)
                .append("role", "OWNER")
                .append("fullName", fullName != null ? fullName.trim() : "")
                .append("phone", phone != null ? phone.trim() : "")
                .append("propertyIds", new ArrayList<String>())
                .append("active", true)
                .append("wishlist", new ArrayList<String>());
        collection.insertOne(doc);
    }

    @Override
    public boolean updateUserProfile(User user) {
        if (user == null || user.getUsername() == null) return false;
        List<Bson> updates = new ArrayList<>();
        if (user.getFullName() != null) updates.add(Updates.set("fullName", user.getFullName().trim()));
        if (user.getPhone() != null) updates.add(Updates.set("phone", user.getPhone().trim()));
        updates.add(Updates.set("active", user.isActive()));

        if (user instanceof Student) {
            Student s = (Student) user;
            if (s.getCollegeName() != null) updates.add(Updates.set("collegeName", s.getCollegeName().trim()));
            if (s.getGenderPreference() != null) updates.add(Updates.set("genderPreference", s.getGenderPreference().trim()));
            if (s.getMaxBudget() > 0) updates.add(Updates.set("maxBudget", s.getMaxBudget()));
        }

        if (!updates.isEmpty()) {
            collection.updateOne(Filters.eq("username", user.getUsername().trim()), Updates.combine(updates));
            return true;
        }
        return false;
    }

    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) throws AuthenticationException {
        if (username == null || currentPassword == null || newPassword == null) return false;
        Document doc = collection.find(Filters.eq("username", username.trim())).first();
        if (doc == null) throw new AuthenticationException("User not found.");

        String stored = doc.getString("password");
        if (!SecurityUtils.verifyPassword(currentPassword, stored)) {
            throw new AuthenticationException("Current password verification failed.");
        }

        String newHashed = SecurityUtils.hashPassword(newPassword.trim());
        collection.updateOne(Filters.eq("username", username.trim()), Updates.set("password", newHashed));
        return true;
    }

    @Override
    public List<String> getWishlist(String username) {
        if (username == null) return new ArrayList<>();
        Document doc = collection.find(Filters.eq("username", username.trim())).first();
        if (doc != null) {
            List<String> list = doc.getList("wishlist", String.class);
            return (list != null) ? list : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean toggleWishlist(String username, String accommodationId) {
        if (username == null || accommodationId == null) return false;
        Document userDoc = collection.find(Filters.eq("username", username.trim())).first();
        if (userDoc != null) {
            List<String> wishlist = userDoc.getList("wishlist", String.class);
            if (wishlist == null) wishlist = new ArrayList<>();

            if (wishlist.contains(accommodationId)) {
                collection.updateOne(Filters.eq("username", username.trim()), Updates.pull("wishlist", accommodationId));
                return false;
            } else {
                collection.updateOne(Filters.eq("username", username.trim()), Updates.addToSet("wishlist", accommodationId));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setUserActive(String username, boolean active) {
        if (username == null) return false;
        return collection.updateOne(Filters.eq("username", username.trim()), Updates.set("active", active)).getMatchedCount() > 0;
    }

    @Override
    public void restoreUser(User user, String passwordHash, List<String> wishlist) {
        if (user == null || user.getUsername() == null) return;
        collection.deleteOne(Filters.eq("username", user.getUsername().trim()));
        Document doc = new Document("username", user.getUsername().trim())
                .append("password", passwordHash != null ? passwordHash : SecurityUtils.hashPassword("student123"))
                .append("role", user.getRole() != null ? user.getRole() : "STUDENT")
                .append("fullName", user.getFullName() != null ? user.getFullName() : "")
                .append("phone", user.getPhone() != null ? user.getPhone() : "")
                .append("active", user.isActive())
                .append("wishlist", wishlist != null ? wishlist : (user.getWishlist() != null ? user.getWishlist() : new ArrayList<String>()));

        if (user instanceof Student) {
            Student s = (Student) user;
            doc.append("collegeName", s.getCollegeName() != null ? s.getCollegeName() : "SCTCE")
               .append("genderPreference", s.getGenderPreference() != null ? s.getGenderPreference() : "Any")
               .append("maxBudget", s.getMaxBudget());
        } else if (user instanceof Owner) {
            Owner o = (Owner) user;
            doc.append("propertyIds", o.getPropertyIds() != null ? o.getPropertyIds() : new ArrayList<String>());
        } else if (user instanceof Admin) {
            Admin a = (Admin) user;
            doc.append("department", a.getDepartment())
               .append("accessLevel", a.getAccessLevel());
        }
        collection.insertOne(doc);
    }

    @Override
    public String getPasswordHash(String username) {
        if (username == null) return null;
        Document doc = collection.find(Filters.eq("username", username.trim())).first();
        return doc != null ? doc.getString("password") : null;
    }

    private User mapDocumentToUser(Document doc) {
        List<String> wishlist = doc.getList("wishlist", String.class);
        if (wishlist == null) wishlist = new ArrayList<>();
        String username = doc.getString("username");
        String role = doc.getString("role");
        String fullName = doc.getString("fullName");
        String phone = doc.getString("phone");
        boolean active = doc.getBoolean("active", true);

        User user;
        if ("STUDENT".equalsIgnoreCase(role)) {
            String college = doc.getString("collegeName");
            String genderPref = doc.getString("genderPreference");
            Double budget = doc.getDouble("maxBudget");
            user = new Student(username, null, fullName, phone,
                    college != null ? college : "SCTCE",
                    genderPref != null ? genderPref : "Any",
                    budget != null ? budget : 8000.0,
                    wishlist);
        } else if ("OWNER".equalsIgnoreCase(role)) {
            List<String> propertyIds = doc.getList("propertyIds", String.class);
            user = new Owner(username, null, fullName, phone,
                    propertyIds != null ? propertyIds : new ArrayList<>(), wishlist);
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            String department = doc.getString("department");
            String accessLevel = doc.getString("accessLevel");
            user = new Admin(username, null, fullName, phone, department, accessLevel, wishlist);
        } else {
            user = new Student(username, null, fullName, phone, wishlist);
        }
        user.setActive(active);
        return user;
    }
}
