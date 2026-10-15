package haven.pattern;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

public class MongoDBConnection {
    private static volatile MongoDBConnection instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private volatile boolean offlineMode = false;

    private static final String CONNECTION_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "haven_db";

    private MongoDBConnection() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(CONNECTION_URI))
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(2500, TimeUnit.MILLISECONDS))
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(DATABASE_NAME);
    }

    public static MongoDBConnection getInstance() {
        if (instance == null) {
            synchronized (MongoDBConnection.class) {
                if (instance == null) {
                    instance = new MongoDBConnection();
                }
            }
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean testConnection() {
        if (offlineMode) return false;
        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception ignored) {}
        }
    }
}
