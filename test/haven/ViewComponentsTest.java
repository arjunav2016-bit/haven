package haven.test;

import haven.view.EmptyStatePanel;
import haven.view.SplashScreenWindow;
import haven.view.StatusBadgeRenderer;
import haven.view.ToastNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Swing UI & View Components Tests")
public class ViewComponentsTest extends TestBase {

    @BeforeEach
    public void checkGuiEnvironment() {
        assumeNotHeadless();
    }

    @Test
    @DisplayName("[TEST 12] Modern UI Feedback Components (Toast & EmptyState)")
    public void test12_ModernUiFeedbackComponentsToastAndEmptyState() {
        EmptyStatePanel emptySearch = EmptyStatePanel.forSearch(() -> {});
        EmptyStatePanel emptyWish = EmptyStatePanel.forWishlist(() -> {});
        EmptyStatePanel emptyEnq = EmptyStatePanel.forEnquiries(() -> {});

        assertNotNull(emptySearch, "EmptyStatePanel for search should not be null");
        assertNotNull(emptyWish, "EmptyStatePanel for wishlist should not be null");
        assertNotNull(emptyEnq, "EmptyStatePanel for enquiries should not be null");

        ToastNotification toastSuccess = new ToastNotification("Test Notification", ToastNotification.Type.SUCCESS);
        assertNotNull(toastSuccess, "ToastNotification component failed to construct");
    }

    @Test
    @DisplayName("[TEST 14] StatusBadgeRenderer Reusability (DRY)")
    public void test14_StatusBadgeRendererReusability() {
        StatusBadgeRenderer renderer = new StatusBadgeRenderer();
        assertNotNull(renderer, "StatusBadgeRenderer failed to instantiate");

        JTable dummyTable = new JTable();
        for (String status : new String[]{"PENDING", "CONFIRMED", "CONTACTED", "DECLINED", "CANCELLED", "UNKNOWN"}) {
            Component c = renderer.getTableCellRendererComponent(dummyTable, status, false, false, 0, 0);
            assertNotNull(c, "StatusBadgeRenderer returned null for status: " + status);
        }
    }

    @Test
    @DisplayName("[TEST 35] SplashScreenWindow Lifecycle & Progress")
    public void test35_SplashScreenWindowLifecycleAndProgress() {
        SplashScreenWindow splash = new SplashScreenWindow();
        assertEquals(520, splash.getWidth(), "SplashScreen width must be 520");
        assertEquals(290, splash.getHeight(), "SplashScreen height must be 290");

        splash.setProgress(15, "Testing init stage...");
        splash.setProgress(70, "Testing repository stage...");
        splash.setProgress(100, "Testing complete stage...");
        splash.dispose();
    }
}
