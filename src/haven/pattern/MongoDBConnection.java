package haven.pattern;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBConnection {
    private static final Logger LOGGER = Logger.getLogger(MongoDBConnection.class.getName());
    private static volatile MongoDBConnection instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private volatile boolean offlineMode = false;

    private static final String CONNECTION_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "haven_db";

    private MongoDBConnection() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(CONNECTION_URI))
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(2000, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder -> builder.connectTimeout(2000, TimeUnit.MILLISECONDS).readTimeout(2000, TimeUnit.MILLISECONDS))
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
        } catch (MongoException e) {
            LOGGER.log(Level.FINE, "MongoDB ping connection test failed: {0}", e.getMessage());
            return false;
        } catch (RuntimeException e) {
            LOGGER.log(Level.FINE, "Unexpected error during MongoDB ping test: {0}", e.getMessage());
            return false;
        }
    }

    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Error closing MongoDB client", e);
            }
        }
    }
}
