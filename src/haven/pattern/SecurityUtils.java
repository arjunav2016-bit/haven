package haven.pattern;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    /**
     * Hashes a plain password into a 64-character SHA-256 hexadecimal string.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Verifies if candidate plain password matches the stored password.
     * Supports both SHA-256 hashes and legacy plaintext for backward compatibility.
     */
    public static boolean verifyPassword(String candidatePassword, String storedPasswordOrHash) {
        if (candidatePassword == null || storedPasswordOrHash == null) {
            return false;
        }
        // Match against SHA-256 hash
        String candidateHash = hashPassword(candidatePassword);
        if (candidateHash.equalsIgnoreCase(storedPasswordOrHash)) {
            return true;
        }
        // Match against legacy plain text
        return candidatePassword.equals(storedPasswordOrHash);
    }

    /**
     * Checks if a stored password string is already a 64-character SHA-256 hash.
     */
    public static boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.matches("^[a-fA-F0-9]{64}$");
    }
}
