package haven.repository.inmemory;

import haven.model.*;
import haven.pattern.PropertyImageStorage;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles transparent offline persistence to 'data/haven_store.json'.
 * Uses BSON Document JSON parsing/generation from the MongoDB driver,
 * providing zero-dependency local JSON file persistence across all 7 domain entities.
 */
public class FilePersistenceManager {
    private static final Logger LOGGER = Logger.getLogger(FilePersistenceManager.class.getName());
    public static final String DEFAULT_STORE_PATH = "data/haven_store.json";
    private static volatile String storePath = DEFAULT_STORE_PATH;

    public static void setStorePath(String path) {
        storePath = (path != null) ? path : DEFAULT_STORE_PATH;
    }

    public static void resetStorePath() {
        storePath = DEFAULT_STORE_PATH;
    }

    public static String getStorePath() {
        return storePath;
    }

    public static class PersistedData {
        public List<Accommodation> accommodations = new ArrayList<>();
        public Map<String, User> users = new HashMap<>();
        public Map<String, String> passwords = new HashMap<>();
        public Map<String, List<String>> wishlists = new HashMap<>();
        public List<Enquiry> enquiries = new ArrayList<>();
        public List<Review> reviews = new ArrayList<>();
        public List<PropertyImage> images = new ArrayList<>();
        public List<ChatMessage> messages = new ArrayList<>();
        public List<Notification> notifications = new ArrayList<>();
    }

    public static boolean storeFileExists() {
        return new File(getStorePath()).exists();
    }

