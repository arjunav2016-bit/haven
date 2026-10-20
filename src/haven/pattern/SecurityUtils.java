package haven.pattern;

/**
 * Backward-compatibility adapter for {@link haven.util.SecurityUtils}.
 * @deprecated Use {@link haven.util.SecurityUtils} directly.
 */
@Deprecated
public class SecurityUtils {

    public static String hashPassword(String plainPassword) {
        return haven.util.SecurityUtils.hashPassword(plainPassword);
    }

    public static boolean verifyPassword(String candidatePassword, String storedPasswordOrHash) {
        return haven.util.SecurityUtils.verifyPassword(candidatePassword, storedPasswordOrHash);
    }

    public static boolean isHashed(String storedPassword) {
        return haven.util.SecurityUtils.isHashed(storedPassword);
    }
}

