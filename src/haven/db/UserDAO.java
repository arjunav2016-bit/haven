
package haven.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import haven.model.User;
import org.bson.Document;

public class UserDAO {

    private final MongoCollection<Document> collection;

    public UserDAO() {
        this.collection = MongoDBConnection.getInstance()
                .getDatabase()
                .getCollection("users");
    }

    // Check whether an email already exists
    public boolean emailExists(String email) {
        return collection.find(
                Filters.eq("email", email)
        ).first() != null;
    }

    // Check whether a username already exists
    public boolean usernameExists(String username) {
        return collection.find(
                Filters.eq("username", username)
        ).first() != null;
    }

    // Save a user in MongoDB
    public boolean insertUser(User user) {

    if (emailExists(user.getEmail())) {
        System.out.println("Email already exists!");
        return false;
    }

    if (usernameExists(user.getUsername())) {
        System.out.println("Username already exists!");
        return false;
    }

    Document doc = new Document("id", user.getId())
            .append("name", user.getName())
            .append("username", user.getUsername())
            .append("email", user.getEmail())
            .append("phoneNumber", user.getPhoneNumber())
            .append("password", user.getPassword())
            .append("role", user.getRole());

    collection.insertOne(doc);

    System.out.println("User registered successfully!");
    return true;
}
}