    public static synchronized void save(PersistedData data) {
        try {
            File targetFile = new File(getStorePath());
            File dir = targetFile.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }

            Document root = new Document();

            // 1. Accommodations
            List<Document> accDocs = new ArrayList<>();
            for (Accommodation a : data.accommodations) {
                Document doc = new Document("id", a.getId())
                        .append("ownerId", a.getOwnerId())
                        .append("name", a.getName())
                        .append("nearbyCollege", a.getNearbyCollege())
                        .append("baseRent", a.getBaseRent())
                        .append("distanceKm", a.getDistanceKm())
                        .append("genderAllowed", a.getGenderAllowed())
                        .append("vacancies", a.getVacancies())
                        .append("locationLink", a.getLocationLink())
                        .append("latitude", a.getLatitude())
                        .append("longitude", a.getLongitude())
                        .append("address", a.getAddress())
                        .append("contactPhone", a.getContactPhone())
                        .append("amenities", a.getAmenities())
                        .append("imagePaths", a.getImagePaths())
                        .append("moderationStatus", a.getModerationStatus())
                        .append("featured", a.isFeatured());

                String typeStr = a.getType();
                doc.append("type", typeStr.toUpperCase());
                if ("Hostel".equalsIgnoreCase(typeStr) && a instanceof Hostel) {
                    Hostel h = (Hostel) a;
                    doc.append("fixedMessFee", h.getFixedMessFee())
                       .append("curfewTime", h.getCurfewTime());
                } else if ("PG".equalsIgnoreCase(typeStr) && a instanceof PG) {
                    PG pg = (PG) a;
                    doc.append("foodIncluded", pg.isFoodIncluded())
                       .append("utilityCharges", pg.getUtilityCharges());
                }
                accDocs.add(doc);
            }
            root.append("accommodations", accDocs);

            // 2. Users
            List<Document> userDocs = new ArrayList<>();
            for (User u : data.users.values()) {
                Document uDoc = new Document("username", u.getUsername())
                        .append("fullName", u.getFullName())
                        .append("phone", u.getPhone())
                        .append("role", u.getRole())
                        .append("active", u.isActive());

                String pass = data.passwords.get(u.getUsername().toLowerCase());
                if (pass != null) {
                    uDoc.append("passwordHash", pass);
                }

                if (u instanceof Student) {
                    Student s = (Student) u;
                    uDoc.append("collegeName", s.getCollegeName())
                        .append("genderPreference", s.getGenderPreference())
                        .append("maxBudget", s.getMaxBudget());
                } else if (u instanceof Admin) {
                    Admin adm = (Admin) u;
                    uDoc.append("department", adm.getDepartment())
                        .append("accessLevel", adm.getAccessLevel());
                } else if (u instanceof Owner) {
                    Owner o = (Owner) u;
                    uDoc.append("propertyIds", o.getPropertyIds());
                }
                userDocs.add(uDoc);
            }
            root.append("users", userDocs);

            // 3. Wishlists
            Document wishDoc = new Document();
            for (Map.Entry<String, List<String>> entry : data.wishlists.entrySet()) {
                wishDoc.append(entry.getKey(), entry.getValue());
            }
            root.append("wishlists", wishDoc);

            // 4. Enquiries
            List<Document> eqDocs = new ArrayList<>();
            for (Enquiry eq : data.enquiries) {
                Document eqDoc = new Document("enquiryId", eq.getEnquiryId())
                        .append("accommodationId", eq.getAccommodationId())
                        .append("accommodationName", eq.getAccommodationName())
                        .append("ownerId", eq.getOwnerId())
                        .append("studentUsername", eq.getStudentUsername())
                        .append("studentPhone", eq.getStudentPhone())
                        .append("message", eq.getMessage())
                        .append("status", eq.getStatus())
                        .append("createdAt", eq.getCreatedAt() != null ? eq.getCreatedAt().getTime() : System.currentTimeMillis())
                        .append("ownerResponse", eq.getOwnerResponse());
                eqDocs.add(eqDoc);
            }
            root.append("enquiries", eqDocs);

            // 5. Reviews
            List<Document> revDocs = new ArrayList<>();
            for (Review r : data.reviews) {
                Document revDoc = new Document("id", r.getId())
                        .append("accommodationId", r.getAccommodationId())
                        .append("username", r.getUsername())
                        .append("authorName", r.getAuthorName())
                        .append("rating", r.getRating())
                        .append("comment", r.getComment())
                        .append("flagged", r.isFlagged())
                        .append("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().getTime() : System.currentTimeMillis());
                revDocs.add(revDoc);
            }
            root.append("reviews", revDocs);

            // 6. Property Images
            List<Document> imgDocs = new ArrayList<>();
            for (PropertyImage img : data.images) {
                Document imgDoc = new Document("id", img.getId())
                        .append("accommodationId", img.getAccommodationId())
                        .append("filePath", img.getFilePath())
                        .append("caption", img.getCaption())
                        .append("uploadedBy", img.getUploadedBy())
                        .append("uploadedAt", img.getUploadedAt())
                        .append("primary", img.isPrimary());
                imgDocs.add(imgDoc);
            }
            root.append("images", imgDocs);

            // 7. Chat Messages
            List<Document> msgDocs = new ArrayList<>();
            for (ChatMessage m : data.messages) {
                Document msgDoc = new Document("id", m.getId())
                        .append("senderUsername", m.getSenderUsername())
                        .append("receiverUsername", m.getReceiverUsername())
                        .append("accommodationId", m.getAccommodationId())
                        .append("accommodationName", m.getAccommodationName())
                        .append("enquiryId", m.getEnquiryId())
                        .append("content", m.getContent())
                        .append("timestamp", m.getTimestamp())
                        .append("read", m.isRead());
                msgDocs.add(msgDoc);
            }
            root.append("messages", msgDocs);

            // 8. Notifications
            List<Document> ntfDocs = new ArrayList<>();
            for (Notification n : data.notifications) {
                Document ntfDoc = new Document("id", n.getId())
                        .append("recipientUsername", n.getRecipientUsername())
                        .append("title", n.getTitle())
                        .append("message", n.getMessage())
                        .append("type", n.getType().name())
                        .append("linkedEntityId", n.getLinkedEntityId())
                        .append("timestamp", n.getTimestamp())
                        .append("read", n.isRead());
                ntfDocs.add(ntfDoc);
            }
            root.append("notifications", ntfDocs);

            String json = root.toJson();
            Path targetPath = Paths.get(getStorePath());
            Path parentDir = targetPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Path tempPath = (parentDir != null)
                    ? parentDir.resolve("haven_store.tmp")
                    : Paths.get("haven_store.tmp");
            Files.writeString(tempPath, json, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tempPath, targetPath, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                Files.move(tempPath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error saving state to " + getStorePath(), e);
        }
    }

    public static synchronized PersistedData load() {
        File file = new File(getStorePath());
        if (!file.exists()) {
            return null;
        }

        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            if (json == null || json.trim().isEmpty()) {
                return null;
            }

            Document root = Document.parse(json);
            PersistedData data = new PersistedData();

            // 1. Accommodations
            List<Document> accDocs = root.getList("accommodations", Document.class);
            if (accDocs != null) {
                for (Document doc : accDocs) {
                    String type = doc.getString("type");
                    String address = doc.getString("address") != null ? doc.getString("address") : "";
                    String contactPhone = doc.getString("contactPhone") != null ? doc.getString("contactPhone") : "";
                    String locationLink = doc.getString("locationLink");
                    double lat = getDoubleSafe(doc, "latitude", 0.0);
                    double lon = getDoubleSafe(doc, "longitude", 0.0);

                    Accommodation acc;
                    boolean isHostel = type == null || "HOSTEL".equalsIgnoreCase(type.trim());
                    if (isHostel) {
                        acc = new Hostel(
                                doc.getString("id"),
                                doc.getString("ownerId"),
                                doc.getString("name"),
                                doc.getString("nearbyCollege"),
                                getDoubleSafe(doc, "baseRent", 4500.0),
                                getDoubleSafe(doc, "distanceKm", 1.0),
                                doc.getString("genderAllowed"),
                                doc.getInteger("vacancies", 0),
                                locationLink,
                                lat,
                                lon,
                                getDoubleSafe(doc, "fixedMessFee", 2500.0),
                                doc.getString("curfewTime") != null ? doc.getString("curfewTime") : "9:30 PM",
                                address,
                                contactPhone
                        );
                    } else {
                        acc = new PG(
                                doc.getString("id"),
                                doc.getString("ownerId"),
                                doc.getString("name"),
                                doc.getString("nearbyCollege"),
                                getDoubleSafe(doc, "baseRent", 4500.0),
                                getDoubleSafe(doc, "distanceKm", 1.0),
                                doc.getString("genderAllowed"),
                                doc.getInteger("vacancies", 0),
                                locationLink,
                                lat,
                                lon,
                                Boolean.TRUE.equals(doc.getBoolean("foodIncluded")),
                                getDoubleSafe(doc, "utilityCharges", 500.0),
                                address,
                                contactPhone
                        );
                    }

                    List<String> amenities = doc.getList("amenities", String.class);
                    if (amenities != null && !amenities.isEmpty()) {
                        for (String am : amenities) acc.addAmenity(am);
                    } else {
                        acc.addAmenity("WiFi");
                        if (acc instanceof Hostel || ((PG) acc).isFoodIncluded()) acc.addAmenity("Food / Mess");
                        acc.addAmenity("Attached Washroom");
                        acc.addAmenity("Power Backup");
                        if (acc instanceof Hostel) acc.addAmenity("CCTV Security");
                    }

                    List<String> imgPaths = doc.getList("imagePaths", String.class);
                    if (imgPaths != null && !imgPaths.isEmpty()) {
                        acc.setImagePaths(imgPaths);
                    } else {
                        PropertyImageStorage.ensureDefaultImagesForAccommodation(acc);
                    }

                    String modStatus = doc.getString("moderationStatus");
                    if (modStatus != null) acc.setModerationStatus(modStatus);
                    Boolean feat = doc.getBoolean("featured");
                    if (feat != null) acc.setFeatured(feat);

                    data.accommodations.add(acc);
                }
            }

            // 2. Users
            List<Document> userDocs = root.getList("users", Document.class);
            if (userDocs != null) {
                for (Document uDoc : userDocs) {
                    String username = uDoc.getString("username");
                    String role = uDoc.getString("role");
                    String fullName = uDoc.getString("fullName");
                    String phone = uDoc.getString("phone");
                    String passHash = uDoc.getString("passwordHash");
                    boolean active = uDoc.getBoolean("active", true);

                    User u;
                    if ("OWNER".equalsIgnoreCase(role)) {
                        List<String> propertyIds = uDoc.getList("propertyIds", String.class);
                        u = new Owner(username, null, fullName, phone, propertyIds != null ? propertyIds : new ArrayList<>(), new ArrayList<>());
                    } else if ("ADMIN".equalsIgnoreCase(role)) {
                        u = new Admin(username, null, fullName, phone,
                                uDoc.getString("department"), uDoc.getString("accessLevel"), new ArrayList<>());
                    } else {
                        String college = uDoc.getString("collegeName");
                        String gender = uDoc.getString("genderPreference");
                        Double budget = uDoc.getDouble("maxBudget");
                        u = new Student(username, null, fullName, phone, college != null ? college : "SCTCE",
                                gender != null ? gender : "Any", budget != null ? budget : 8000.0, new ArrayList<>());
                    }
                    u.setActive(active);
                    data.users.put(username.toLowerCase(), u);
                    if (passHash != null) {
                        data.passwords.put(username.toLowerCase(), passHash);
                    }
                }
            }

            // 3. Wishlists
            Document wishDoc = (Document) root.get("wishlists");
            if (wishDoc != null) {
                for (String key : wishDoc.keySet()) {
                    List<String> list = wishDoc.getList(key, String.class);
                    data.wishlists.put(key.toLowerCase(), list != null ? new ArrayList<>(list) : new ArrayList<>());
                }
            }

            // 4. Enquiries
            List<Document> eqDocs = root.getList("enquiries", Document.class);
            if (eqDocs != null) {
                for (Document eqDoc : eqDocs) {
                    Long time = eqDoc.getLong("createdAt");
                    Date d = (time != null) ? new Date(time) : new Date();
                    Enquiry eq = new Enquiry(
                            eqDoc.getString("enquiryId"),
                            eqDoc.getString("accommodationId"),
                            eqDoc.getString("accommodationName"),
                            eqDoc.getString("ownerId"),
                            eqDoc.getString("studentUsername"),
                            eqDoc.getString("studentPhone"),
                            eqDoc.getString("message"),
                            eqDoc.getString("status"),
                            d,
                            eqDoc.getString("ownerResponse")
                    );
                    data.enquiries.add(eq);
                }
            }

            // 5. Reviews
            List<Document> revDocs = root.getList("reviews", Document.class);
            if (revDocs != null) {
                for (Document revDoc : revDocs) {
                    Long time = revDoc.getLong("createdAt");
                    Review r = new Review(
                            revDoc.getString("id"),
                            revDoc.getString("accommodationId"),
                            revDoc.getString("username"),
                            revDoc.getString("authorName"),
                            revDoc.getInteger("rating", 5),
                            revDoc.getString("comment"),
                            (time != null) ? time : System.currentTimeMillis()
                    );
                    r.setFlagged(revDoc.getBoolean("flagged", false));
                    data.reviews.add(r);
                }
            }

            // 6. Property Images
            List<Document> imgDocs = root.getList("images", Document.class);
            if (imgDocs != null) {
                for (Document imgDoc : imgDocs) {
                    Long ts = imgDoc.getLong("uploadedAt");
                    data.images.add(new PropertyImage(
                            imgDoc.getString("id"),
                            imgDoc.getString("accommodationId"),
                            imgDoc.getString("filePath"),
                            imgDoc.getString("caption"),
                            imgDoc.getString("uploadedBy"),
                            ts != null ? ts : System.currentTimeMillis(),
                            imgDoc.getBoolean("primary", false)
                    ));
                }
            }

            // 7. Chat Messages
            List<Document> msgDocs = root.getList("messages", Document.class);
            if (msgDocs != null) {
                for (Document msgDoc : msgDocs) {
                    Long ts = msgDoc.getLong("timestamp");
                    String content = msgDoc.getString("content");
                    if (content != null && !content.trim().isEmpty()) {
                        data.messages.add(new ChatMessage(
                                msgDoc.getString("id"),
                                msgDoc.getString("senderUsername"),
                                msgDoc.getString("receiverUsername"),
                                msgDoc.getString("accommodationId"),
                                msgDoc.getString("accommodationName"),
                                msgDoc.getString("enquiryId"),
                                content,
                                ts != null ? ts : System.currentTimeMillis(),
                                msgDoc.getBoolean("read", false)
                        ));
                    }
                }
            }

            // 8. Notifications
            List<Document> ntfDocs = root.getList("notifications", Document.class);
            if (ntfDocs != null) {
                for (Document ntfDoc : ntfDocs) {
                    Long ts = ntfDoc.getLong("timestamp");
                    Notification.Type type = Notification.Type.SYSTEM;
                    try {
                        String tStr = ntfDoc.getString("type");
                        if (tStr != null) type = Notification.Type.valueOf(tStr);
                    } catch (IllegalArgumentException e) {
                        LOGGER.log(Level.FINE, "Unknown notification type in stored data, defaulting to SYSTEM", e);
                    }
                    data.notifications.add(new Notification(
                            ntfDoc.getString("id"),
                            ntfDoc.getString("recipientUsername"),
                            ntfDoc.getString("title"),
                            ntfDoc.getString("message"),
                            type,
                            ntfDoc.getString("linkedEntityId"),
                            ts != null ? ts : System.currentTimeMillis(),
                            ntfDoc.getBoolean("read", false)
                    ));
                }
            }

            return data;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load " + getStorePath() + ", falling back to seed", e);
            return null;
        }
    }

    private static double getDoubleSafe(Document doc, String key, double defaultValue) {
        if (doc == null) return defaultValue;
        Object val = doc.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else if (val instanceof String) {
            try {
                return Double.parseDouble(((String) val).trim());
            } catch (NumberFormatException ignored) {
                LOGGER.log(Level.FINE, "Failed to parse JSON double for key: " + key, ignored);
            }
        }
        return defaultValue;
    }
}
