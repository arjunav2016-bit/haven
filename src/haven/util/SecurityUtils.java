package haven.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * SecurityUtils - Cryptographic password hashing and verification utility.
 *
 * <p>Implements industry-standard PBKDF2 with HMAC-SHA256 (65,536 iterations, 16-byte random salt,
 * 256-bit derived key) and constant-time equality comparisons to prevent timing attacks and rainbow tables.</p>
 *
 * <p>Also supports safe backward compatibility for legacy unsalted SHA-256 hashes without allowing
 * pass-the-hash attacks.</p>
 */
public final class SecurityUtils {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtils() {}

    /**
     * Hashes a plaintext password using salted PBKDF2WithHmacSHA256.
     *
     * @param plainPassword plaintext password to hash
     * @return formatted hash string: PBKDF2$&lt;iterations&gt;$&lt;saltHex&gt;$&lt;hashHex&gt;
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return "";
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return hashWithSalt(plainPassword, salt, ITERATIONS);
    }

    /**
     * Hashes a password with an explicit salt and iteration count.
     */
    public static String hashWithSalt(String plainPassword, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, iterations, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return "PBKDF2$" + iterations + "$" + bytesToHex(salt) + "$" + bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 hashing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies whether candidate plain password matches stored password or hash.
     * Prevents Pass-the-Hash attacks by rejecting raw hash submissions.
     *
     * @param candidatePassword plaintext candidate entered by user
     * @param storedPasswordOrHash hash or legacy password stored in database
     * @return true if valid match, false otherwise
     */
    public static boolean verifyPassword(String candidatePassword, String storedPasswordOrHash) {
        if (candidatePassword == null || storedPasswordOrHash == null) {
            return false;
        }

        // 1. Verify salted PBKDF2 hash
        if (storedPasswordOrHash.startsWith("PBKDF2$")) {
            String[] parts = storedPasswordOrHash.split("\\$");
            if (parts.length == 4) {
                try {
                    int iterations = Integer.parseInt(parts[1]);
                    byte[] salt = hexToBytes(parts[2]);
                    byte[] expectedHash = hexToBytes(parts[3]);

                    PBEKeySpec spec = new PBEKeySpec(candidatePassword.toCharArray(), salt, iterations, KEY_LENGTH);
                    SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
                    byte[] candidateHash = skf.generateSecret(spec).getEncoded();

                    return MessageDigest.isEqual(candidateHash, expectedHash);
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }

        // 2. Verify legacy 64-character SHA-256 hash (constant-time, no pass-the-hash fallback)
        if (isSha256Hex(storedPasswordOrHash)) {
            String candidateSha256 = legacySha256(candidatePassword);
            byte[] candBytes = candidateSha256.getBytes(StandardCharsets.UTF_8);
            byte[] storedBytes = storedPasswordOrHash.toLowerCase().getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(candBytes, storedBytes);
        }

        // 3. Fallback only for non-hash raw passwords (e.g. dev mock), but never match if candidate equals a hash!
        if (!isHashed(storedPasswordOrHash)) {
            byte[] candBytes = candidatePassword.getBytes(StandardCharsets.UTF_8);
            byte[] storedBytes = storedPasswordOrHash.getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(candBytes, storedBytes);
        }

        return false;
    }

    /**
     * Checks if a stored password string is already hashed (either PBKDF2 or SHA-256).
     */
    public static boolean isHashed(String storedPassword) {
        if (storedPassword == null) return false;
        return storedPassword.startsWith("PBKDF2$") || isSha256Hex(storedPassword);
    }

    private static boolean isSha256Hex(String str) {
        return str != null && str.matches("^[a-fA-F0-9]{64}$");
    }

    /**
     * Generates a legacy SHA-256 hash (used only for backward compatibility verification).
     */
    public static String legacySha256(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
