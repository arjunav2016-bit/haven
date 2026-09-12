package haven.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static volatile MongoDBConnection instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private static final String CONNECTION_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "haven_db";

    private MongoDBConnection() {
        this.mongoClient = MongoClients.create(CONNECTION_URI);
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

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}