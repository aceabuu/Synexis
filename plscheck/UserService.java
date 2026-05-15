import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/**
 * UserService — Module E: User Account Module
 *
 * Responsibilities (per SRS / Progress Report):
 *   - Registration with email + password validation
 *   - Secure login / logout
 *   - Password recovery workflow (simulated)
 *   - User profile management (shipping & billing preferences)
 *   - Security: SHA-256 + random salt (Bcrypt-equivalent for pure-JVM demo)
 *   - Reliability: session token (JWT-style) for stateless session management
 */
public class UserService {
    private static UserService instance;

    private final List<User> users = new ArrayList<>();
    // sessionToken -> userId  (simulates JWT stateless session)
    private final Map<String, String> activeSessions = new HashMap<>();
    // passwordResetTokens: email -> reset token (simulates recovery email)
    private final Map<String, String> passwordResetTokens = new HashMap<>();

    private UserService() {
        seedDefaultAccounts();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    // ─── Seed ────────────────────────────────────────────────────────────────

    private void seedDefaultAccounts() {
        // Default admin account
        String adminSalt   = generateSalt();
        String adminHash   = hashPassword("admin123", adminSalt);
        User admin = new User(UUID.randomUUID().toString(),
                              "admin@qdreon.com", adminSalt + ":" + adminHash,
                              "Qdreon Admin", "admin");
        users.add(admin);

        // Demo customer account
        String custSalt  = generateSalt();
        String custHash  = hashPassword("customer123", custSalt);
        User customer = new User(UUID.randomUUID().toString(),
                                 "customer@qdreon.com", custSalt + ":" + custHash,
                                 "Customer", "customer");
        customer.setPhone("09171234567");
        customer.setDefaultShippingAddress("123 Demo St, Cebu City, 6000");
        users.add(customer);
    }

    // ─── Security helpers ─────────────────────────────────────────────────────

    private String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        // storedHash format: "salt:hash"
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) return false;
        String salt        = parts[0];
        String expectedHash = parts[1];
        return expectedHash.equals(hashPassword(password, salt));
    }

    private String generateSessionToken() {
        return "QDR-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    // ─── Registration ─────────────────────────────────────────────────────────

    /**
     * Registers a new customer account.
     * Returns null on success, or an error message string on failure.
     */
    public String register(String fullName, String email, String password) {
        // Validation
        if (fullName == null || fullName.trim().isEmpty())
            return "Full name is required.";
        if (!isValidEmail(email))
            return "Please enter a valid email address.";
        if (password == null || password.length() < 6)
            return "Password must be at least 6 characters.";
        if (getUserByEmail(email) != null)
            return "An account with this email already exists.";

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        User user = new User(UUID.randomUUID().toString(),
                             email.toLowerCase().trim(),
                             salt + ":" + hash,
                             fullName.trim(),
                             "customer");
        users.add(user);
        return null; // success
    }

    // ─── Login / Logout ───────────────────────────────────────────────────────

    /**
     * Authenticates credentials.
     * Returns a session token on success, or null on failure.
     */
    public String login(String email, String password) {
        User user = getUserByEmail(email);
        if (user == null) return null;
        if (!verifyPassword(password, user.getPasswordHash())) return null;

        String token = generateSessionToken();
        activeSessions.put(token, user.getId());
        return token;
    }

    public void logout(String sessionToken) {
        activeSessions.remove(sessionToken);
    }

    /** Resolve a session token to the logged-in user, or null if invalid/expired. */
    public User getUserBySession(String sessionToken) {
        if (sessionToken == null) return null;
        String userId = activeSessions.get(sessionToken);
        if (userId == null) return null;
        return users.stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
    }

    // ─── Password Recovery (simulated) ───────────────────────────────────────

    /**
     * Initiates password recovery.
     * Returns a reset token (would normally be emailed). Returns null if email not found.
     */
    public String initiatePasswordReset(String email) {
        User user = getUserByEmail(email);
        if (user == null) return null;
        String token = UUID.randomUUID().toString();
        passwordResetTokens.put(email.toLowerCase(), token);
        return token;
    }

    /**
     * Completes password reset with a token.
     * Returns true on success.
     */
    public boolean resetPassword(String email, String token, String newPassword) {
        String stored = passwordResetTokens.get(email.toLowerCase());
        if (stored == null || !stored.equals(token)) return false;
        if (newPassword == null || newPassword.length() < 6) return false;

        User user = getUserByEmail(email);
        if (user == null) return false;

        String salt = generateSalt();
        String hash = hashPassword(newPassword, salt);
        user.setPasswordHash(salt + ":" + hash);
        passwordResetTokens.remove(email.toLowerCase());
        return true;
    }

    // ─── Profile Management ───────────────────────────────────────────────────

    public boolean updateProfile(String sessionToken, String fullName, String phone, String address) {
        User user = getUserBySession(sessionToken);
        if (user == null) return false;
        if (fullName != null && !fullName.trim().isEmpty()) user.setFullName(fullName.trim());
        if (phone != null) user.setPhone(phone.trim());
        if (address != null) user.setDefaultShippingAddress(address.trim());
        return true;
    }

    public boolean changePassword(String sessionToken, String currentPassword, String newPassword) {
        User user = getUserBySession(sessionToken);
        if (user == null) return false;
        if (!verifyPassword(currentPassword, user.getPasswordHash())) return false;
        if (newPassword == null || newPassword.length() < 6) return false;

        String salt = generateSalt();
        String hash = hashPassword(newPassword, salt);
        user.setPasswordHash(salt + ":" + hash);
        return true;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public User getUserByEmail(String email) {
        if (email == null) return null;
        String normalized = email.toLowerCase().trim();
        return users.stream().filter(u -> u.getEmail().equals(normalized)).findFirst().orElse(null);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        return e.contains("@") && e.contains(".") && e.indexOf("@") < e.lastIndexOf(".");
    }
}
