package haven.test;

import haven.repository.inmemory.InMemoryStore;
import haven.repository.mongo.MongoDBConnection;
import haven.event.HavenEventBus;
import haven.repository.RepositoryProvider;
import haven.repository.inmemory.FilePersistenceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Base test fixture providing clean test isolation:
 * - Redirects FilePersistenceManager away from real data/haven_store.json to a temp directory
 * - Resets singletons (InMemoryStore, HavenEventBus, RepositoryProvider) before and after each test
 * - Provides helpers for conditional skipping of Mongo and Swing tests
 */
public abstract class TestBase {

    @TempDir
    protected Path tempDir;

    @BeforeEach
    public void setUpIsolation() throws IOException {
        Path tempStore = tempDir.resolve("test_haven_store.json");
        FilePersistenceManager.setStorePath(tempStore.toString());
        InMemoryStore.resetInstance();
        HavenEventBus.resetInstance();
        RepositoryProvider.reset();
        haven.service.ServiceProvider.reset();
    }

    @AfterEach
    public void tearDownIsolation() {
        FilePersistenceManager.resetStorePath();
        InMemoryStore.resetInstance();
        HavenEventBus.resetInstance();
        RepositoryProvider.reset();
        haven.service.ServiceProvider.reset();
    }

    protected void assumeMongoAvailable() {
        Assumptions.assumeTrue(
                !MongoDBConnection.getInstance().isOfflineMode() && MongoDBConnection.getInstance().testConnection(),
                "Skipping test: MongoDB server is not reachable on localhost:27017"
        );
    }

    protected void assumeNotHeadless() {
        Assumptions.assumeFalse(
                GraphicsEnvironment.isHeadless(),
                "Skipping test: GraphicsEnvironment is headless"
        );
    }
}
